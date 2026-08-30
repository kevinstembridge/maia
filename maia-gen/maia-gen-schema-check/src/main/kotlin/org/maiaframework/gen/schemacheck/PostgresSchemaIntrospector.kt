package org.maiaframework.gen.schemacheck

import java.sql.Connection

/**
 * Introspects the real tables in a given set of Postgres schemas — columns (with types
 * normalized via [PostgresActualTypeNormalizer]), primary key columns, foreign keys, and
 * non-primary-key indexes — via plain JDBC queries against `information_schema`/`pg_catalog`.
 *
 * Does NOT own the lifecycle of the [Connection] passed in: the caller is responsible for
 * opening and closing it.
 */
class PostgresSchemaIntrospector(private val connection: Connection) {

    fun introspectSchemas(schemas: Set<String>): List<ActualTableDef> {

        return listTables(schemas).map { (schema, table) ->
            ActualTableDef(
                schemaAndTableName = "$schema.$table",
                columns = columnsFor(schema, table),
                primaryKeyColumns = primaryKeyColumnsFor(schema, table),
                foreignKeys = foreignKeysFor(schema, table),
                indexes = indexesFor(schema, table),
            )
        }

    }

    private fun listTables(schemas: Set<String>): List<Pair<String, String>> {

        if (schemas.isEmpty()) return emptyList()

        val placeholders = schemas.joinToString(",") { "?" }
        val sql = """
            select table_schema, table_name
            from information_schema.tables
            where table_schema in ($placeholders)
            and table_type = 'BASE TABLE'
            order by table_schema, table_name
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            schemas.forEachIndexed { index, schema -> statement.setString(index + 1, schema) }
            statement.executeQuery().use { rs ->
                val tables = mutableListOf<Pair<String, String>>()
                while (rs.next()) {
                    tables.add(rs.getString("table_schema") to rs.getString("table_name"))
                }
                return tables
            }
        }

    }

    private fun columnsFor(schema: String, table: String): List<ActualColumnDef> {

        val sql = """
            select column_name, data_type, udt_name, character_maximum_length, is_nullable
            from information_schema.columns
            where table_schema = ? and table_name = ?
            order by ordinal_position
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val columns = mutableListOf<ActualColumnDef>()
                while (rs.next()) {
                    val characterMaximumLength = rs.getInt("character_maximum_length").takeUnless { rs.wasNull() }
                    columns.add(
                        ActualColumnDef(
                            name = rs.getString("column_name"),
                            postgresType = PostgresActualTypeNormalizer.normalize(
                                rs.getString("data_type"),
                                rs.getString("udt_name"),
                                characterMaximumLength,
                            ),
                            nullable = rs.getString("is_nullable") == "YES",
                        )
                    )
                }
                return columns
            }
        }

    }

    private fun primaryKeyColumnsFor(schema: String, table: String): List<String> {

        val sql = """
            select kcu.column_name
            from information_schema.table_constraints tc
            join information_schema.key_column_usage kcu
                on tc.constraint_name = kcu.constraint_name and tc.table_schema = kcu.table_schema
            where tc.constraint_type = 'PRIMARY KEY' and tc.table_schema = ? and tc.table_name = ?
            order by kcu.ordinal_position
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val columns = mutableListOf<String>()
                while (rs.next()) columns.add(rs.getString("column_name"))
                return columns
            }
        }

    }

    private fun foreignKeysFor(schema: String, table: String): List<ActualForeignKeyDef> {

        val sql = """
            select
                kcu.column_name,
                ccu.table_schema as referenced_schema,
                ccu.table_name as referenced_table,
                ccu.column_name as referenced_column
            from information_schema.table_constraints tc
            join information_schema.key_column_usage kcu
                on tc.constraint_name = kcu.constraint_name and tc.table_schema = kcu.table_schema
            join information_schema.constraint_column_usage ccu
                on tc.constraint_name = ccu.constraint_name and tc.table_schema = ccu.table_schema
            where tc.constraint_type = 'FOREIGN KEY' and tc.table_schema = ? and tc.table_name = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val foreignKeys = mutableListOf<ActualForeignKeyDef>()
                while (rs.next()) {
                    foreignKeys.add(
                        ActualForeignKeyDef(
                            columnName = rs.getString("column_name"),
                            referencedSchemaAndTable = "${rs.getString("referenced_schema")}.${rs.getString("referenced_table")}",
                            referencedColumn = rs.getString("referenced_column"),
                        )
                    )
                }
                return foreignKeys
            }
        }

    }

    private fun indexesFor(schema: String, table: String): List<ActualIndexDef> {

        // `pg_index.indkey` is an int2vector holding the index's column attnums in the index's
        // *declared* column order (which is what we need to compare against ExpectedIndexDef's
        // column order) — but joining it to pg_attribute via `a.attnum = any(ix.indkey)` loses that
        // order entirely; without any ORDER BY, array_agg would return rows in whatever order
        // pg_attribute happens to produce them (typically attnum order, not declared index order).
        // `array_position(ix.indkey::int2[], a.attnum)` looks up each column's position within the
        // declared indkey list, and array_agg orders by that, restoring the original column order.
        // The explicit `::int2[]` cast is required because indkey's int2vector type isn't accepted
        // directly by array_position, which expects a normal array.
        val sql = """
            select
                ic.relname as index_name,
                ix.indisunique as is_unique,
                array_agg(a.attname order by array_position(ix.indkey::int2[], a.attnum)) as column_names
            from pg_index ix
            join pg_class ic on ic.oid = ix.indexrelid
            join pg_class tc on tc.oid = ix.indrelid
            join pg_namespace n on n.oid = tc.relnamespace
            join pg_attribute a on a.attrelid = tc.oid and a.attnum = any(ix.indkey)
            where n.nspname = ? and tc.relname = ? and ix.indisprimary = false
            group by ic.relname, ix.indisunique
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val indexes = mutableListOf<ActualIndexDef>()
                while (rs.next()) {
                    @Suppress("UNCHECKED_CAST")
                    val columnNames = (rs.getArray("column_names").array as Array<String>).toList()
                    indexes.add(
                        ActualIndexDef(
                            name = rs.getString("index_name"),
                            columns = columnNames,
                            unique = rs.getBoolean("is_unique"),
                        )
                    )
                }
                return indexes
            }
        }

    }

}

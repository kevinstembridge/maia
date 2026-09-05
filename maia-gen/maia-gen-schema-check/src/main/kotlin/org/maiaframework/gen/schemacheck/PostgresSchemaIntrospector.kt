package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.schemacheck.actual.ActualColumnDef
import org.maiaframework.gen.schemacheck.actual.ActualForeignKeyDef
import org.maiaframework.gen.schemacheck.actual.ActualIndexDef
import org.maiaframework.gen.schemacheck.actual.ActualTableDef
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
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
            val primaryKey = primaryKeyFor(schema, table)
            ActualTableDef(
                schemaAndTableName = "$schema.$table",
                columnDefs = columnsFor(schema, table),
                primaryKeyColumnNames = primaryKey.columns,
                foreignKeys = foreignKeysFor(schema, table),
                indexes = indexesFor(schema, table),
                primaryKeyConstraintName = primaryKey.constraintName,
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
                            name = TableColumnName(rs.getString("column_name")),
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

    private data class PrimaryKeyInfo(val columns: List<TableColumnName>, val constraintName: String?)

    private fun primaryKeyFor(schema: String, table: String): PrimaryKeyInfo {

        val sql = """
            select kcu.column_name, tc.constraint_name
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
                val columns = mutableListOf<TableColumnName>()
                var constraintName: String? = null
                while (rs.next()) {
                    columns.add(TableColumnName(rs.getString("column_name")))
                    constraintName = rs.getString("constraint_name")
                }
                return PrimaryKeyInfo(columns, constraintName)
            }
        }

    }

    private fun foreignKeysFor(schema: String, table: String): List<ActualForeignKeyDef> {

        // information_schema.key_column_usage/constraint_column_usage have no join key that pairs
        // a referencing column with its corresponding referenced column -- joining them on
        // constraint_name alone produces every combination of local and referenced columns (a
        // cross product) once a foreign key spans more than one column, not just the correct
        // pairs. pg_constraint's conkey/confkey are parallel int2[] arrays already in the correct
        // per-column correspondence for that one constraint, so zipping them with a two-array
        // unnest() (which pairs elements positionally) sidesteps the ambiguity entirely, for both
        // single- and multi-column foreign keys.
        val sql = """
            select
                local_att.attname as column_name,
                fn.nspname as referenced_schema,
                fc.relname as referenced_table,
                ref_att.attname as referenced_column
            from pg_constraint con
            join pg_class c on c.oid = con.conrelid
            join pg_namespace n on n.oid = c.relnamespace
            join pg_class fc on fc.oid = con.confrelid
            join pg_namespace fn on fn.oid = fc.relnamespace
            join unnest(con.conkey, con.confkey) as cols(local_attnum, ref_attnum) on true
            join pg_attribute local_att on local_att.attrelid = con.conrelid and local_att.attnum = cols.local_attnum
            join pg_attribute ref_att on ref_att.attrelid = con.confrelid and ref_att.attnum = cols.ref_attnum
            where con.contype = 'f' and n.nspname = ? and c.relname = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val foreignKeys = mutableListOf<ActualForeignKeyDef>()
                while (rs.next()) {
                    foreignKeys.add(
                        ActualForeignKeyDef(
                            columnName = TableColumnName(rs.getString("column_name")),
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
                    val columnNames = (rs.getArray("column_names").array as Array<String>).map { TableColumnName(it) }
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

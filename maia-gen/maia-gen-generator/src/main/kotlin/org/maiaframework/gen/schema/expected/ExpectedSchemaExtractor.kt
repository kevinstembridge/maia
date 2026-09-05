package org.maiaframework.gen.schema.expected

import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.jdbc.JdbcCompatibleType


class ExpectedSchemaExtractor {


    fun extract(entityHierarchies: List<EntityHierarchy>): List<ExpectedTableDef> {

        return entityHierarchies.map { extractTable(it) }

    }


    private fun extractTable(entityHierarchy: EntityHierarchy): ExpectedTableDef {

        val rootEntityDef = entityHierarchy.entityDef

        val sqlFieldsForColumns = `build sqlFieldDefs`(entityHierarchy)

        val (historizedFkSqlFields, plainFkSqlFields) = sqlFieldsForColumns
            .filter { it.fieldType is ForeignKeyFieldType }
            .partition {
                rootEntityDef.isHistoryEntity &&
                    (it.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef.withVersionHistory.value
            }

        return ExpectedTableDef(
            schemaAndTableName = schemaAndTableNameFor(rootEntityDef),
            columns = `determine expected columns`(entityHierarchy, sqlFieldsForColumns),
            foreignKeys = `determine foreign keys`(plainFkSqlFields),
            indexes = `determine expected indexes`(entityHierarchy),
            compositeForeignKeys = `determine expected composite foreign keys`(historizedFkSqlFields, entityHierarchy),
        )

    }


    private fun `build sqlFieldDefs`(entityHierarchy: EntityHierarchy): List<SqlFieldDef> {

        val rootEntityDef = entityHierarchy.entityDef
        val nonDerivedSqlFields = `get non-derived sql fields from`(entityHierarchy)
        return `remove effective timestamp columns from`(rootEntityDef, nonDerivedSqlFields)

    }


    private fun `remove effective timestamp columns from`(
        rootEntityDef: EntityDef,
        nonDerivedSqlFields: List<SqlFieldDef>
    ): List<SqlFieldDef> {

        val effectiveTimestampColumnNames = setOf("effective_from", "effective_to")

        val sqlFieldsForColumns = if (rootEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
            nonDerivedSqlFields.filterNot { it.tableColumnName.value in effectiveTimestampColumnNames }
        } else {
            nonDerivedSqlFields
        }

        return sqlFieldsForColumns

    }


    private fun `get non-derived sql fields from`(entityHierarchy: EntityHierarchy): List<SqlFieldDef> {

        return entityHierarchy.allFieldDefsSorted
            .filterNot { it.isDerived.value }
            .groupBy { it.tableColumnName }
            .mapValues { toSqlFieldDef(it, entityHierarchy) }
            .values
            .toList()

    }


    private fun `determine expected columns`(
        entityHierarchy: EntityHierarchy,
        sqlFieldsForColumns: List<SqlFieldDef>
    ): List<ExpectedColumnDef> {

        val columns = mutableListOf<ExpectedColumnDef>()

        val baseColumns = sqlFieldsForColumns.map { sqlFieldDef ->
            ExpectedColumnDef(
                name = sqlFieldDef.tableColumnName,
                postgresType = "${sqlFieldDef.fieldType.jdbcCompatibleType.postgresDataType}${sqlFieldDef.sizeConstraint}",
                nullable = sqlFieldDef.nullable,
                isPrimaryKey = sqlFieldDef.isPrimaryKey
            )
        }

        columns.addAll(baseColumns)

        if (entityHierarchy.hasSubclasses()) {
            columns.plus(ExpectedColumnDef(TableColumnName.typeDiscriminator, "text", nullable = false, isPrimaryKey = false))
        }

        if (entityHierarchy.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
            columns.plus(ExpectedColumnDef(TableColumnName.effectiveRange, "tstzrange", nullable = false, isPrimaryKey = false))
        }

        return columns

    }


    private fun `determine foreign keys`(plainFkSqlFields: List<SqlFieldDef>): List<ExpectedForeignKeyDef> {

        return plainFkSqlFields.map { sqlFieldDef ->
            val foreignEntityDef = (sqlFieldDef.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef
            ExpectedForeignKeyDef(
                columnName = sqlFieldDef.tableColumnName,
                referencedSchemaAndTable = schemaAndTableNameFor(foreignEntityDef),
                referencedColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName,
            )
        }

    }


    private fun `determine expected composite foreign keys`(
        historizedFkSqlFields: List<SqlFieldDef>,
        entityHierarchy: EntityHierarchy
    ): List<ExpectedCompositeForeignKeyDef> {

        return historizedFkSqlFields.map { sqlFieldDef ->
            val fieldType = sqlFieldDef.fieldType as ForeignKeyFieldType
            val foreignEntityDef = fieldType.foreignKeyFieldDef.foreignEntityDef
            val foreignHistoryEntityDef = foreignEntityDef.historyEntityDef!!

            val versionColumnName = entityHierarchy.allFieldDefsSorted
                .first { it.classFieldName == fieldType.foreignKeyFieldDef.foreignKeyFieldName.withSuffix("Version") }
                .tableColumnName
            val foreignIdColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName
            val foreignVersionColumn = foreignHistoryEntityDef.`tableColumnName of version field`()

            ExpectedCompositeForeignKeyDef(
                columnNames = listOf(sqlFieldDef.tableColumnName, versionColumnName),
                referencedSchemaAndTable = schemaAndTableNameFor(foreignHistoryEntityDef),
                referencedColumns = listOf(foreignIdColumn, foreignVersionColumn),
            )

        }

    }


    private fun EntityDef.`tableColumnName of version field`(): TableColumnName {

        return allEntityFieldsSorted
            .first { it.classFieldName == ClassFieldName.version }
            .tableColumnName

    }


    private fun `determine expected indexes`(entityHierarchy: EntityHierarchy): List<ExpectedIndexDef> {

        return entityHierarchy.entityDefs
            .reversed()
            .flatMap { it.databaseIndexDefs }
            .distinctBy { databaseIndexDef -> databaseIndexDef.indexDef.indexFieldDefs.map { it.databaseColumnName } }
            .map { databaseIndexDef ->

                val indexColumnNames = databaseIndexDef.indexDef.indexFieldDefs
                    .map { it.databaseColumnName }
                    .toMutableList()

                if (entityHierarchy.hasSubclasses()) {
                    indexColumnNames.add(TableColumnName.typeDiscriminator)
                }

                ExpectedIndexDef(
                    name = databaseIndexDef.indexDef.indexName.value,
                    columns = indexColumnNames,
                    unique = databaseIndexDef.isUnique,
                )

            }

    }


    private fun toSqlFieldDef(
        entityFieldDefs: Map.Entry<TableColumnName, List<EntityFieldDef>>,
        entityHierarchy: EntityHierarchy
    ): SqlFieldDef {

        val tableColumnName = entityFieldDefs.key
        val fieldTypes = entityFieldDefs.value.map { it.fieldType }.toSet()

        if (fieldTypes.size > 1) {
            throw IllegalStateException("Expecting all fields with database column name of $tableColumnName in entity hierarchy ${entityHierarchy.entityDef.entityBaseName} to have the same type. Found: $fieldTypes")
        }

        val sizeConstraint = maxSizeConstraintFor(entityFieldDefs)

        return SqlFieldDef(
            fieldType = fieldTypes.first(),
            tableColumnName = tableColumnName,
            sizeConstraint = sizeConstraint,
            nullable = entityFieldDefs.value.any { shouldBeNullable(it, entityHierarchy.entityDef) },
            isPrimaryKey = entityFieldDefs.value.any { it.isPrimaryKey.value },
        )

    }


    private fun maxSizeConstraintFor(
        entityFieldDefs: Map.Entry<TableColumnName, List<EntityFieldDef>>
    ): String {

        val maxSizeConstraint = determineMaxSizeConstraintFor(entityFieldDefs.value)
        return determineSizeConstraintFor(maxSizeConstraint)

    }


    private fun shouldBeNullable(
        entityFieldDef: EntityFieldDef,
        baseEntityDef: EntityDef
    ): Boolean {

        return entityFieldDef.nullable
                || (entityFieldDef.isNotFromBaseEntity(baseEntityDef) && !notNullByFramework(entityFieldDef))

    }


    private fun EntityFieldDef.isNotFromBaseEntity(baseEntityDef: EntityDef): Boolean {
        return entityBaseName != baseEntityDef.entityBaseName
    }


    private fun notNullByFramework(entityFieldDef: EntityFieldDef): Boolean {

        return (entityFieldDef.classFieldName == ClassFieldName.id
                || entityFieldDef.classFieldName == ClassFieldName.createdTimestamp
                || entityFieldDef.classFieldName == ClassFieldName.lastModifiedTimestamp
                || entityFieldDef.classFieldName == ClassFieldName.version)

    }


    private fun determineMaxSizeConstraintFor(entityFieldDefs: List<EntityFieldDef>): EntityFieldDef? {

        if (entityFieldDefs.any { it.classFieldDef.lengthConstraint == null }) {
            return null
        }

        return entityFieldDefs
            .maxBy { it.classFieldDef.lengthConstraint?.max ?: 0 }

    }


    private fun determineSizeConstraintFor(entityFieldDef: EntityFieldDef?): String {

        if (entityFieldDef == null) {
            return ""
        }

        return when (entityFieldDef.fieldType.jdbcCompatibleType) {
            JdbcCompatibleType.varchar -> "(${maxLengthOf(entityFieldDef)})"
            else -> ""
        }

    }


    private fun maxLengthOf(entityFieldDef: EntityFieldDef): Long {

        return entityFieldDef.classFieldDef.lengthConstraint?.max
            ?: throw IllegalArgumentException("Expecting entity field '${entityFieldDef.classFieldName}' on entity '${entityFieldDef.entityBaseName}' to have a max length constraint.")

    }


    private fun schemaAndTableNameFor(entityDef: EntityDef) = "${entityDef.schemaName}.${entityDef.tableName.rawTableName}"


    private data class SqlFieldDef(
        val fieldType: FieldType,
        val tableColumnName: TableColumnName,
        val sizeConstraint: String,
        val nullable: Boolean,
        val isPrimaryKey: Boolean,
    )


}

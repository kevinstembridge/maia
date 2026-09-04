package org.maiaframework.gen.schema

import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.jdbc.JdbcCompatibleType


data class ExpectedColumnDef(
    val name: String,
    val postgresType: String,
    val nullable: Boolean,
)


data class ExpectedForeignKeyDef(
    val columnName: String,
    val referencedSchemaAndTable: String,
    val referencedColumn: String,
)


data class ExpectedCompositeForeignKeyDef(
    val columnNames: List<String>,
    val referencedSchemaAndTable: String,
    val referencedColumns: List<String>,
)


data class ExpectedIndexDef(
    val name: String,
    val columns: List<String>,
    val unique: Boolean,
)


data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
    val compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
)


class ExpectedSchemaExtractor {


    fun extract(entityHierarchies: List<EntityHierarchy>): List<ExpectedTableDef> {

        return entityHierarchies.map { extractTable(it) }

    }


    private fun extractTable(entityHierarchy: EntityHierarchy): ExpectedTableDef {

        val baseEntityDef = entityHierarchy.entityDef

        val nonDerivedSqlFields = entityHierarchy.allFieldDefsSorted
            .filterNot { it.isDerived.value }
            .groupBy { it.tableColumnName }
            .mapValues { toSqlFieldDef(it, entityHierarchy) }
            .values
            .toList()

        val effectiveTimestampColumnNames = setOf("effective_from", "effective_to")

        val sqlFieldsForColumns = if (baseEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
            nonDerivedSqlFields.filterNot { it.tableColumnName.value in effectiveTimestampColumnNames }
        } else {
            nonDerivedSqlFields
        }

        val baseColumns = sqlFieldsForColumns.map { sqlFieldDef ->
            ExpectedColumnDef(
                name = sqlFieldDef.tableColumnName.value,
                postgresType = "${sqlFieldDef.fieldType.jdbcCompatibleType.postgresDataType}${sqlFieldDef.sizeConstraint}",
                nullable = sqlFieldDef.nullable,
            )
        }

        val withDiscriminator = if (entityHierarchy.hasSubclasses()) {
            baseColumns.plus(ExpectedColumnDef("type_discriminator", "text", nullable = false))
        } else {
            baseColumns
        }

        val columns = if (baseEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
            withDiscriminator.plus(ExpectedColumnDef("effective_range", "tstzrange", nullable = false))
        } else {
            withDiscriminator
        }

        val (historizedFkSqlFields, plainFkSqlFields) = sqlFieldsForColumns
            .filter { it.fieldType is ForeignKeyFieldType }
            .partition {
                baseEntityDef.isHistoryEntity &&
                    (it.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef.withVersionHistory.value
            }

        val foreignKeys = plainFkSqlFields.map { sqlFieldDef ->
            val foreignEntityDef = (sqlFieldDef.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef
            ExpectedForeignKeyDef(
                columnName = sqlFieldDef.tableColumnName.value,
                referencedSchemaAndTable = schemaAndTableNameFor(foreignEntityDef),
                referencedColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName.value,
            )
        }

        val compositeForeignKeys = historizedFkSqlFields.map { sqlFieldDef ->
            val fieldType = sqlFieldDef.fieldType as ForeignKeyFieldType
            val foreignEntityDef = fieldType.foreignKeyFieldDef.foreignEntityDef
            val foreignHistoryEntityDef = foreignEntityDef.historyEntityDef!!

            val versionColumnName = entityHierarchy.allFieldDefsSorted
                .first { it.classFieldName == fieldType.foreignKeyFieldDef.foreignKeyFieldName.withSuffix("Version") }
                .tableColumnName.value
            val foreignIdColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName.value
            val foreignVersionColumn = foreignHistoryEntityDef.allEntityFieldsSorted
                .first { it.classFieldName == ClassFieldName.version }.tableColumnName.value

            ExpectedCompositeForeignKeyDef(
                columnNames = listOf(sqlFieldDef.tableColumnName.value, versionColumnName),
                referencedSchemaAndTable = schemaAndTableNameFor(foreignHistoryEntityDef),
                referencedColumns = listOf(foreignIdColumn, foreignVersionColumn),
            )
        }

        val indexes = entityHierarchy.entityDefs
            .reversed()
            .flatMap { it.databaseIndexDefs }
            .distinctBy { databaseIndexDef -> databaseIndexDef.indexDef.indexFieldDefs.map { it.databaseColumnName } }
            .map { databaseIndexDef ->
                val typeDiscriminatorField = if (entityHierarchy.hasSubclasses()) listOf("type_discriminator") else emptyList()
                ExpectedIndexDef(
                    name = databaseIndexDef.indexDef.indexName.value,
                    columns = databaseIndexDef.indexDef.indexFieldDefs.map { it.databaseColumnName.value }.plus(typeDiscriminatorField),
                    unique = databaseIndexDef.isUnique,
                )
            }

        val primaryKeyColumns = nonDerivedSqlFields.filter { it.isPrimaryKey }.map { it.tableColumnName.value }

        return ExpectedTableDef(
            schemaAndTableName = schemaAndTableNameFor(baseEntityDef),
            columns = columns,
            primaryKeyColumns = primaryKeyColumns,
            foreignKeys = foreignKeys,
            indexes = indexes,
            compositeForeignKeys = compositeForeignKeys,
        )

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


    private fun maxSizeConstraintFor(entityFieldDefs: Map.Entry<TableColumnName, List<EntityFieldDef>>): String {

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

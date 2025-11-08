package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.domain.types.CollectionName
import org.maiaframework.domain.types.TypeDiscriminator

class EntityMetaRenderer(private val entityHierarchy: EntityHierarchy) : AbstractKotlinRenderer(entityHierarchy.entityDef.metaClassDef) {


    private val entityDef: EntityDef = entityHierarchy.entityDef


    override fun renderPreClassFields() {

        val entityUqcnFirstToLower = this.entityDef.entityUqcn.firstToLower()

        this.entityDef.allEntityFieldsSorted.forEach { fieldDef ->

            blankLine()
            appendLine("    const val ${fieldDef.classFieldDef.classFieldName} = \"${fieldDef.dbColumnFieldDef.tableColumnName}\"")

        }

        addImportFor(Fqcns.MAHANA_ENTITY_KEY)
        blankLine()
        appendLine("    val ENTITY_KEY = EntityKey(\"${this.entityDef.entityBaseName}\")")

        addImportFor(TypeDiscriminator::class.java)

        this.entityDef.typeDiscriminatorOrNull?.let { typeDiscriminator ->

            blankLine()
            appendLine("    val TYPE_DISCRIMINATOR = TypeDiscriminator(\"${typeDiscriminator.value}\")")

        }

        if (this.entityHierarchy.typeDiscriminators().isEmpty()) {
            blankLine()
            appendLine("    val TYPE_DISCRIMINATORS = sortedSetOf<TypeDiscriminator>()")

        } else {

            blankLine()
            appendLine("    val TYPE_DISCRIMINATORS = sortedSetOf(${
                this.entityHierarchy.typeDiscriminators().joinToString(", ") { "TypeDiscriminator(\"${it.value}\")" }
            })")

        }


        blankLine()

        when (entityDef.databaseType) {
            DatabaseType.JDBC -> {
                addImportFor(Fqcns.MAHANA_JDBC_SCHEMA_AND_TABLE_NAME)
                addImportFor(Fqcns.MAHANA_JDBC_TABLE_NAME)
                addImportFor(Fqcns.MAHANA_SCHEMA_NAME)
                appendLine("    val SCHEMA_NAME = SchemaName(\"${this.entityDef.schemaName.value}\")")
                blankLine()
                appendLine("    val TABLE_NAME = TableName(\"${this.entityDef.tableName}\")")
                blankLine()
                appendLine("    val SCHEMA_AND_TABLE_NAME = SchemaAndTableName(SCHEMA_NAME, TABLE_NAME)")
            }
            DatabaseType.MONGO -> {
                addImportFor(CollectionName::class.java)
                appendLine("    val COLLECTION_NAME = CollectionName(\"${this.entityDef.tableName}\")")
            }
        }

        if (entityDef.isStagingEntity) {
            blankLine()
            appendLine("    val columnWidths = listOf(${entityDef.stagingEntityFieldDefs.map { it.width }.joinToString(", ")})")
        }

        if (this.entityHierarchy.hasSubclasses()) {

            blankLine()
            blankLine()
            appendLine("    fun typeDiscriminatorFor($entityUqcnFirstToLower: ${this.entityDef.entityUqcn}): TypeDiscriminator {")
            blankLine()
            appendLine("        return when($entityUqcnFirstToLower) {")

            this.entityHierarchy.concreteEntityDefs.forEach { entityDef ->

                addImportFor(entityDef.entityClassDef.fqcn)

                appendLine("            is ${entityDef.entityUqcn} -> TypeDiscriminator(\"${entityDef.typeDiscriminator}\")")

            }

            appendLine("            else -> throw IllegalStateException(\"Unknown type of PartyEntity: $$entityUqcnFirstToLower\")")
            appendLine("        }")
            blankLine()
            appendLine("    }")

        }

        if (this.entityDef.databaseIndexDefs.isNotEmpty()) {

            blankLine()
            blankLine()
            appendLine("    object IndexName {")

            this.entityDef.databaseIndexDefs.forEach { entityIndexDef ->

                val indexName = entityIndexDef.indexDef.indexName
                blankLine()
                appendLine("        const val $indexName = \"$indexName\"")

            }

            blankLine()
            appendLine("    }")

        }

    }


    override fun renderFunctions() {

        `render function convertClassFieldNameToTableColumnName`()
        `render function mapCsvHeaderToTableColumnName`()

    }


    private fun `render function convertClassFieldNameToTableColumnName`() {

        blankLine()
        blankLine()
        appendLine("    fun convertClassFieldNameToTableColumnName(classFieldName: String): String {")
        blankLine()
        appendLine("        return when(classFieldName) {")

        this.entityDef.allEntityFieldsSorted.forEach { fieldDef ->
            appendLine("            \"${fieldDef.classFieldDef.classFieldName}\" -> \"${fieldDef.dbColumnFieldDef.tableColumnName}\"")
        }

        appendLine("            else ->")
        appendLine("                throw IllegalArgumentException(\"Unknown classFieldName [\$classFieldName]\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


    private fun `render function mapCsvHeaderToTableColumnName`() {

        if (this.entityDef.stagingEntityFieldDefs.isEmpty()) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    val mapCsvHeaderToTableColumnName: (String) -> String? = { csvHeaderName ->")
        blankLine()
        appendLine("        when (csvHeaderName) {")

        this.entityDef.stagingEntityFieldDefs.forEach { mapping ->
            appendLine("            \"${mapping.dataRowHeaderName}\" -> \"${mapping.tableColumnName}\"")
        }

        appendLine("            else -> null")
        appendLine("        }")
        blankLine()
        appendLine("    }")

    }


}

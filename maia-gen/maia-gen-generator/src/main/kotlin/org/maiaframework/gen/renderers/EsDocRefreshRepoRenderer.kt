package org.maiaframework.gen.renderers

import org.maiaframework.domain.types.TypeDiscriminator
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.EsRefreshRole
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.JoinHop
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ConstructorArg
import org.maiaframework.gen.spec.definition.lang.Fqcn


/**
 * Renders the repo holding the `findRootIdsVia<Entity>` queries of a hand-built EsDoc that declares `watchesVia`
 * sources. Each query returns the ids of the source entity rows that reach a given row of the watched entity by
 * following the declared foreign keys, which are the documents to refresh when that row changes. The SQL has the
 * same shape as the reverse joins generated for searchable DTOs.
 */
class EsDocRefreshRepoRenderer(
    private val esDocDef: EsDocDef,
    private val applicationModelDef: ApplicationModelDef
) : AbstractKotlinRenderer(
    esDocDef.refreshRepoClassDef
) {


    private val reverseJoins = EsReverseJoins.reverseJoinsFor(esDocDef)


    private val rootEntityDef = esDocDef.refreshDependencies.first { it.role == EsRefreshRole.ROOT && it.isRoot }.entityDef


    /** Every entity whose table the queries mention. */
    private val tableEntityDefs = (listOf(rootEntityDef) + reverseJoins.flatMap { join -> join.dependency.pathsFromRoot.flatMap { path -> path.map { it.toEntity } } }).distinct()


    private val schemaPropertyNames = tableEntityDefs.mapNotNull { it.configurableSchemaPropertyName }.distinct()


    init {

        require(schemaPropertyNames.size <= 1) {
            "EsDoc ${esDocDef.esDocBaseName} watchesVia sources span entities with different configurable schema properties $schemaPropertyNames; not supported"
        }

        addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS) { privat() }.build())

        schemaPropertyNames.singleOrNull()?.let { propertyName ->

            val classFieldDef = aClassField("schemaName", Fqcn.STRING) { privat() }.build()

            val annotationDefs = if (tableEntityDefs.all { it.daoHasSpringAnnotation.value }) {
                listOf(AnnotationDef(Fqcns.SPRING_VALUE_ANNOTATION, value = { "\"\\\${$propertyName}\"" }))
            } else {
                emptyList()
            }

            addConstructorArg(ConstructorArg(classFieldDef, annotationDefs))

        }

    }


    override fun renderFunctions() {

        reverseJoins.forEach { reverseJoin ->

            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            addImportFor(Fqcns.MAIA_SQL_PARAMS)

            blankLine()
            blankLine()
            appendLine("    fun ${reverseJoin.functionName}(id: DomainId): List<DomainId> {")
            blankLine()
            appendLine("        val sqlParams = SqlParams().addValue(\"id\", id)")
            blankLine()
            appendLine("        val sql = \"\"\"")

            reverseJoin.dependency.pathsFromRoot.forEachIndexed { index, path ->

                if (index > 0) {
                    appendLine("            union")
                }

                renderSelect(path, reverseJoin.primaryKeyField.tableColumnName.toString())

            }

            appendLine("            \"\"\".trimIndent()")
            blankLine()
            appendLine("        return this.jdbcOps.queryForList(sql, sqlParams) { rsa -> rsa.readDomainId(\"id\") }")
            blankLine()
            appendLine("    }")

        }

    }


    private fun renderSelect(path: List<JoinHop>, primaryKeyColumn: String) {

        val rootTable = rootEntityDef.schemaAndTableName

        appendLine("            select distinct $rootTable.id as id")
        appendLine("            from $rootTable")

        path.forEachIndexed { index, hop ->

            val source = if (index == 0) rootTable else alias(index - 1)

            appendLine("            inner join ${hop.toEntity.schemaAndTableName} ${alias(index)}")
            appendLine("                    on $source.${hop.fromField.tableColumnName} = ${alias(index)}.${hop.toField.tableColumnName}")

        }

        appendLine("            where ${alias(path.lastIndex)}.$primaryKeyColumn = :id")

        discriminatorPredicate(rootEntityDef, "$rootTable.type_discriminator")?.let { appendLine("            and $it") }

        path.forEachIndexed { index, hop ->
            if (hop.toEntity.superclassEntityDef != null) {
                discriminatorPredicate(hop.toEntity, "${alias(index)}.type_discriminator")?.let { appendLine("            and $it") }
            }
        }

    }


    private fun alias(hopIndex: Int) = "h$hopIndex"


    /** A subclass entity shares its superclass's table, so its rows are restricted by type discriminator. */
    private fun discriminatorPredicate(entityDef: EntityDef, column: String): String? {

        val discriminators: Collection<TypeDiscriminator> = applicationModelDef.entityHierarchyFor(entityDef).typeDiscriminators()

        return when {
            discriminators.size == 1 -> "$column = '${discriminators.first()}'"
            discriminators.size > 1 -> "$column in (${discriminators.joinToString(", ") { "'${it.value}'" }})"
            else -> null
        }

    }


}

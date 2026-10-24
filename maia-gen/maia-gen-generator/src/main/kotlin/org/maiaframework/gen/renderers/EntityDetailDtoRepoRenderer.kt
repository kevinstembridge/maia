package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ManyToManyRowMapperFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isValueFieldWrapper
import org.maiaframework.gen.spec.definition.lang.EntityRefFieldType

class EntityDetailDtoRepoRenderer(private val entityDetailViewDef: EntityDetailViewDef) : AbstractKotlinRenderer(entityDetailViewDef.repoClassDef) {


    private val manyToManyRowMapperFieldDefs = entityDetailViewDef.manyToManyFieldDefs
        .map { ManyToManyRowMapperFieldDef(it, entityDetailViewDef.entityDef) }


    init {

        addConstructorArg(aClassField("entityRepo", entityDetailViewDef.entityRepoClassDef.fqcn).privat().build())

        if (manyToManyRowMapperFieldDefs.isNotEmpty()) {
            addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS).privat().build())
        }

        entityDetailViewDef.dtoDef.allFields
            .filter { it.fieldType is EntityRefFieldType }
            .groupBy { it.fieldType.fqcn }
            .mapValues { entry -> entry.value.first() }
            .forEach {

                val classFieldDef = it.value
                val entityRefDef = (classFieldDef.fieldType as EntityRefFieldType).entityRefDef
                val entityRepoClassDef = entityRefDef.entityRepoClassDef

                addConstructorArg(
                    aClassField(
                        entityRepoClassDef.uqcn.firstToLower(),
                        entityRepoClassDef.fqcn
                    ).privat().build()
                )

            }

    }


    override fun renderPreClassFields() {

        manyToManyRowMapperFieldDefs.forEach { rowMapperFieldDef ->

            val entityRefDef = rowMapperFieldDef.entityRefDef

            addImportFor(entityRefDef.rowMapperDef.classDef.fqcn)

            append("""
                |
                |
                |    private val ${rowMapperFieldDef.classFieldName}RefDtoRowMapper = ${entityRefDef.rowMapperDef.classDef.uqcn}()
                |""".trimMargin())

        }

    }


    override fun renderFunctions() {

        `render function fetch`()
        `render functions for value mapping fields`()
        `render functions for EntityRef fields`()
        `render functions for manyToManyRefDtos`()

    }


    private fun `render function fetch`() {

        if (entityDetailViewDef.entityDef.hasCompositePrimaryKey) {

            val primaryKeyFqcn = entityDetailViewDef.entityDef.entityPkClassDef.fqcn
            val pkUqcn = primaryKeyFqcn.uqcn

            addImportFor(primaryKeyFqcn)

            append("""
            |
            |
            |    fun fetch(primaryKey: $pkUqcn): ${entityDetailViewDef.dtoDef.uqcn} {
            |
            |        val entity = this.entityRepo.findByPrimaryKey(primaryKey)
            |
            |        return ${entityDetailViewDef.dtoDef.uqcn}(
            |""".trimMargin()
            )

        } else {

            val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(entityDetailViewDef.entityDef.primaryKeyClassFields)
            val primaryKeyFieldNamesCsv = fieldNamesCsv(entityDetailViewDef.entityDef.primaryKeyClassFields)

            entityDetailViewDef.entityDef.primaryKeyFields.forEach { addImportFor(it.fieldType) }

            append("""
                |
                |
                |    fun fetch($primaryKeyFieldNamesAndTypesCsv): ${entityDetailViewDef.dtoDef.uqcn} {
                |
                |        val entity = this.entityRepo.findByPrimaryKey($primaryKeyFieldNamesCsv)
                |""".trimMargin()
            )

            if (manyToManyRowMapperFieldDefs.isNotEmpty()) {
                blankLine()
                manyToManyRowMapperFieldDefs.forEach { rowMapperFieldDef ->
                    appendLine("        val ${rowMapperFieldDef.classFieldName}RefDtoList = fetch${rowMapperFieldDef.classFieldName.firstToUpper()}RefDtos($primaryKeyFieldNamesCsv)")
                    blankLine()
                }
            }

            append("""
                |
                |        return ${entityDetailViewDef.dtoDef.uqcn}(
                |""".trimMargin()
            )

        }

        entityDetailViewDef.dtoDef.allFieldsSorted.forEach { classFieldDef ->

            val classFieldName = classFieldDef.classFieldName
            val fieldType = classFieldDef.fieldType

            if (classFieldDef.isMasked) {

                if (classFieldDef.fieldType.isValueFieldWrapper()) {

                    addImportFor(classFieldDef.fieldType)
                    appendLine("            $classFieldName = ${classFieldDef.fqcn.uqcn}(\"MASKED\"),")

                } else {
                    appendLine("            $classFieldName = \"MASKED\",")
                }

            } else if (fieldType is EntityRefFieldType) {

                if (classFieldDef.nullable) {
                    appendLine("            $classFieldName = entity.$classFieldName?.let { ${fieldType.entityRefDef.dtoUqcn.firstToLower()}For(it) },")
                } else {
                    appendLine("            $classFieldName = ${fieldType.entityRefDef.dtoUqcn.firstToLower()}For(entity.$classFieldName),")
                }

            } else if (manyToManyRowMapperFieldDefs.any { it.classFieldName == classFieldName }) {

                appendLine("            $classFieldName = ${classFieldName}RefDtoList,")

            } else if (classFieldDef.valueMappings != null) {

                appendLine("            $classFieldName = ${classFieldName}(entity.$classFieldName),")

            } else {

                appendLine("            $classFieldName = entity.$classFieldName,")

            }

        }

        append("""
            |        )
            |
            |    }
            |""".trimMargin()
        )

    }


    private fun `render functions for value mapping fields`() {

        entityDetailViewDef.dtoDef.allFieldsSorted
            .filter { it.valueMappings != null }
            .forEach { classFieldDef ->

                append("""
                    |
                    |
                    |    private fun ${classFieldDef.classFieldName}(input: ${classFieldDef.fqcn.uqcn}): String {
                    |
                    |        return when (input) {
                    |""".trimMargin()
                )

                classFieldDef.valueMappings!!.forEach { (key, value) ->
                    appendLine("""            "$key" -> "$value"""")
                }

                append("""
                    |            else -> input
                    |        }
                    |
                    |    }
                    |
                    |""".trimMargin()
                )

            }

    }


    private fun `render functions for EntityRef fields`() {

        entityDetailViewDef.dtoDef.allFields
            .filter { it.fieldType is EntityRefFieldType }
            .groupBy { it.fieldType.fqcn }
            .mapValues { entry -> entry.value.first() }
            .forEach {

                val classFieldDef = it.value
                val entityRefDef = (classFieldDef.fieldType as EntityRefFieldType).entityRefDef
                val entityRepoClassDef = entityRefDef.entityRepoClassDef

                addImportFor(entityRefDef.refDtoFqcn)

                append("""
                    |
                    |
                    |    private fun ${entityRefDef.dtoUqcn.firstToLower()}For(id: DomainId): ${entityRefDef.dtoUqcn} {
                    |
                    |        return this.${entityRepoClassDef.uqcn.firstToLower()}.entityRefFor(id)
                    |
                    |    }
                    |""".trimMargin()
                )

            }

    }


    private fun `render functions for manyToManyRefDtos`() {

        if (manyToManyRowMapperFieldDefs.isEmpty()) return

        val tripleQuote = "\"\"\""

        manyToManyRowMapperFieldDefs.forEach { rowMapperFieldDef ->

            val classFieldName = rowMapperFieldDef.classFieldName
            val manyToManyEntityDef = rowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef
            val otherSideEntity = rowMapperFieldDef.otherSideEntity
            val otherSideIdTableColumnName = rowMapperFieldDef.otherSideIdTableColumnName
            val thisSideIdTableColumnName = rowMapperFieldDef.thisSideIdTableColumnName
            val entityRefDef = rowMapperFieldDef.entityRefDef

            val isSystemManagedJoin = manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
                && manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP
            val effectiveRangeClause = if (isSystemManagedJoin) "\n            and mtm.effective_range @> current_timestamp" else ""

            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            addImportFor(Fqcns.MAIA_SQL_PARAMS)
            addImportFor(entityRefDef.refDtoFqcn)

            append("""
                |
                |
                |    private fun fetch${classFieldName.firstToUpper()}RefDtos(entityId: DomainId): List<${entityRefDef.refDtoFqcn.uqcn}> {
                |
                |        return this.jdbcOps.queryForList(
                |            $tripleQuote
                |            select
                |                other.id,
                |                other.${otherSideEntity.entityDef.entityRefDef.nameEntityFieldDef.tableColumnName}
                |            from ${otherSideEntity.entityDef.schemaAndTableName} other
                |            join ${manyToManyEntityDef.entityDef.schemaAndTableName} mtm
                |                on other.id = mtm.${otherSideIdTableColumnName}
                |            where mtm.${thisSideIdTableColumnName} = :entityId${effectiveRangeClause}
                |            order by other.${otherSideEntity.entityDef.entityRefDef.nameEntityFieldDef.tableColumnName}
                |            $tripleQuote.trimIndent(),
                |            SqlParams().apply {
                |                addValue("entityId", entityId)
                |            },
                |            this.${classFieldName}RefDtoRowMapper
                |        )
                |
                |    }
                |""".trimMargin())

        }

    }


}

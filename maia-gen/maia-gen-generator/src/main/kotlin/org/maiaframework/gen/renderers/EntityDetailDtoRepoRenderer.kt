package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.ManyToManyRowMapperFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isValueFieldWrapper
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType

class EntityDetailDtoRepoRenderer(private val entityDetailViewDef: EntityDetailViewDef) : AbstractKotlinRenderer(entityDetailViewDef.repoClassDef) {


    private val manyToManyRowMapperFieldDefs = entityDetailViewDef.manyToManyFieldDefs
        .map { ManyToManyRowMapperFieldDef(it, entityDetailViewDef.entityDef) }


    init {

        addConstructorArg(aClassField("entityRepo", entityDetailViewDef.entityRepoClassDef.fqcn).privat().build())

        if (manyToManyRowMapperFieldDefs.isNotEmpty()) {
            addConstructorArg(aClassField("jdbcOps", Fqcns.MAIA_JDBC_OPS).privat().build())
        }

        entityDetailViewDef.dtoDef.allFields
            .filter { it.fieldType is PkAndNameFieldType }
            .groupBy { it.fieldType.fqcn }
            .mapValues { entry -> entry.value.first() }
            .forEach {

                val classFieldDef = it.value
                val pkAndNameDef = (classFieldDef.fieldType as PkAndNameFieldType).pkAndNameDef
                val entityRepoClassDef = pkAndNameDef.entityRepoClassDef

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

            val entityPkAndNameDef = rowMapperFieldDef.entityPkAndNameDef

            addImportFor(entityPkAndNameDef.rowMapperDef.classDef.fqcn)

            append("""
                |
                |
                |    private val ${rowMapperFieldDef.classFieldName}PkAndNameDtoRowMapper = ${entityPkAndNameDef.rowMapperDef.classDef.uqcn}()
                |""".trimMargin())

        }

    }


    override fun renderFunctions() {

        `render function fetch`()
        `render functions for value mapping fields`()
        `render functions for PkAndName fields`()
        `render functions for manyToManyPkAndNameDtos`()

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
                    appendLine("        val ${rowMapperFieldDef.classFieldName}PkAndNameDtoList = fetch${rowMapperFieldDef.classFieldName.firstToUpper()}PkAndNameDtos($primaryKeyFieldNamesCsv)")
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

            } else if (fieldType is PkAndNameFieldType) {

                if (classFieldDef.nullable) {
                    appendLine("            $classFieldName = entity.$classFieldName?.let { ${fieldType.pkAndNameDef.dtoUqcn.firstToLower()}For(it) },")
                } else {
                    appendLine("            $classFieldName = ${fieldType.pkAndNameDef.dtoUqcn.firstToLower()}For(entity.$classFieldName),")
                }

            } else if (manyToManyRowMapperFieldDefs.any { it.classFieldName == classFieldName }) {

                appendLine("            $classFieldName = ${classFieldName}PkAndNameDtoList,")

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


    private fun `render functions for PkAndName fields`() {

        entityDetailViewDef.dtoDef.allFields
            .filter { it.fieldType is PkAndNameFieldType }
            .groupBy { it.fieldType.fqcn }
            .mapValues { entry -> entry.value.first() }
            .forEach {

                val classFieldDef = it.value
                val pkAndNameDef = (classFieldDef.fieldType as PkAndNameFieldType).pkAndNameDef
                val entityRepoClassDef = pkAndNameDef.entityRepoClassDef

                addImportFor(pkAndNameDef.pkAndNameDtoFqcn)

                append("""
                    |
                    |
                    |    private fun ${pkAndNameDef.dtoUqcn.firstToLower()}For(id: DomainId): ${pkAndNameDef.dtoUqcn} {
                    |
                    |        return this.${entityRepoClassDef.uqcn.firstToLower()}.pkAndNameFor(id)
                    |
                    |    }
                    |""".trimMargin()
                )

            }

    }


    private fun `render functions for manyToManyPkAndNameDtos`() {

        if (manyToManyRowMapperFieldDefs.isEmpty()) return

        val tripleQuote = "\"\"\""

        manyToManyRowMapperFieldDefs.forEach { rowMapperFieldDef ->

            val classFieldName = rowMapperFieldDef.classFieldName
            val manyToManyEntityDef = rowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef
            val otherSideEntity = rowMapperFieldDef.otherSideEntity
            val otherSideIdTableColumnName = rowMapperFieldDef.otherSideIdTableColumnName
            val thisSideIdTableColumnName = rowMapperFieldDef.thisSideIdTableColumnName
            val entityPkAndNameDef = rowMapperFieldDef.entityPkAndNameDef

            val isSystemManagedJoin = manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
                && manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP
            val effectiveRangeClause = if (isSystemManagedJoin) "\n            and mtm.effective_range @> current_timestamp" else ""

            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            addImportFor(Fqcns.MAIA_SQL_PARAMS)
            addImportFor(entityPkAndNameDef.pkAndNameDtoFqcn)

            append("""
                |
                |
                |    private fun fetch${classFieldName.firstToUpper()}PkAndNameDtos(entityId: DomainId): List<${entityPkAndNameDef.pkAndNameDtoFqcn.uqcn}> {
                |
                |        return this.jdbcOps.queryForList(
                |            $tripleQuote
                |            select
                |                other.id,
                |                other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                |            from ${otherSideEntity.entityDef.schemaAndTableName} other
                |            join ${manyToManyEntityDef.entityDef.schemaAndTableName} mtm
                |                on other.id = mtm.${otherSideIdTableColumnName}
                |            where mtm.${thisSideIdTableColumnName} = :entityId${effectiveRangeClause}
                |            order by other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                |            $tripleQuote.trimIndent(),
                |            SqlParams().apply {
                |                addValue("entityId", entityId)
                |            },
                |            this.${classFieldName}PkAndNameDtoRowMapper
                |        )
                |
                |    }
                |""".trimMargin())

        }

    }


}

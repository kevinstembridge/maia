package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isValueFieldWrapper
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType

class EntityDetailDtoRepoRenderer(private val entityDetailViewDef: EntityDetailViewDef) : AbstractKotlinRenderer(entityDetailViewDef.repoClassDef) {


    init {

        addConstructorArg(aClassField("entityRepo", entityDetailViewDef.entityRepoClassDef.fqcn).privat().build())

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


    override fun renderFunctions() {

        `render function fetch`()
        `render functions for value mapping fields`()
        `render functions for PkAndName fields`()

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

                appendLine("            $classFieldName = ${fieldType.pkAndNameDef.dtoUqcn.firstToLower()}For(entity.$classFieldName),")

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
                    |    private fun ${pkAndNameDef.dtoUqcn.firstToLower()}For(id: DomainId): ${pkAndNameDef.dtoUqcn} {
                    |
                    |        return this.${entityRepoClassDef.uqcn.firstToLower()}.pkAndNameFor(id)
                    |
                    |    }
                    |""".trimMargin()
                )

            }

    }


}

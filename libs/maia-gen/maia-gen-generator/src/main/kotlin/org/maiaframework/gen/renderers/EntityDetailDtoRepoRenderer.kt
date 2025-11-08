package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDetailDtoDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes.isValueFieldWrapper
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType

class EntityDetailDtoRepoRenderer(private val entityDetailDtoDef: EntityDetailDtoDef) : AbstractKotlinRenderer(entityDetailDtoDef.repoClassDef) {


    init {

        addConstructorArg(aClassField("entityRepo", entityDetailDtoDef.entityRepoClassDef.fqcn).privat().build())

        entityDetailDtoDef.dtoDef.allFields
            .filter { it.fieldType is IdAndNameFieldType }
            .groupBy { it.fieldType.fqcn }
            .mapValues { entry -> entry.value.first() }
            .forEach {

                val classFieldDef = it.value
                val idAndNameDef = (classFieldDef.fieldType as IdAndNameFieldType).idAndNameDef
                val entityRepoClassDef = idAndNameDef.entityRepoClassDef

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
        `render functions for IdAndName fields`()

    }


    private fun `render function fetch`() {

        addImportFor(Fqcns.MAHANA_DOMAIN_ID)

        append("""
            |
            |
            |    fun fetch(entityId: DomainId): ${entityDetailDtoDef.dtoDef.uqcn} {
            |
            |        val entity = this.entityRepo.findById(entityId)
            |        
            |        return ${entityDetailDtoDef.dtoDef.uqcn}(
            |""".trimMargin()
        )


        entityDetailDtoDef.dtoDef.allFieldsSorted.forEach { classFieldDef ->

            val classFieldName = classFieldDef.classFieldName
            val fieldType = classFieldDef.fieldType

            if (classFieldDef.isMasked) {

                if (classFieldDef.fieldType.isValueFieldWrapper()) {

                    addImportFor(classFieldDef.fieldType)
                    appendLine("            $classFieldName = ${classFieldDef.fqcn.uqcn}(\"MASKED\"),")

                } else {
                    appendLine("            $classFieldName = \"MASKED\",")
                }

            } else if (fieldType is IdAndNameFieldType) {

                appendLine("            $classFieldName = ${fieldType.idAndNameDef.dtoUqcn.firstToLower()}For(entity.$classFieldName),")

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

        entityDetailDtoDef.dtoDef.allFieldsSorted
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
                    appendLine("""            "$key" -> "$value"""".trimMargin())
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


    private fun `render functions for IdAndName fields`() {

        entityDetailDtoDef.dtoDef.allFields
            .filter { it.fieldType is IdAndNameFieldType }
            .groupBy { it.fieldType.fqcn }
            .mapValues { entry -> entry.value.first() }
            .forEach {

                val classFieldDef = it.value
                val idAndNameDef = (classFieldDef.fieldType as IdAndNameFieldType).idAndNameDef
                val entityRepoClassDef = idAndNameDef.entityRepoClassDef

                addImportFor(idAndNameDef.idAndNameDtoFqcn)

                append("""
                    |
                    |    private fun ${idAndNameDef.dtoUqcn.firstToLower()}For(id: DomainId): ${idAndNameDef.dtoUqcn} {
                    |
                    |        return this.${entityRepoClassDef.uqcn.firstToLower()}.idAndNameFor(id)
                    |
                    |    }
                    |""".trimMargin()
                )

            }

    }


}

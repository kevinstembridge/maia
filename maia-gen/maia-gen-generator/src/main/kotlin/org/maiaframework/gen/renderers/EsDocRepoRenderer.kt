package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class EsDocRepoRenderer(private val esDocDef: EsDocDef) : AbstractKotlinRenderer(esDocDef.esDocRepoClassDef) {


    init {

        addConstructorArg(aClassField("entityRepo", this.esDocDef.entityDef!!.entityRepoFqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function findById`()
        `render function findAllAsSequence`()
        renderFunction_buildEsDoc()

    }


    private fun `render function findById`() {

        val entityDef = esDocDef.entityDef!!
        val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(entityDef.primaryKeyClassFields)
        val primaryKeyFieldNamesCsv = fieldNamesCsv(entityDef.primaryKeyClassFields)

        entityDef.primaryKeyFields.forEach { addImportFor(it.fieldType) }

        append("""
            |
            |
            |    fun findByPrimaryKey($primaryKeyFieldNamesAndTypesCsv): ${esDocDef.uqcn} {
            |
            |        val entity = this.entityRepo.findByPrimaryKey($primaryKeyFieldNamesCsv)
            |        return buildEsDoc(entity)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function findAllAsSequence`() {

        appendLine("""
            |
            |
            |    fun findAllAsSequence(): Sequence<${this.esDocDef.uqcn}> {
            |
            |        return this.entityRepo.findAllAsSequence().map { buildEsDoc(it) }
            |
            |    }
            """.trimMargin())

    }


    private fun renderFunction_buildEsDoc() {

        blankLine()
        blankLine()
        appendLine("    private fun buildEsDoc(entity: ${this.esDocDef.entityDef!!.entityUqcn}): ${this.esDocDef.uqcn} {")
        blankLine()

        this.esDocDef.fields.forEach { field ->

            appendLine("        val ${field.classFieldDef.classFieldName} = entity.${field.entityFieldDef!!.classFieldName}")

        }

        blankLine()
        appendLine("        return ${this.esDocDef.uqcn}(")

        this.esDocDef.fields.forEach { field ->
            appendLine("            ${field.classFieldDef.classFieldName},")
        }

        appendLine("        )")

        blankLine()
        appendLine("    }")

    }


}

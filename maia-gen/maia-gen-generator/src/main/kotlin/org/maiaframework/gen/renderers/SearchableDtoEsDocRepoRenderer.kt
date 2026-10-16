package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


/**
 * Renders the repo that builds EsDocs for a searchable DTO from the DTO's own (joined) repo, so that
 * the EsDoc always reflects what the DTO's inner joins and type discriminator select.
 */
class SearchableDtoEsDocRepoRenderer(
    private val searchableDtoDef: SearchableDtoDef,
    private val esDocDef: EsDocDef
) : AbstractKotlinRenderer(
    esDocDef.esDocRepoClassDef
) {


    init {

        addConstructorArg(aClassField("dtoRepo", this.searchableDtoDef.dtoRepoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function findByIds`()
        `render function findAllAsSequence`()
        `render function buildEsDoc`()

    }


    private fun `render function findByIds`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        append("""
            |
            |
            |    fun findByIds(ids: Collection<DomainId>): List<${this.esDocDef.uqcn}> {
            |
            |        return this.dtoRepo.findByIds(ids).map { buildEsDoc(it) }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function findAllAsSequence`() {

        append("""
            |
            |
            |    fun findAllAsSequence(): Sequence<${this.esDocDef.uqcn}> {
            |
            |        return this.dtoRepo.findAllAsSequence().map { buildEsDoc(it) }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function buildEsDoc`() {

        blankLine()
        blankLine()
        appendLine("    private fun buildEsDoc(dto: ${this.searchableDtoDef.uqcn}): ${this.esDocDef.uqcn} {")
        blankLine()
        appendLine("        return ${this.esDocDef.uqcn}(")

        this.esDocDef.fields.forEach { field ->
            val name = field.classFieldDef.classFieldName
            appendLine("            $name = dto.$name,")
        }

        appendLine("        )")
        blankLine()
        appendLine("    }")

    }


}

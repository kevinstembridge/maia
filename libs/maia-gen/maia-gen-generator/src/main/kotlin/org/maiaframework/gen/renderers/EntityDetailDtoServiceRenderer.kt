package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDetailDtoDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class EntityDetailDtoServiceRenderer(
    private val entityDetailDtoDef: EntityDetailDtoDef
) : AbstractKotlinRenderer(
    entityDetailDtoDef.serviceClassDef
) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("repo", entityDetailDtoDef.repoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function fetch`()

    }


    private fun `render function fetch`() {

        addImportFor(Fqcns.MAHANA_DOMAIN_ID)

        appendLine("""
            |
            |
            |    fun fetch(id: DomainId): ${this.entityDetailDtoDef.dtoDef.uqcn}? {
            |
            |        return repo.fetch(id)
            |
            |    }
        """.trimMargin())

    }


}

package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef

class EntityDetailDtoServiceRenderer(
    private val entityDetailViewDef: EntityDetailViewDef
) : AbstractKotlinRenderer(
    entityDetailViewDef.serviceClassDef
) {


    init {

        addConstructorArg(ClassFieldDef.aClassField("repo", entityDetailViewDef.repoClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function fetch`()

    }


    private fun `render function fetch`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        appendLine("""
            |
            |
            |    fun fetch(id: DomainId): ${this.entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        return repo.fetch(id)
            |
            |    }
        """.trimMargin())

    }


}

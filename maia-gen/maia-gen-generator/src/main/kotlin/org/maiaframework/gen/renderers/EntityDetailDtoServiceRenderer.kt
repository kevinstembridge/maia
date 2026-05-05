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

        if (entityDetailViewDef.entityDef.hasSurrogatePrimaryKey) {

            `render fetch for surrogate primary key`()

        } else if (entityDetailViewDef.entityDef.hasCompositePrimaryKey) {

            `render fetch for composite primary key`()

        } else {

            `render fetch for natural primary key`()

        }

    }


    private fun `render fetch for surrogate primary key`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        append(
            """
                |
                |
                |    fun fetch(id: DomainId): ${this.entityDetailViewDef.dtoDef.uqcn}? {
                |
                |        return repo.fetch(id)
                |
                |    }
                |""".trimMargin()
        )

    }


    private fun `render fetch for composite primary key`() {

        addImportFor(entityDetailViewDef.entityDef.entityPkClassDef.fqcn)

        append("""
            |
            |
            |    fun fetch(primaryKey: ${entityDetailViewDef.entityDef.entityPkClassDef.uqcn}): ${this.entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        return repo.fetch(primaryKey)
            |
            |    }
            |""".trimMargin()
        )

    }


    private fun `render fetch for natural primary key`() {

        val fqcn = entityDetailViewDef.entityDef.primaryKeyFields.first().classFieldDef.fqcn
        val pkUqcn = fqcn.uqcn

        addImportFor(fqcn)

        append("""
            |
            |
            |    fun fetch(primaryKey: ${pkUqcn}): ${this.entityDetailViewDef.dtoDef.uqcn}? {
            |
            |        return repo.fetch(primaryKey)
            |
            |    }
            |""".trimMargin()
        )

    }


}

package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField

class TypeaheadCrudListenerRenderer(
    private val typeaheadDef: TypeaheadDef
) : AbstractKotlinRenderer(
    typeaheadDef.crudListenerClassDef!!
) {


    private val entityCrudApiDef = typeaheadDef.entityCrudApiDef!!


    init {

        addConstructorArg(aClassField("indexService", this.typeaheadDef.indexServiceClassDef.fqcn).privat().build())

    }


    override fun renderFunctions() {

        `render function onCreate`()
        `render function onUpdate`()
        `render function onDelete`()

    }


    private fun `render function onCreate`() {

        this.entityCrudApiDef.createApiDef?.let { apiDef ->

            val contextDtoDef = apiDef.crudApiDef.context
            val entityUqcn = this.entityCrudApiDef.entityDef.entityUqcn

            if (contextDtoDef != null) {
                addImportFor(contextDtoDef.fqcn)
                appendLine("""
                    |
                    |
                    |    override fun on${entityUqcn}Created(entity: $entityUqcn, context: ${contextDtoDef.uqcn}) {
                    |
                    |        this.indexService.refreshById(entity.id, context)
                    |
                    |    }
                    |""".trimMargin())
            } else {
                appendLine("""
                    |
                    |
                    |    override fun on${entityUqcn}Created(entity: $entityUqcn) {
                    |
                    |        this.indexService.refreshById(entity.id)
                    |
                    |    }
                    |""".trimMargin())
            }

        }

    }


    private fun `render function onUpdate`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        this.entityCrudApiDef.updateApiDef?.let {
            appendLine("""
                |
                |    override fun on${this.entityCrudApiDef.entityDef.entityUqcn}Updated(id: DomainId) {
                |
                |        this.indexService.refreshById(id)
                |
                |    }
                |""".trimMargin())

        }

    }


    private fun `render function onDelete`() {

        this.entityCrudApiDef.deleteApiDef?.let {
            appendLine("""
                |
                |    override fun on${this.entityCrudApiDef.entityDef.entityUqcn}Deleted(entity: ${this.entityCrudApiDef.entityDef.entityUqcn}) {
                |
                |        this.indexService.deleteById(entity.id)
                |
                |    }
                |""".trimMargin())
        }

    }


}

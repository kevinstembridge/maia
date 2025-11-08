package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.Fqcns

class CrudListenerRenderer(
    private val entityCrudApiDef: EntityCrudApiDef
) : AbstractKotlinRenderer(
    entityCrudApiDef.entityDef.crudListenerClassDef
) {


    override fun renderFunctions() {

        renderFunction_onCreate()
        renderFunction_onUpdate()
        renderFunction_onDelete()

    }


    private fun renderFunction_onCreate() {

        this.entityCrudApiDef.createApiDef?.let { apiDef ->

            val contextDtoDef = apiDef.crudApiDef.context
            val entityUqcn = this.entityCrudApiDef.entityDef.entityUqcn

            blankLine()
            blankLine()

            if (contextDtoDef != null) {
                addImportFor(contextDtoDef.fqcn)
                appendLine("    fun on${entityUqcn}Created(entity: $entityUqcn, context: ${contextDtoDef.uqcn})")
            } else {
                appendLine("    fun on${entityUqcn}Created(entity: $entityUqcn)")
            }

        }

    }


    private fun renderFunction_onUpdate() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)

        if (this.entityCrudApiDef.updateApiDef != null || this.entityCrudApiDef.entityDef.hasModifiableFields()) {
            blankLine()
            blankLine()
            appendLine("    fun on${this.entityCrudApiDef.entityDef.entityUqcn}Updated(id: DomainId)")
        }

    }


    private fun renderFunction_onDelete() {

        this.entityCrudApiDef.deleteApiDef?.let {
            blankLine()
            blankLine()
            appendLine("    fun on${this.entityCrudApiDef.entityDef.entityUqcn}Deleted(entity: ${this.entityCrudApiDef.entityDef.entityUqcn})")
        }

    }


}

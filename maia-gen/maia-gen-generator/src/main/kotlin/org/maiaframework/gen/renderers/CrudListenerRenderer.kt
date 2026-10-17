package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef


/**
 * Which callbacks an entity's CrudListener interface declares. Shared by every renderer that renders or
 * implements that interface, so that implementations can never diverge from the interface.
 */
internal object CrudListenerCallbacks {


    /**
     * True when the entity declares a create operation, with or without an API: the generated CrudService's
     * `create(entity)` notifies listeners either way.
     */
    fun hasCreateCallback(entityDef: EntityDef): Boolean {

        return entityDef.entityCrudApiDef?.createApiDef != null || entityDef.crudDef.crudOperationDefs.createOperationDef != null

    }


    fun hasUpdateCallback(entityDef: EntityDef): Boolean {

        return entityDef.entityCrudApiDef?.updateApiDef != null || entityDef.hasModifiableFields()

    }


    /** Covers both `on<E>BeforeDelete` (default no-op) and `on<E>Deleted`. */
    fun hasDeleteCallback(entityDef: EntityDef): Boolean {

        return entityDef.entityCrudApiDef?.deleteApiDef != null

    }


}


class CrudListenerRenderer(
    private val entityDef: EntityDef
) : AbstractKotlinRenderer(
    entityDef.crudListenerClassDef
) {


    override fun renderFunctions() {

        `render function onCreate`()
        `render function onEntityUpdated`()
        `render function onDelete`()

    }


    private fun `render function onCreate`() {

        if (CrudListenerCallbacks.hasCreateCallback(this.entityDef)) {

            val entityUqcn = this.entityDef.entityUqcn

            blankLine()
            blankLine()
            appendLine("    fun on${entityUqcn}Created(entity: $entityUqcn)")

        }

    }


    private fun `render function onEntityUpdated`() {

        val entityUqcn = this.entityDef.entityUqcn

        if (CrudListenerCallbacks.hasUpdateCallback(this.entityDef)) {
            blankLine()
            blankLine()
            appendLine("    fun on${entityUqcn}Updated(before: $entityUqcn, after: $entityUqcn)")
        }

    }


    private fun `render function onDelete`() {

        if (CrudListenerCallbacks.hasDeleteCallback(this.entityDef)) {

            val entityUqcn = this.entityDef.entityUqcn

            blankLine()
            blankLine()
            appendLine("    fun on${entityUqcn}BeforeDelete(entity: $entityUqcn) {}")
            blankLine()
            blankLine()
            appendLine("    fun on${entityUqcn}Deleted(entity: $entityUqcn)")

        }

    }


}

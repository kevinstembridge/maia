package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef

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

        this.entityDef.entityCrudApiDef?.createApiDef?.let { _ ->

            val entityUqcn = this.entityDef.entityUqcn

            blankLine()
            blankLine()
            appendLine("    fun on${entityUqcn}Created(entity: $entityUqcn)")

        }

    }


    private fun `render function onEntityUpdated`() {

        val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(this.entityDef.primaryKeyClassFields)

        this.entityDef.primaryKeyFields.forEach { addImportFor(it.fieldType) }

        if (this.entityDef.entityCrudApiDef?.updateApiDef != null || this.entityDef.hasModifiableFields()) {
            blankLine()
            blankLine()
            appendLine("    fun on${this.entityDef.entityUqcn}Updated($primaryKeyFieldNamesAndTypesCsv)")
        }

    }


    private fun `render function onDelete`() {

        this.entityDef.entityCrudApiDef?.deleteApiDef?.let {
            blankLine()
            blankLine()
            appendLine("    fun on${this.entityDef.entityUqcn}Deleted(entity: ${this.entityDef.entityUqcn})")
        }

    }


}

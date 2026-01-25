package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityCrudApiDef

class CrudListenerRenderer(
    private val entityCrudApiDef: EntityCrudApiDef
) : AbstractKotlinRenderer(
    entityCrudApiDef.entityDef.crudListenerClassDef
) {


    override fun renderFunctions() {

        `render function onCreate`()
        `render function onEntityUpdated`()
        `render function onDelete`()

    }


    private fun `render function onCreate`() {

        this.entityCrudApiDef.createApiDef?.let { _ ->

            val entityUqcn = this.entityCrudApiDef.entityDef.entityUqcn

            blankLine()
            blankLine()
            appendLine("    fun on${entityUqcn}Created(entity: $entityUqcn)")

        }

    }


    private fun `render function onEntityUpdated`() {

        val primaryKeyFieldNamesAndTypesCsv = fieldNamesAndTypesCsv(this.entityCrudApiDef.entityDef.primaryKeyClassFields)

        this.entityCrudApiDef.entityDef.primaryKeyFields.forEach { addImportFor(it.fieldType) }

        if (this.entityCrudApiDef.updateApiDef != null || this.entityCrudApiDef.entityDef.hasModifiableFields()) {
            blankLine()
            blankLine()
            appendLine("    fun on${this.entityCrudApiDef.entityDef.entityUqcn}Updated($primaryKeyFieldNamesAndTypesCsv)")
        }

    }


    private fun `render function onDelete`() {

        this.entityCrudApiDef.deleteApiDef?.let {
            blankLine()
            blankLine()
            appendLine("    fun on${this.entityCrudApiDef.entityDef.entityUqcn}Deleted(entity: ${this.entityCrudApiDef.entityDef.entityUqcn})")
        }

    }


}

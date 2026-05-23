package org.maiaframework.gen.spec.definition


class CrudOperationDefs(
    val createOperationDef: CrudCreateOperationDef?,
    val updateOperationDef: CrudUpdateOperationDef?,
    val deleteOperationDef: CrudDeleteOperationDef?,
    val superclassCrudApiDef: EntityCrudApiDef?,
    val customCrudServiceFqcn: CustomCrudServiceFqcn?
) {


    companion object {

        val EMPTY = CrudOperationDefs(
            null,
            null,
            null,
            superclassCrudApiDef = null,
            customCrudServiceFqcn = null
        )

    }


}

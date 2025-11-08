package org.maiaframework.gen.spec.definition

class CrudApiDefs(
    val createApiDef: CrudApiDef?,
    val updateApiDef: CrudApiDef?,
    val deleteApiDef: CrudApiDef?,
    val superclassCrudApiDef: EntityCrudApiDef?
) {

    companion object {

        val EMPTY = CrudApiDefs(null, null, null, superclassCrudApiDef = null)

    }

}

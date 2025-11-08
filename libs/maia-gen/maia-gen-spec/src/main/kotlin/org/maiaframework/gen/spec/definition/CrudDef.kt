package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.WithCrudListener

data class CrudDef(
    val withCrudListener: WithCrudListener,
    val crudApiDefs: CrudApiDefs
) {


    companion object {

        val EMPTY = CrudDef(WithCrudListener.FALSE, CrudApiDefs.EMPTY)

    }


}

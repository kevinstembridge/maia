package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.WithCrudListener

data class CrudDef(
    val withCrudListener: WithCrudListener,
    val crudOperationDefs: CrudOperationDefs
) {


    companion object {


        val EMPTY = CrudDef(
            WithCrudListener.FALSE,
            CrudOperationDefs.EMPTY
        )


    }


}

package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsEditableByUser(value: Boolean) : BooleanType<IsEditableByUser>(value) {


    companion object {

        val TRUE = IsEditableByUser(true)

        val FALSE = IsEditableByUser(false)

    }


}

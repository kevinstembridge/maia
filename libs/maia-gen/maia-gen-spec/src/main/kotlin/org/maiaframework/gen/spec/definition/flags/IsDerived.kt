package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsDerived(value: Boolean) : BooleanType<IsDerived>(value) {


    companion object {

        val TRUE = IsDerived(true)

        val FALSE = IsDerived(false)

    }


}

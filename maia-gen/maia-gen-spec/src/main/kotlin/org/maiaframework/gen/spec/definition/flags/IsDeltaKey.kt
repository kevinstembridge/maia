package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsDeltaKey(value: Boolean) : BooleanType<IsDeltaKey>(value) {


    companion object {

        val TRUE = IsDeltaKey(true)

        val FALSE = IsDeltaKey(false)

    }


}

package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsDeltaField(value: Boolean) : BooleanType<IsDeltaField>(value) {


    companion object {

        val TRUE = IsDeltaField(true)

        val FALSE = IsDeltaField(false)

    }


}

package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsDeltaEntity(value: Boolean) : BooleanType<IsDeltaEntity>(value) {


    companion object {

        val TRUE = IsDeltaEntity(true)

        val FALSE = IsDeltaEntity(false)

    }


}

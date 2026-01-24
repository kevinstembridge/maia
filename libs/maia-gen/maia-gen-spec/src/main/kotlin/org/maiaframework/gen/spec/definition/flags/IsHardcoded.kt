package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsHardcoded(value: Boolean) : BooleanType<IsHardcoded>(value) {


    companion object {

        val TRUE = IsHardcoded(true)

        val FALSE = IsHardcoded(false)

    }


}

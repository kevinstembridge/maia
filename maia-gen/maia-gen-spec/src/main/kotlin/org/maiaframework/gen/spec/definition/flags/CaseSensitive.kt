package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class CaseSensitive(value: Boolean) : BooleanType<CaseSensitive>(value) {


    companion object {

        val TRUE = CaseSensitive(true)

        val FALSE = CaseSensitive(false)

    }


}

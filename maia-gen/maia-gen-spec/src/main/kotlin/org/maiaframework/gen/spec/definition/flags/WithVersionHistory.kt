package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class WithVersionHistory(value: Boolean) : BooleanType<WithVersionHistory>(value) {


    companion object {

        val TRUE = WithVersionHistory(true)

        val FALSE = WithVersionHistory(false)

    }


}

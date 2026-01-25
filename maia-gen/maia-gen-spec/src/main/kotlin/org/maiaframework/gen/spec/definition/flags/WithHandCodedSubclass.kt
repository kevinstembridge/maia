package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class WithHandCodedSubclass private constructor(value: Boolean) : BooleanType<WithHandCodedSubclass>(value) {


    companion object {

        val TRUE = WithHandCodedSubclass(true)

        val FALSE = WithHandCodedSubclass(false)

    }


}

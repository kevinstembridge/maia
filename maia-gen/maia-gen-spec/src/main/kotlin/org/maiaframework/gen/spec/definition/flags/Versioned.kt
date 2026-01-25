package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class Versioned(value: Boolean) : BooleanType<Versioned>(value) {


    companion object {

        val TRUE = Versioned(true)

        val FALSE = Versioned(false)

    }


}

package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class Deletable private constructor(value: Boolean) : BooleanType<Deletable>(value) {


    companion object {

        val TRUE = Deletable(true)

        val FALSE = Deletable(false)

    }


}

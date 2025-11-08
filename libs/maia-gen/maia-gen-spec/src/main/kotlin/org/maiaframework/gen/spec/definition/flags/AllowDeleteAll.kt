package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class AllowDeleteAll private constructor(value: Boolean) : BooleanType<AllowDeleteAll>(value) {


    companion object {

        val TRUE = AllowDeleteAll(true)

        val FALSE = AllowDeleteAll(false)

    }


}

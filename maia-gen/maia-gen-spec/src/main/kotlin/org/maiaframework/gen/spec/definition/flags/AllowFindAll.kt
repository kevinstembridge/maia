package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class AllowFindAll private constructor(value: Boolean) : BooleanType<AllowFindAll>(value) {


    companion object {

        val TRUE = AllowFindAll(true)

        val FALSE = AllowFindAll(false)

    }


}

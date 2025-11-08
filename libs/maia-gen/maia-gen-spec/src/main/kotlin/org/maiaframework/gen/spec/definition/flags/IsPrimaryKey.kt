package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsPrimaryKey(value: Boolean) : BooleanType<IsPrimaryKey>(value) {


    companion object {

        val TRUE = IsPrimaryKey(true)

        val FALSE = IsPrimaryKey(false)

    }


}

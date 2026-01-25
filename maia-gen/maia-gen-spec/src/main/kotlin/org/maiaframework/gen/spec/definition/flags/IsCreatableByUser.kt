package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsCreatableByUser(value: Boolean) : BooleanType<IsCreatableByUser>(value) {


    companion object {

        val TRUE = IsCreatableByUser(true)

        val FALSE = IsCreatableByUser(false)

    }


}

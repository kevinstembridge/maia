package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class IsCappedCollection(value: Boolean) : BooleanType<IsCappedCollection>(value) {


    companion object {

        val TRUE = IsCappedCollection(true)

        val FALSE = IsCappedCollection(false)

    }


}

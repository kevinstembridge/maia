package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class EmitEventsOnSuccess(value: Boolean) : BooleanType<EmitEventsOnSuccess>(value) {


    companion object {

        val TRUE = EmitEventsOnSuccess(true)

        val FALSE = EmitEventsOnSuccess(false)

    }


}

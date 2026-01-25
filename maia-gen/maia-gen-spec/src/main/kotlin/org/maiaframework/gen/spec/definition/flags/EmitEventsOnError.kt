package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class EmitEventsOnError(value: Boolean) : BooleanType<EmitEventsOnError>(value) {


    companion object {

        val TRUE = EmitEventsOnError(true)

        val FALSE = EmitEventsOnError(false)

    }


}

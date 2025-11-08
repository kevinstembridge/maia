package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class WithProvidedFieldConverter private constructor(value: Boolean) : BooleanType<WithProvidedFieldConverter>(value) {


    companion object {

        val TRUE = WithProvidedFieldConverter(true)

        val FALSE = WithProvidedFieldConverter(false)

    }


}

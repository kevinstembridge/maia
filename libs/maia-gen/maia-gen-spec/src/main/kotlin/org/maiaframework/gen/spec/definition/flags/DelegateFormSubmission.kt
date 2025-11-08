package org.maiaframework.gen.spec.definition.flags

import org.maiaframework.types.BooleanType

class DelegateFormSubmission(value: Boolean) : BooleanType<DelegateFormSubmission>(value) {


    companion object {

        val TRUE = DelegateFormSubmission(true)

        val FALSE = DelegateFormSubmission(false)

    }


}

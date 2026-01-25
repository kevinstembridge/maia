package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class WithoutImplicitCreatedByIdField(val value: Boolean) {


    companion object {

        val TRUE = WithoutImplicitCreatedByIdField(true)

        val FALSE = WithoutImplicitCreatedByIdField(false)

    }


}

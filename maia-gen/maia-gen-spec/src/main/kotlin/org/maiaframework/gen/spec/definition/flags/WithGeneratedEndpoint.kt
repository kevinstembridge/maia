package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class WithGeneratedEndpoint(val value: Boolean) {

    companion object {

        val TRUE = WithGeneratedEndpoint(true)

        val FALSE = WithGeneratedEndpoint(false)

    }

}

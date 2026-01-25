package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class WithGeneratedDto(val value: Boolean) {

    companion object {

        val TRUE = WithGeneratedDto(true)

        val FALSE = WithGeneratedDto(false)

    }

}

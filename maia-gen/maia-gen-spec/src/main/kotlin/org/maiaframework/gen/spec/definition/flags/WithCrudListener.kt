package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class WithCrudListener(val value: Boolean) {

    companion object {

        val TRUE = WithCrudListener(true)

        val FALSE = WithCrudListener(false)

    }

}

package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class HasSingleEffectiveRecord(val value: Boolean) {


    companion object {

        val TRUE = HasSingleEffectiveRecord(true)

        val FALSE = HasSingleEffectiveRecord(false)

    }


}

package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class HasEffectiveTimestamps(val value: Boolean) {


    companion object {

        val TRUE = HasEffectiveTimestamps(true)

        val FALSE = HasEffectiveTimestamps(false)

    }


}

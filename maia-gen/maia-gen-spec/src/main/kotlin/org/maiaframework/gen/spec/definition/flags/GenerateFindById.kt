package org.maiaframework.gen.spec.definition.flags

@JvmInline
value class GenerateFindById(val value: Boolean) {


    fun isFalse(): Boolean = value == false


    companion object {

        val TRUE = GenerateFindById(true)

        val FALSE = GenerateFindById(false)

    }

}

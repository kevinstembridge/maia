package org.maiaframework.gen.spec.definition.flags

data class IsPrimaryKey(val value: Boolean, val isSurrogate: Boolean) {


    companion object {

        val SURROGATE = IsPrimaryKey(value = true, isSurrogate = true)

        val FALSE = IsPrimaryKey(value = false, isSurrogate = false)

    }


}

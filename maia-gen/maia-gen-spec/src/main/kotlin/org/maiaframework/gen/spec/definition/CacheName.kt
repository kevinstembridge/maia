package org.maiaframework.gen.spec.definition

@JvmInline
value class CacheName(val value: String) {


    override fun toString(): String {
        return value
    }


    fun withSuffix(suffix: String): CacheName {

        return CacheName("$value$suffix")

    }


}

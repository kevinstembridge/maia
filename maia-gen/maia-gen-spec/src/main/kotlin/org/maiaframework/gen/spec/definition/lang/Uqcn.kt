package org.maiaframework.gen.spec.definition.lang


import org.maiaframework.common.BlankStringException
import org.maiaframework.types.StringType

class Uqcn(value: String) : StringType<Uqcn>(value) {


    fun withPrefix(prefix: String): Uqcn {

        BlankStringException.throwIfBlank(prefix, "prefix")
        return Uqcn(prefix + value)

    }


    fun withSuffix(suffix: String): Uqcn {

        BlankStringException.throwIfBlank(suffix, "suffix")
        return Uqcn(value + suffix)

    }


}

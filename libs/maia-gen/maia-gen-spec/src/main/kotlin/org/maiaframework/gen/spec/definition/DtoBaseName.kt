package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType


class DtoBaseName(value: String) : StringType<DtoBaseName>(value) {


    fun withSuffix(suffix: String): DtoBaseName {

        return DtoBaseName("$value$suffix")

    }


    fun withSuffix(suffix: DtoSuffix): DtoBaseName {

        return withSuffix(suffix.value)

    }


}

package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType


class AngularComponentBaseName(value: String) : StringType<AngularComponentBaseName>(value) {


    fun withSuffix(suffix: String): AngularComponentBaseName {

        return AngularComponentBaseName("$value$suffix")

    }


    fun withSuffix(suffix: DtoSuffix): AngularComponentBaseName {

        return withSuffix(suffix.value)

    }


}

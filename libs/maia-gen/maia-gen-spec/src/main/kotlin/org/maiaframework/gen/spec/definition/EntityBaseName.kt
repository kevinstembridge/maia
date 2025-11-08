package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType


class EntityBaseName(value: String) : StringType<EntityBaseName>(value) {


    fun withSuffix(suffix: String): EntityBaseName {

        return EntityBaseName("${this.value}$suffix")

    }


}

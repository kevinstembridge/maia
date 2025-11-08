package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType


class EntityName(value: String) : StringType<EntityName>(value) {

    fun withSuffix(suffix: String): EntityName {

        return EntityName("${this.value}$suffix")

    }


}

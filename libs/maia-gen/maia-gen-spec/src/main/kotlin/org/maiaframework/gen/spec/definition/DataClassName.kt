package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType


class DataClassName(value: String) : StringType<DataClassName>(value) {


    fun withSuffix(suffix: String): DataClassName {

        return DataClassName("${this.value}$suffix")

    }


}

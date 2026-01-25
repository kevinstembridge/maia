package org.maiaframework.gen.spec.definition

import org.maiaframework.types.StringType


class IndexName(value: String) : StringType<IndexName>(value) {


    fun replaceUidxSuffix(): IndexName {

        return IndexName(this.value.replace("uidx", "idx"))

    }


}

package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.jdbc.PostgresIdentifiers
import org.maiaframework.types.StringType


class IndexName(value: String) : StringType<IndexName>(value) {


    init {
        PostgresIdentifiers.requireValidLength(
            this.value,
            "index name",
            "Provide a shorter name explicitly via index { indexName(\"...\") }."
        )
    }


    fun replaceUidxSuffix(): IndexName {

        return IndexName(this.value.replace("uidx", "idx"))

    }


}

package org.maiaframework.googlecharts


class ColumnDef(val id: String?, val label: String?, val type: String, val role: String? = null) {


    init {

        require(type.isNotBlank()) { "type must not be blank" }

        id?.let { value -> require(value.isNotBlank()) { "id must no be blank" } }
        label?.let { value -> require(value.isNotBlank()) { "label must no be blank" } }

    }


}

package org.maiaframework.domain

interface EntityFieldConverter {

    fun convert(tableColumnName: String, inputValue: Any?): Any?

}

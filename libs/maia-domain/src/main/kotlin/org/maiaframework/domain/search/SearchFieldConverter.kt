package org.maiaframework.domain.search

interface SearchFieldConverter {


    fun convertValue(tableColumnPath: String, inputValue: Any?): Any?


}

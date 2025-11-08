package org.maiaframework.jdbc

interface SqlParameterSourceMapper<T> {

    fun populateParameterSourceMapper(type: T, paramSource: SqlParams)

}

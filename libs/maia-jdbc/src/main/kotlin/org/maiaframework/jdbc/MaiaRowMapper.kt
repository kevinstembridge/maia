package org.maiaframework.jdbc

fun interface MaiaRowMapper<T> {

    fun mapRow(rsa: ResultSetAdapter): T

}

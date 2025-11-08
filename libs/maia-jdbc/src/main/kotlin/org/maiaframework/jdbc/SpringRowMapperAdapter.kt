package org.maiaframework.jdbc

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class SpringRowMapperAdapter<T>(private val rowMapper: (ResultSetAdapter) -> T): RowMapper<T> {

    constructor(rowMapper: MaiaRowMapper<T>): this({ resultSetAdapter -> rowMapper.mapRow(resultSetAdapter) })


    override fun mapRow(rs: ResultSet, rowNum: Int): T? {

        val rsa = ResultSetAdapter(rs)
        return this.rowMapper.invoke(rsa)

    }


}

package org.maiaframework.showcase.many_to_many

import org.maiaframework.jdbc.MaiaRowMapper
import org.maiaframework.jdbc.ResultSetAdapter

class RightPkAndNameDtoRowMapper : MaiaRowMapper<RightPkAndNameDto> {


    override fun mapRow(rsa: ResultSetAdapter): RightPkAndNameDto {

        return RightPkAndNameDto(
            rsa.readDomainId("id"),
            rsa.readString("some_string")
        )

    }


}

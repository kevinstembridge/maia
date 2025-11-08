package org.maiaframework.jdbc

import java.sql.Types

enum class JdbcCompatibleType(
    val postgresDataType: String,
    val sqlType: Int,
) {

    array("bigint", Types.ARRAY),
    bigint("bigint", Types.BIGINT),
    boolean("boolean", Types.BIT),
    date("date", Types.DATE),
    decimal("decimal", Types.DECIMAL),
    integer("integer", Types.INTEGER),
    jsonb("jsonb", Types.OTHER),
    smallint("smallint", Types.SMALLINT),
    text("text", Types.VARCHAR),
    timestamp("timestamp", Types.TIMESTAMP),
    timestamp_with_time_zone("timestamp(3) with time zone", Types.TIMESTAMP_WITH_TIMEZONE),
    uuid("uuid", Types.OTHER),
    varchar("varchar", Types.VARCHAR),

}

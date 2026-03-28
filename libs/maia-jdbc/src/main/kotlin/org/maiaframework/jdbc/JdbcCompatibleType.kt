package org.maiaframework.jdbc

import java.sql.Types

enum class JdbcCompatibleType(
    val postgresDataType: String,
    val sqlType: Int,
) {

    bigint("bigint", Types.BIGINT),
    boolean("boolean", Types.BIT),
    boolean_array("boolean[]", Types.ARRAY),
    date("date", Types.DATE),
    decimal("decimal", Types.DECIMAL),
    decimal_array("decimal[]", Types.ARRAY),
    integer("integer", Types.INTEGER),
    integer_array("integer[]", Types.ARRAY),
    jsonb("jsonb", Types.OTHER),
    smallint("smallint", Types.SMALLINT),
    smallint_array("smallint[]", Types.ARRAY),
    text("text", Types.VARCHAR),
    text_array("text[]", Types.ARRAY),
    timestamp("timestamp", Types.TIMESTAMP),
    timestamp_array("timestamp[]", Types.ARRAY),
    timestamp_with_time_zone("timestamp(3) with time zone", Types.TIMESTAMP_WITH_TIMEZONE),
    uuid("uuid", Types.OTHER),
    uuid_array("uuid[]", Types.ARRAY),
    varchar("varchar", Types.VARCHAR),

}

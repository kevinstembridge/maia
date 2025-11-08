package org.maiaframework.jdbc

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.types.BooleanType
import org.maiaframework.types.DoubleType
import org.maiaframework.types.IntType
import org.maiaframework.types.LongType
import org.maiaframework.types.StringType
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.net.URL
import java.sql.Date
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneId

class SqlParams {


    val underlying = MapSqlParameterSource()


    fun addValue(
        paramName: String,
        value: Any?,
        sqlType: Int
    ): SqlParams {

        if (value != null) {
            this.underlying.addValue(paramName, value, sqlType)
        } else {
            setNull(paramName, sqlType)
        }

        return this

    }


    fun addListValue(
        paramName: String,
        value: Iterable<*>,
        sqlType: Int? = null
    ): SqlParams {

        if (sqlType == null) {
            this.underlying.addValue(paramName, value)
        } else {
            this.underlying.addValue(paramName, sqlType, sqlType)
        }

        return this

    }


    private fun setNull(
        paramName: String,
        sqlType: Int
    ) {

        this.underlying.addValue(paramName, null, sqlType)

    }


    override fun toString(): String {
        return this.underlying.values.toString()
    }


    fun addValue(
        dbColumn: DbColumn,
        instant: Instant?
    ): SqlParams {

        return addValue(dbColumn.value, instant)

    }


    fun addValue(
        columnName: String,
        instant: Instant?
    ): SqlParams {

        val offsetDateTime = instant?.let { OffsetDateTime.ofInstant(it, ZoneId.of("UTC")) }
        return addValue(columnName, offsetDateTime, Types.TIMESTAMP_WITH_TIMEZONE)

    }


    fun addValue(
        dbColumn: DbColumn,
        period: Period?
    ): SqlParams {

        return addValue(dbColumn.value, period?.toString())

    }


    fun addValue(
        columnName: String,
        period: Period?
    ): SqlParams {

        return addValue(columnName, period?.toString())

    }


    fun addValue(
        columnName: String,
        emailAddress: EmailAddress?
    ): SqlParams {

        return addValue(columnName, emailAddress?.value)

    }


    fun addValue(
        dbColumn: DbColumn,
        localDate: LocalDate?
    ): SqlParams {

        return addValue(dbColumn.value, localDate)

    }


    fun addValue(
        columnName: String,
        localDate: LocalDate?
    ): SqlParams {

        return addValue(columnName, localDate?.toString(), Types.DATE)

    }


    fun addValue(
        columnName: String,
        value: StringType<*>?
    ): SqlParams {

        return addValue(columnName, value?.value)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: StringType<*>?
    ): SqlParams {

        return addValue(dbColumn.value, value?.value)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: String?
    ): SqlParams {

        return addValue(dbColumn.value, value)

    }


    fun addValue(
        columnName: String,
        value: DomainId?
    ): SqlParams {

        return addValue(columnName, value?.value, Types.OTHER)

    }


    fun addValue(
        columnName: String,
        value: String?
    ): SqlParams {

        return addValue(columnName, value, Types.VARCHAR)

    }


    fun addValue(
        columnName: String,
        value: URL?
    ): SqlParams {

        return addValue(columnName, value, Types.VARCHAR)

    }


    fun addValue(
        columnName: String,
        value: List<*>
    ): SqlParams {

        return addListValue(columnName, value)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: Enum<*>?
    ): SqlParams {

        return addValue(dbColumn.value, value?.name)

    }


    fun addValue(
        columnName: String,
        value: Enum<*>?
    ): SqlParams {

        return addValue(columnName, value?.name, Types.VARCHAR)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: Int?
    ): SqlParams {

        return addValue(dbColumn.value, value)

    }


    fun addValue(
        columnName: String,
        value: Int?
    ): SqlParams {

        return addValue(columnName, value, Types.INTEGER)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: IntType<*>?
    ): SqlParams {

        return addValue(dbColumn.value, value?.value)

    }


    fun addValue(
        columnName: String,
        value: IntType<*>?
    ): SqlParams {

        return addValue(columnName, value?.value)

    }


    fun addValue(
        columnName: String,
        value: Long?
    ): SqlParams {

        return addValue(columnName, value, Types.BIGINT)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: LongType<*>?
    ): SqlParams {

        return addValue(dbColumn.value, value?.value)

    }


    fun addValue(
        columnName: String,
        value: LongType<*>?
    ): SqlParams {

        return addValue(columnName, value?.value)

    }


    fun addValue(
        columnName: String,
        value: Double?
    ): SqlParams {

        return addValue(columnName, value, Types.NUMERIC)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: DoubleType<*>?
    ): SqlParams {

        return addValue(dbColumn.value, value?.value)

    }


    fun addValue(
        columnName: String,
        value: DoubleType<*>?
    ): SqlParams {

        return addValue(columnName, value?.value)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: Boolean?
    ): SqlParams {

        return addValue(dbColumn.value, value)

    }


    fun addValue(
        columnName: String,
        value: Boolean?
    ): SqlParams {

        return addValue(columnName, value, Types.BIT)

    }


    fun addValue(
        dbColumn: DbColumn,
        value: BooleanType<*>?
    ): SqlParams {

        return addValue(dbColumn.value, value?.value)

    }


    fun addValue(
        columnName: String,
        value: BooleanType<*>?
    ): SqlParams {


        return addValue(columnName, value?.value)
    }


    fun addJsonValue(
        columnName: String,
        jsonString: String?
    ): SqlParams {

        return addValue(columnName, jsonString, Types.OTHER)

    }


    fun addListOfStrings(
        columnName: String,
        values: List<String>
    ): SqlParams {

        return addValue(columnName, values.toTypedArray<String>(), Types.ARRAY)

    }


    fun <T> addListOfStrings(
        columnName: String,
        values: List<T>,
        mapper: (T) -> String
    ): SqlParams {

        return addValue(columnName, values.map(mapper).toTypedArray<String>(), Types.ARRAY)

    }


    fun addListOfInstants(
        columnName: String,
        values: List<Instant>
    ): SqlParams {

        val array = values.map { Timestamp.from(it) }.toTypedArray()
        return addValue(columnName, array, Types.ARRAY)

    }


    fun addListOfLocalDates(
        columnName: String,
        values: List<LocalDate>
    ): SqlParams {

        val array = values.map { Date.valueOf(it) }.toTypedArray()
        return addValue(columnName, array, Types.ARRAY)

    }


}

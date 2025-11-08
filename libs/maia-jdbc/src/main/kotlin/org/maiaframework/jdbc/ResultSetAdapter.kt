package org.maiaframework.jdbc

import org.maiaframework.domain.DomainId
import java.net.URL
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.Period

class ResultSetAdapter(private val underlying: ResultSet) {


    fun readDomainId(columnName: String): DomainId {

        return DomainId(readString(columnName))

    }


    fun readDomainIdOrNull(columnName: String): DomainId? {

        return readStringOrNull(columnName) { DomainId(it) }

    }


    fun <T> readBoolean(columnName: String, mapper: (Boolean) -> T): T {

        return readBoolean(columnName).let(mapper)

    }


    fun readBoolean(columnName: String): Boolean {

        try {

            val actual = this.underlying.getBoolean(columnName)

            return if (this.underlying.wasNull()) {
                throw NullResultSetFieldException(columnName)
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun <T> readBooleanOrNull(columnName: String, func: (Boolean) -> T): T? {

        return readBooleanOrNull(columnName)?.let(func)

    }


    fun <T> readIntOrNull(columnName: String, func: (Int) -> T): T? {

        return readIntOrNull(columnName)?.let(func)

    }


    fun <T> readLongOrNull(columnName: String, func: (Long) -> T): T? {

        return readLongOrNull(columnName)?.let(func)

    }


    fun readBooleanOrNull(columnName: String): Boolean? {

        try {

            val actual = this.underlying.getBoolean(columnName)

            return if (this.underlying.wasNull()) {
                null
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }

    fun <T> readInt(columnName: String, mapper: (Int) -> T): T {

        return readInt(columnName).let(mapper)

    }


    fun readInt(columnName: String): Int {

        try {

            val actual = this.underlying.getInt(columnName)

            return if (this.underlying.wasNull()) {
                throw NullResultSetFieldException(columnName)
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readInt(columnIndex: Int): Int {

        try {

            val actual = this.underlying.getInt(columnIndex)

            return if (this.underlying.wasNull()) {
                throw NullResultSetFieldException(columnIndex)
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readIntOrNull(columnName: String): Int? {

        try {

            val actual = this.underlying.getInt(columnName)

            return if (this.underlying.wasNull()) {
                null
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun <T> readLong(columnName: String, mapper: (Long) -> T): T {

        return readLong(columnName).let(mapper)

    }


    fun <T> readLong(columnIndex: Int, mapper: (Long) -> T): T {

        return readLong(columnIndex).let(mapper)

    }


    fun readLong(columnName: String): Long {

        try {

            val actual = this.underlying.getLong(columnName)

            return if (this.underlying.wasNull()) {
                throw NullResultSetFieldException(columnName)
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readLong(columnIndex: Int): Long {

        try {

            val actual = this.underlying.getLong(columnIndex)

            return if (this.underlying.wasNull()) {
                throw NullResultSetFieldException(columnIndex)
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readLongOrNull(columnName: String): Long? {

        try {

            val actual = this.underlying.getLong(columnName)

            return if (this.underlying.wasNull()) {
                null
            } else {
                actual
            }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun <T : Enum<T>> readEnum(
        columnName: String,
        enumClass: Class<T>
    ): T {

        val raw = readString(columnName)

        try {

            return java.lang.Enum.valueOf(enumClass, raw)

        } catch (e: IllegalArgumentException) {
            throw InvalidEnumValueException(raw, columnName, enumClass)
        }

    }


    fun <T : Enum<T>> readEnumOrNull(
        columnName: String,
        enumClass: Class<T>
    ): T? {

        val raw = readStringOrNull(columnName)
            ?: return null

        try {

            return java.lang.Enum.valueOf(enumClass, raw)

        } catch (e: IllegalArgumentException) {
            throw InvalidEnumValueException(raw, columnName, enumClass)
        }

    }


    fun readString(dbColumn: DbColumn): String {

        return readString(dbColumn.value)

    }


    fun readStringOrNull(dbColumn: DbColumn): String? {

        return readStringOrNull(dbColumn.value)

    }


    fun readString(columnName: String): String {

        return readStringOrNull(columnName)
            ?: throw NullResultSetFieldException(columnName)

    }


    fun <T> readString(columnName: String, mappingFunc: (String) -> T): T {

        return readStringOrNull(columnName, mappingFunc)
            ?: throw NullResultSetFieldException(columnName)

    }


    fun readStringOrNull(columnName: String): String? {

        try {
            return this.underlying.getString(columnName)
        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun <T> readStringOrNull(columnName: String, mappingFunc: (String) -> T): T? {

        try {
            return this.underlying.getString(columnName)?.let(mappingFunc)
        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun <T> readListOfStrings(columnName: String, mapper: (String) -> T): List<T> {

        return readListOfStrings(columnName).map(mapper)

    }


    fun readListOfStrings(columnName: String): List<String> {

        try {

            @Suppress("UNCHECKED_CAST")
            return (underlying.getArray(columnName).array as Array<String>).toList()

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readUrl(dbColumn: DbColumn): URL {

        return readUrl(dbColumn.value)

    }


    fun readUrlOrNull(dbColumn: DbColumn): URL? {

        return readUrlOrNull(dbColumn.value)

    }


    fun readUrl(columnName: String): URL {

        return readUrlOrNull(columnName)
            ?: throw NullResultSetFieldException(columnName)

    }


    fun readUrlOrNull(columnName: String): URL? {

        try {
            return this.underlying.getString(columnName)?.let(::URL)
        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readInstant(dbColumn: DbColumn): Instant {

        return readInstant(dbColumn.value)

    }


    fun readInstant(columnName: String): Instant {

        return readInstantOrNull(columnName)
            ?: throw NullResultSetFieldException(columnName)

    }


    fun readInstantOrNull(columnName: String): Instant? {

        try {
            val timestamp = this.underlying.getTimestamp(columnName)
            return readInstantOrNull(timestamp)
        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    private fun readInstantOrNull(timestamp: Timestamp?): Instant? {

        return timestamp?.toInstant()

    }


    fun readListOfInstants(columnName: String): List<Instant> {

        try {

            @Suppress("UNCHECKED_CAST")
            val array = this.underlying.getArray(columnName).array as Array<Timestamp>
            return array.map { it.toInstant() }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readListOfLocalDates(columnName: String): List<LocalDate> {

        try {

            @Suppress("UNCHECKED_CAST")
            val array = this.underlying.getArray(columnName).array as Array<Date>
            return array.map { it.toLocalDate() }

        } catch (e: SQLException) {
            throw MaiaDataAccessException(e)
        }

    }


    fun readPeriod(dbColumn: DbColumn): Period {

        return readPeriod(dbColumn.value)

    }


    fun readPeriod(columnName: String): Period {

        return readPeriodOrNull(columnName)
            ?: throw NullResultSetFieldException(columnName)

    }


    fun readPeriodOrNull(columnName: String): Period? {

        return readStringOrNull(columnName) {
            Period.parse(it)
        }

    }


    fun readLocalDate(columnName: String): LocalDate {

        return readLocalDateOrNull(columnName)
            ?: throw NullResultSetFieldException(columnName)

    }


    fun readLocalDateOrNull(columnName: String): LocalDate? {

        return readStringOrNull(columnName) { LocalDate.parse(it) }

    }


}

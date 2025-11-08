package org.maiaframework.hazelcast.serializers

import com.hazelcast.nio.serialization.compact.CompactReader
import java.time.Instant
import java.time.LocalDate

object CompactReaderExtension {


    fun CompactReader.readStringNonNull(fieldName: String): String {

        return CompactReaderException.throwIfNullOrBlank(fieldName, this.readString(fieldName))

    }


    fun <T> CompactReader.readStringNonNull(fieldName: String, mapperFunc: (String) -> T): T {

        val value = this.readString(fieldName)
        CompactReaderException.throwIfNullOrBlank(fieldName, value)
        return mapperFunc.invoke(value!!)

    }


    fun CompactReader.readNullableString(fieldName: String): String? {

        return this.readString(fieldName)

    }


    fun <T> CompactReader.readNullableString(fieldName: String, mapperFunc: (String) -> T?): T? {

        val value = this.readString(fieldName)
        return value?.let { mapperFunc(it) }

    }


    fun CompactReader.readNullableLocalDate(fieldName: String): LocalDate? {

        return this.readNullableString(fieldName)?.let { LocalDate.parse(it) }

    }


    fun CompactReader.readInt32NonNull(fieldName: String): Int {

        val raw = this.readInt32(fieldName)
        CompactReaderException.throwIfNull(fieldName, raw)
        return raw

    }


    fun CompactReader.readInt64NonNull(fieldName: String): Long {

        val raw = this.readInt64(fieldName)
        CompactReaderException.throwIfNull(fieldName, raw)
        return raw

    }


    fun CompactReader.readInstantNonNull(fieldName: String): Instant {

        val timestamp = this.readTimestampWithTimezone(fieldName)
        CompactReaderException.throwIfNull(fieldName, timestamp)
        return timestamp!!.toInstant()

    }


    fun CompactReader.readListOfStrings(fieldName: String): List<String> {

        return this.readArrayOfString(fieldName)?.toList() ?: emptyList()

    }


    fun <T> CompactReader.readListOfCompact(fieldName: String, compactClass: Class<T>): List<T> {

        return this.readArrayOfCompact(fieldName, compactClass)?.toList() ?: emptyList()

    }


    fun <T> CompactReader.readListOfStrings(fieldName: String, mappingFunc: (String) -> T): List<T> {

        return this.readListOfStrings(fieldName).map(mappingFunc)

    }


    fun CompactReader.readSetOfStrings(fieldName: String): Set<String> {

        return this.readListOfStrings(fieldName).toSet()

    }


    fun <T> CompactReader.readSetFromStrings(fieldName: String, mappingFunc: (String) -> T): Set<T> {

        return this.readListOfStrings(fieldName).map(mappingFunc).toSet()

    }


    fun <T> CompactReader.readListOfSetsFromCsvStrings(fieldName: String, mapper: (String) -> T): List<Set<T>> {

        return this.readListOfStrings(fieldName).map { it.split(",").map(mapper).toSet() }

    }


    fun CompactReader.readListOfSetsFromCsvStrings(fieldName: String): List<Set<String>> {

        return this.readListOfStrings(fieldName).map { it.split(",").toSet() }

    }


}

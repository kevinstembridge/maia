package org.maiaframework.hazelcast.serializers

import com.hazelcast.nio.serialization.compact.CompactWriter
import java.time.Instant
import java.time.LocalDate

object CompactWriterExtension {


    fun CompactWriter.writeListOfStrings(fieldName: String, strings: List<String>) {

        this.writeArrayOfString(fieldName, strings.toTypedArray())

    }


    fun CompactWriter.writeSetOfStrings(fieldName: String, strings: Set<String>) {

        this.writeArrayOfString(fieldName, strings.toTypedArray())

    }


    fun CompactWriter.writeInstant(fieldName: String, instant: Instant) {

        this.writeString(fieldName, instant.toString())

    }


    fun CompactWriter.writeSetOfStringsAsCsv(fieldName: String, strings: Collection<Set<String>>) {

        val csvStrings: List<String> = strings.map { it.joinToString(",") }
        this.writeArrayOfString(fieldName, csvStrings.toTypedArray())

    }


    fun <T> CompactWriter.writeSetOfStringsAsCsv(fieldName: String, strings: Collection<Set<T>>, mapper: (T) -> String) {

        val csvStrings = strings.map { it.map(mapper).joinToString(",") }.toTypedArray()
        this.writeArrayOfString(fieldName, csvStrings)

    }


    fun CompactWriter.writeListOfStringsAsCsv(fieldName: String, strings: Collection<List<String>>) {

        val csvStrings: List<String> = strings.map { it.joinToString(",") }
        this.writeArrayOfString(fieldName, csvStrings.toTypedArray())

    }


    fun <T> CompactWriter.writeListOfStringsAsCsv(fieldName: String, strings: Collection<List<T>>, mapper: (T) -> String) {

        val csvStrings = strings.map { it.map(mapper).joinToString(",") }.toTypedArray()
        this.writeArrayOfString(fieldName, csvStrings)

    }


    fun CompactWriter.writeNullableDate(fieldName: String, localDate: LocalDate?) {

        if (localDate == null) {
            this.writeString(fieldName, null)
        } else {
            this.writeString(fieldName, localDate.toString())
        }

    }


}

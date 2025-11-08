package org.maiaframework.dao.mongo


import org.maiaframework.domain.DomainId
import org.maiaframework.domain.types.CollectionName
import org.maiaframework.lang.text.StringFunctions
import org.bson.Document
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date
import java.util.function.Function
import java.util.stream.Collectors


object DocumentFacade {


    fun <SOURCE, T> readList(
        mapper: (SOURCE) -> T,
        collectionFieldName: String,
        collectionName: CollectionName? = null,
        classFieldName: String,
        document: Document
    ): List<T> {

        val value = document[collectionFieldName] as List<SOURCE>?

        return if (value == null) {

            emptyList()

        } else {

            try {
                value.map { mapper.invoke(it) }
            } catch (e: RuntimeException) {
                val id = document.getObjectId("_id")
                throw InvalidFieldException(id, collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    fun <T : Enum<T>> readEnum(
        enumClass: Class<T>,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = document.getString(collectionFieldName)
        MissingFieldException.throwIfBlank(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)

        try {
            return java.lang.Enum.valueOf<T>(enumClass, value)
        } catch (e: IllegalArgumentException) {
            throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
        }

    }


    fun <T : Enum<T>> readEnumList(
        enumClass: Class<T>,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document): List<T> {

        val value = document[collectionFieldName] as List<String>?

        return if (value == null) {

            emptyList()

        } else {

            try {
                value.map { rawValue -> java.lang.Enum.valueOf<T>(enumClass, rawValue) }
            } catch (e: RuntimeException) {
                val id = document.getObjectId("_id")
                throw InvalidFieldException(id, collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    fun <T : Enum<T>> readEnumOrNull(
        enumClass: Class<T>,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = document.getString(collectionFieldName) ?: return null

        try {
            return java.lang.Enum.valueOf<T>(enumClass, value)
        } catch (e: IllegalArgumentException) {
            throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
        }

    }


    fun <T : Enum<T>> readEnumList_kt(
        enumClass: Class<T>,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document): List<T> {

        val value = document[collectionFieldName] as List<String>?

        return if (value == null) {

            emptyList()

        } else {

            try {
                value.map { rawValue -> java.lang.Enum.valueOf<T>(enumClass, rawValue) }
            } catch (e: RuntimeException) {
                val id = document.getObjectId("_id")
                throw InvalidFieldException(id, collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    @JvmStatic
    fun readBoolean(obj: Any): Boolean? {

        return obj as Boolean

    }


    @JvmStatic
    fun readInteger(obj: Any): Int? {

        return obj as Int

    }


    fun <T> readBoolean(
        mapper: (Boolean) -> T,
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): T {

        val value = readBoolean(collectionFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    fun <T> readBooleanOrNull(
        mapper: (Boolean) -> T,
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): T? {

        val value: Boolean? = readBooleanOrNull(collectionFieldName, collectionName, classFieldName, document)
        return value?.run(mapper)

    }


    fun readBoolean(
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): Boolean {

        val value = document.getBoolean(collectionFieldName)

        return value ?: false

    }


    fun readBooleanOrNull(
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): Boolean? {

        return document.getBoolean(collectionFieldName)

    }


    fun readObjectId(
            documentFieldName: String,
            collectionName: CollectionName?,
            classFieldName: String,
            document: Document
    ): ObjectId {

        val value = document.getObjectId(documentFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), documentFieldName, classFieldName, collectionName)
        return value

    }


    fun <T> readObjectId_kt(
        mapper: (ObjectId) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readObjectId(collectionFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    fun <T> readObjectIdOrNull(
        mapper: (ObjectId) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = readObjectIdOrNull(collectionFieldName, collectionName, classFieldName, document)
        return value?.run(mapper)

    }


    fun readObjectId_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): ObjectId {

        val value = document.getObjectId(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value

    }


    fun readDomainId(
            documentFieldName: String,
            collectionName: CollectionName?,
            classFieldName: String,
            document: Document
    ): DomainId {

        val value = document.getObjectId(documentFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), documentFieldName, classFieldName, collectionName)
        return DomainId(value.toHexString())

    }


    fun <T> readDomainId_kt(
        mapper: (DomainId) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readDomainId(collectionFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    fun readDomainId_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): DomainId {

        val value = document.getString(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return DomainId(value)

    }


    fun readDocument(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Document {

        val value = document.get(collectionFieldName, Document::class.java)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value

    }


    fun readObjectIdOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): ObjectId? {

        return document.getObjectId(collectionFieldName)

    }


    fun readDomainIdOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): DomainId? {

        return DomainId(document.getString(collectionFieldName))

    }


    fun <T> readInt(
        mapper: (Int) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = document.getInteger(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return mapper.invoke(value)

    }


    fun <T> readIntOrNull(
        mapper: (Int) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = document.getInteger(collectionFieldName)
        return value?.run(mapper)

    }


    fun readInt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Int {

        val value = document.getInteger(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value!!

    }


    fun readIntOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Int? {

        return document.getInteger(collectionFieldName)

    }


    fun <T> readLong(
        mapper: (Long) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = document.getLong(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return mapper.invoke(value)

    }


    fun <T> readLongOrNull(
        mapper: (Long) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = document.getLong(collectionFieldName)
        return value?.run(mapper)

    }


    fun readLong(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Long {

        val value = document.getLong(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value!!

    }


    fun readLongOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Long? {

        return document.getLong(collectionFieldName)

    }


    fun <T> readDouble(
        mapper: (Double) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = document.getDouble(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return mapper.invoke(value)

    }


    fun <T> readDoubleOrNull(
        mapper: (Double) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = document.getDouble(collectionFieldName)
        return value?.run(mapper)

    }


    fun readDouble(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Double {

        val value = document.getDouble(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value!!

    }


    fun readDoubleOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Double? {

        return document.getDouble(collectionFieldName)

    }


    @JvmStatic
    fun <T> readInstant(
        mapper: Function<Instant, T>,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readInstant(collectionFieldName, collectionName, classFieldName, document)
        return mapper.apply(value)

    }


    fun <T> readInstant_kt(
        mapper: (Instant) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readInstant(collectionFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    @JvmStatic
    fun readInstant(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Instant {

        val value = document.getDate(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value.toInstant()

    }


    fun readInstant_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Instant {

        val value = document.getDate(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value.toInstant()

    }


    fun readInstantOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Instant? {

        val value = document.getDate(collectionFieldName)
        return value?.toInstant()

    }


    fun <T> readLocalDate(
        mapper: (LocalDate) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readLocalDate(collectionFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    fun <T> readLocalDateOrNull(
        mapper: (LocalDate) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = readLocalDateOrNull(collectionFieldName, collectionName, classFieldName, document)
        return value?.run(mapper)

    }


    @JvmStatic
    fun readLocalDate(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): LocalDate {

        val value = document.getDate(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()

    }


    fun readLocalDate_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): LocalDate {

        val value = document.getDate(collectionFieldName)
        MissingFieldException.throwIfNull(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()

    }


    fun readLocalDateOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): LocalDate? {

        val value = document.getDate(collectionFieldName)
        return value?.toInstant()?.atZone(ZoneId.of("UTC"))?.toLocalDate()

    }


    fun <T> readString(
            mapper: (String) -> T,
            documentFieldName: String,
            collectionName: CollectionName,
            classFieldName: String,
            document: Document
    ): T {

        val value = readString(documentFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    fun <T> readStringOrNull(
        mapper: (String) -> T,
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): T? {

        val value = readStringOrNull(collectionFieldName, collectionName, classFieldName, document)
        return value?.run(mapper)

    }


    @JvmStatic
    fun readString(
            documentFieldName: String,
            collectionName: CollectionName,
            classFieldName: String,
            document: Document
    ): String {

        val value = document.getString(documentFieldName)
        MissingFieldException.throwIfBlank(value, document.getObjectId("_id"), documentFieldName, classFieldName, collectionName)
        return value

    }


    fun readString_kt(
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): String {

        val value = document.getString(collectionFieldName)
        MissingFieldException.throwIfBlank(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return value

    }


    fun readStringOrNull(
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): String? {

        return StringFunctions.stripToNull(document.getString(collectionFieldName))

    }


    @JvmStatic
    fun readStringList(
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): List<String> {

        val value = document[collectionFieldName] as List<String>?

        return value ?: emptyList()

    }


    fun readStringList_kt(
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): List<String> {

        val value = document[collectionFieldName] as List<String>?

        return value ?: emptyList()

    }


    @JvmStatic
    fun readInstantList(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): List<Instant> {

        val value = document[collectionFieldName] as List<Date>?

        return if (value == null) {
            emptyList()
        } else {

            try {
                value.map { it.toInstant() }
            } catch (e: ClassCastException) {
                throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    fun readInstantList_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): List<Instant> {

        val value = document[collectionFieldName] as List<Date>?

        return if (value == null) {
            emptyList()
        } else {

            try {
                value.map { it.toInstant() }
            } catch (e: ClassCastException) {
                throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    @JvmStatic
    fun readLocalDateList(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): List<LocalDate> {

        val value = document[collectionFieldName] as List<Date>?

        return if (value == null) {
            emptyList()
        } else {

            try {
                value.map { d -> d.toInstant().atZone(ZoneId.of("UTC")).toLocalDate() }
            } catch (e: ClassCastException) {
                throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    fun readLocalDateList_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): List<LocalDate> {

        val value = document[collectionFieldName] as List<Date>?

        return if (value == null) {
            emptyList()
        } else {

            try {
                value.map { d -> d.toInstant().atZone(ZoneId.of("UTC")).toLocalDate() }
            } catch (e: ClassCastException) {
                throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    @JvmStatic
    fun readPeriodList(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): List<Period> {

        val value = document[collectionFieldName] as List<String>?

        return if (value == null) {
            emptyList()
        } else {

            try {
                value.stream().map { Period.parse(it) }.collect(Collectors.toList())
            } catch (e: ClassCastException) {
                throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    fun readPeriodList_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): List<Period> {

        val value = document[collectionFieldName] as List<String>?

        return if (value == null) {
            emptyList()
        } else {

            try {
                value.stream().map { Period.parse(it) }.collect(Collectors.toList())
            } catch (e: ClassCastException) {
                throw InvalidFieldException(document.getObjectId("_id"), collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    @JvmStatic
    fun <T> readPeriod(
        mapper: Function<Period, T>,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readPeriod(collectionFieldName, collectionName, classFieldName, document)
        return mapper.apply(value)

    }


    fun <T> readPeriod_kt(
        mapper: (Period) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T {

        val value = readPeriod(collectionFieldName, collectionName, classFieldName, document)
        return mapper.invoke(value)

    }


    fun <T> readPeriodOrNull(
        mapper: (Period) -> T,
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): T? {

        val value = readPeriodOrNull(collectionFieldName, collectionName, classFieldName, document)
        return value?.run(mapper)

    }


    @JvmStatic
    fun readPeriod(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Period {

        val value = document.getString(collectionFieldName)
        MissingFieldException.throwIfBlank(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return Period.parse(value)

    }


    fun readPeriod_kt(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Period {

        val value = document.getString(collectionFieldName)
        MissingFieldException.throwIfBlank(value, document.getObjectId("_id"), collectionFieldName, classFieldName, collectionName)
        return Period.parse(value)

    }


    fun readPeriodOrNull(
        collectionFieldName: String,
        collectionName: CollectionName?,
        classFieldName: String,
        document: Document
    ): Period? {

        val value = document.getString(collectionFieldName)

        if (value != null) {
            return Period.parse(value)
        } else {
            return null
        }

    }


    @JvmStatic
    fun <KEY, TARGET_VALUE> readMap(
        keyMapper: Function<String, KEY>,
        valueMapper: Function<Any, TARGET_VALUE>,
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): Map<KEY, TARGET_VALUE> {

        val rawMap = document[collectionFieldName] as Map<String, Any>?

        return if (rawMap == null) {

            emptyMap()

        } else {

            try {
                rawMap.mapKeys { keyMapper.apply(it.key) }.mapValues { valueMapper.apply(it.value) }
            } catch (e: Exception) {
                throw InvalidFieldException(document["_id"] as ObjectId, collectionName, collectionFieldName, classFieldName, e)
            }

        }

    }


    fun <KEY, TARGET_VALUE> readMap_kt(
        keyMapper: (String) -> KEY,
        valueMapper: (Any?) -> TARGET_VALUE,
        collectionFieldName: String,
        collectionName: CollectionName,
        classFieldName: String,
        document: Document
    ): Map<KEY, TARGET_VALUE> {

        val rawMap = document[collectionFieldName] as Map<String, Any?>?

        return if (rawMap == null) {

            emptyMap()

        } else {

            rawMap.mapKeys { keyMapper.invoke(it.key) }.mapValues { valueMapper.invoke(it.value) }

        }

    }


}

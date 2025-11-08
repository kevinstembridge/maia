package org.maiaframework.gen.persist.mongo


import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import java.util.function.Function


class MapFieldReader<KEY, SOURCE_VALUE, TARGET_VALUE>(private val keyMapper: Function<String, KEY>, private val valueMapper: Function<SOURCE_VALUE, TARGET_VALUE>) {


    fun readField(collectionFieldName: String, classFieldName: String, document: Document, collectionName: CollectionName): Map<KEY, TARGET_VALUE> {

        val rawMap = document[collectionFieldName] as Map<String, SOURCE_VALUE>?

        return if (rawMap == null) {

            emptyMap()

        } else {

            rawMap.mapKeys { keyMapper.apply(it.key) }.mapValues { valueMapper.apply(it.value) }

        }

    }


}

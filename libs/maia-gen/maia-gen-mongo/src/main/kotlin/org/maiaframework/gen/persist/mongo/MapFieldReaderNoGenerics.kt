package org.maiaframework.gen.persist.mongo


import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import java.util.function.Function


class MapFieldReaderNoGenerics<KEY, VALUE>(private val keyMapper: Function<String, KEY>, private val valueMapper: Function<Any, VALUE>) {


    fun readField(collectionFieldName: String, classFieldName: String, document: Document, collectionName: CollectionName): Map<KEY, VALUE> {

        val rawMap = document[collectionFieldName] as Map<String, Any>?

        return if (rawMap == null) {

            emptyMap()

        } else {

            rawMap.mapKeys { keyMapper.apply(it.key) }.mapValues { valueMapper.apply(it) }

        }

    }


}

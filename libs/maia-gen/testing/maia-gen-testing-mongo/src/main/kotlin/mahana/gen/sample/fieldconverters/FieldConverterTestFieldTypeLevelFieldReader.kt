package org.maiaframework.gen.sample.fieldconverters

import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import org.springframework.stereotype.Component
import java.util.*


@Component
class FieldConverterTestFieldTypeLevelFieldReader {


    private var value = UUID.randomUUID().toString()


    fun setNextValue(value: String) {

        this.value = value

    }


    fun readField(collectionFieldName: String, classFieldName: String, document: Document, collectionName: CollectionName): String {

        val valueFromDocument = document.getString(collectionFieldName)
        return valueFromDocument + this.value

    }


}

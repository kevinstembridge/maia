package org.maiaframework.domain.mongo

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.EntityFieldConverter
import org.maiaframework.domain.persist.FieldUpdate
import org.bson.Document
import org.bson.conversions.Bson
import java.util.*


abstract class AbstractEntityUpdater protected constructor(
    val id: DomainId,
    val version: Long? = null,
    val fields: List<FieldUpdate>
) {


    fun filterBson(): Bson {

        val filter = Document("_id", this.id)
        this.version?.let { v -> filter.append("v", v) }
        return filter

    }


    fun asBson(fieldConverter: EntityFieldConverter, incrementVersion: Boolean = true): Bson {

        val setFieldsDocument = Document()
        val unsetFieldsDocument = Document()

        this.fields.forEach { fieldUpdate ->

            val collectionFieldName = fieldUpdate.dbColumnName
            val fieldValue = fieldUpdate.value

            if (fieldValue == null) {

                unsetFieldsDocument[collectionFieldName] = ""

            } else if (fieldValue is Optional<*>) {

                if (fieldValue.isPresent) {
                    setFieldsDocument[collectionFieldName] = fieldConverter.convert(collectionFieldName, fieldValue.get())
                } else {
                    unsetFieldsDocument[collectionFieldName] = ""
                }

            } else {

                setFieldsDocument[collectionFieldName] = fieldConverter.convert(collectionFieldName, fieldValue)

            }

        }

        val document = Document()

        if (setFieldsDocument.isNotEmpty()) {
            document["\$set"] = setFieldsDocument
        }

        if (unsetFieldsDocument.isNotEmpty()) {
            document["\$unset"] = unsetFieldsDocument
        }

        if (version != null && incrementVersion) {
            document["\$inc"] = Document("v", 1)
        }

        return document

    }


}


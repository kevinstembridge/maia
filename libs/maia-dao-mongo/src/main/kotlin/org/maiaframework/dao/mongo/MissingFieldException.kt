package org.maiaframework.dao.mongo


import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*


class MissingFieldException(
    val id: ObjectId,
    val collectionName: CollectionName?,
    val collectionFieldName: String,
    val classFieldName: String,
    val fieldClass: Class<*>
) : RuntimeException("Missing a value for required field: collection = "
        + collectionName
        + ", collectionField = "
        + collectionFieldName
        + ", type = "
        + fieldClass.name
        + ", id = "
        + id
        + ", classField = "
        + classFieldName
        + ".") {

    companion object {


        fun throwIfNull(fieldValue: Date?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, Date::class.java)

        }


        fun throwIfNull(fieldValue: ObjectId?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, ObjectId::class.java)

        }


        fun throwIfNull(fieldValue: Document?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, Document::class.java)

        }


        fun throwIfNull(fieldValue: Boolean?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, Boolean::class.java)

        }


        fun throwIfNull(fieldValue: String?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, String::class.java)

        }


        fun throwIfNull(fieldValue: Int?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, Int::class.java)

        }


        fun throwIfNull(fieldValue: Long?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, Long::class.java)

        }


        fun throwIfNull(fieldValue: Double?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            throwIfNull(fieldValue, id, collectionFieldName, classFieldName, collectionName, Double::class.java)

        }


        fun throwIfNull(fieldValue: Any?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?, fieldClass: Class<*>) {

            if (fieldValue == null) {
                throw MissingFieldException(id, collectionName, collectionFieldName, classFieldName, fieldClass)
            }

        }


        fun throwIfBlank(fieldValue: String?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?) {

            if (fieldValue == null || fieldValue.trim { it <= ' ' }.isEmpty()) {
                throw MissingFieldException(id, collectionName, collectionFieldName, classFieldName, String::class.java)
            }

        }


        private fun throwIfNull(fieldValue: Double?, id: ObjectId, collectionFieldName: String, classFieldName: String, collectionName: CollectionName?, fieldClass: Class<*>) {

            if (fieldValue == null) {
                throw MissingFieldException(id, collectionName, collectionFieldName, classFieldName, fieldClass)
            }

        }
    }


}

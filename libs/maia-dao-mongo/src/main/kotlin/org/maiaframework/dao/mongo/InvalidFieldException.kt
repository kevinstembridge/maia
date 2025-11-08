package org.maiaframework.dao.mongo


import org.maiaframework.domain.types.CollectionName
import org.bson.types.ObjectId


class InvalidFieldException(
    val id: ObjectId,
    val collectionName: CollectionName?,
    val collectionFieldName: String,
    val classFieldName: String,
    cause: Throwable
) : RuntimeException("Invalid value in field: collection = $collectionName, collectionField = $collectionFieldName, id = $id, classField = $classFieldName.", cause)

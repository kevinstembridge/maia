package org.maiaframework.dao.mongo

import org.maiaframework.domain.types.CollectionName

data class MongoLookupDescriptor(
    val fromCollection: CollectionName,
    val localField: String,
    val foreignField: String,
    val dtoFieldsFromLookupEntity: Set<ForeignField>
)

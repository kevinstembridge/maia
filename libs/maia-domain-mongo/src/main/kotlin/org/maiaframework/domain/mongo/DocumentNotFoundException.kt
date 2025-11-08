package org.maiaframework.domain.mongo

import org.maiaframework.domain.types.CollectionName
import org.bson.conversions.Bson


class DocumentNotFoundException(val collectionName: CollectionName, val query: Bson) : RuntimeException("Unable to find document in collection [$collectionName] with query $query")

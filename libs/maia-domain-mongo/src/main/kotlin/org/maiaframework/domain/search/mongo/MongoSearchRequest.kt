package org.maiaframework.domain.search.mongo

import org.bson.conversions.Bson

data class MongoSearchRequest(
    val query: Bson,
    val projection: Bson?,
    val offset: Int,
    val limit: Int?,
    val sort: Bson?)

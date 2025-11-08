package org.maiaframework.dao.mongo.search

import org.bson.conversions.Bson

data class MongoAggregationSearchRequest(
    val searchAggregations: List<Bson>,
    val totalCountAggregations: List<Bson>,
    val skip: Int,
    val limit: Int?)

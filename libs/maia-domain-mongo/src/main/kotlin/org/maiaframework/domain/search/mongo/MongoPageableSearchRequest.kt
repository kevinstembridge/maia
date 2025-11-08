package org.maiaframework.domain.search.mongo

import org.bson.conversions.Bson
import org.springframework.data.domain.Pageable

data class MongoPageableSearchRequest(val query: Bson, val pageable: Pageable?)

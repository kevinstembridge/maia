package org.maiaframework.gen.search

import org.maiaframework.domain.search.SearchModel

class SearchableException(
    val searchModel: SearchModel,
    cause: Throwable
): RuntimeException(
    "Error for search: $searchModel. $cause",
    cause
)

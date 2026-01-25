package org.maiaframework.domain.search

class SearchableException(
    val searchModel: SearchModel,
    cause: Throwable
): RuntimeException(
    "Error for search: $searchModel. $cause",
    cause
)

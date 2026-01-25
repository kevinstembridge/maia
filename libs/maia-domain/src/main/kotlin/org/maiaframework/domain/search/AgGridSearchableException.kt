package org.maiaframework.domain.search

class AgGridSearchableException(
    val searchModel: AgGridSearchModel,
    cause: Throwable
): RuntimeException(
    "Error for search: $searchModel. $cause",
    cause
)

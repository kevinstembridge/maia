package org.maiaframework.gen.search

import org.maiaframework.domain.search.AgGridSearchModel

class AgGridSearchableException(
    val searchModel: AgGridSearchModel,
    cause: Throwable
): RuntimeException(
    "Error for search: $searchModel. $cause",
    cause
)

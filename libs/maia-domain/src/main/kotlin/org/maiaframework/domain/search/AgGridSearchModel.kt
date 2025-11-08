package org.maiaframework.domain.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AgGridSearchModel(
    val filterModel: JsonNode,
    val sortModel: List<AgGridSortModelItem>,
    /**
     * Zero-indexed.
     */
    val startRow: Int,
    val endRow: Int? = null
)


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AgGridSortModelItem(
    val colId: String,
    val sort: String
)


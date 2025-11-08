package org.maiaframework.domain.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchModel(
    val filterModel: List<FilterModelItem>,
    val sortModel: List<SortModelItem>,
    val startRow: Int,
    val endRow: Int? = null
)


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class FilterModelItem(
    val fieldPath: String,
    val fieldType: String,
    val filterType: String,
    val filter: String? = null,
    val filterTo: String? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val caseSensitive: Boolean = false
)


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SortModelItem(
    val fieldPath: String,
    val sortDirection: String
)

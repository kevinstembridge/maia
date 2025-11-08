package org.maiaframework.jdbc

import org.maiaframework.domain.search.FilterModelItem
import java.time.Instant
import java.time.LocalDate

object FilterModelHelper {


    fun sqlParamStringFor(filterModelItem: FilterModelItem): String {

        val filterType = filterModelItem.filterType

        return when (filterType) {
            "contains" -> "%${filterModelItem.filter}%"
            else -> filterModelItem.filter!!
        }

    }


    fun sqlParamIntFor(filterModelItem: FilterModelItem): Int {

        return filterModelItem.toInt()

    }


    private fun FilterModelItem.toInt(): Int {

        try {
            return filter!!.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Expecting filter model item to be numeric: $this", e)
        }

    }


    fun sqlParamInstantFor(filterModelItem: FilterModelItem): Instant {

        // TODO cater for different filter types (dateFrom, dateTo, etc)
        return when (val filterType = filterModelItem.filterType) {
            "equals" -> Instant.parse(filterModelItem.filter)
            else -> throw IllegalArgumentException("Unsupported filterType '$filterType'")
        }

    }


    fun sqlParamLocalDateFor(filterModelItem: FilterModelItem): LocalDate {

        val filterType = filterModelItem.filterType
        val filterValue = LocalDate.parse(filterModelItem.filter)

        // TODO cater for different filter types (dateFrom, dateTo, etc)
        return when (filterType) {
            "equals" -> filterValue
            else -> filterValue
        }

    }


}

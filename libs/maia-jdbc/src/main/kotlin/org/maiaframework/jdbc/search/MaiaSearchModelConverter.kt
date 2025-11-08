package org.maiaframework.jdbc.search

import org.maiaframework.domain.search.FilterModelItem
import org.maiaframework.domain.search.SearchModel
import org.maiaframework.jdbc.SqlParams
import java.sql.Types

class MaiaSearchModelConverter(
    private val fieldNameToColumnNameMapper: (String) -> String
) {


    fun buildWhereClauseFor(
        filterModelItems: List<FilterModelItem>,
        sqlParams: SqlParams,
        typeDiscriminatorExpression: String? = null
    ): String {

        val expressions = filterModelItems.map { filterModelItem ->
            buildExpressionFor(filterModelItem, sqlParams)
        }.plus(typeDiscriminatorExpression)
            .filterNotNull()

        return expressions.mapIndexed { index, expression ->

            val whereOrAnd = if (index == 0) "where" else "and"
            "$whereOrAnd $expression"

        }.joinToString(" ")

    }


    private fun buildExpressionFor(
        filterModelItem: FilterModelItem,
        sqlParams: SqlParams
    ): String {

        val classFieldName = filterModelItem.fieldPath
        val dbColumnName = fieldNameToColumnNameMapper.invoke(classFieldName)

        val fieldType = filterModelItem.fieldType
        val filterType = filterModelItem.filterType

        when (fieldType) {

            "id" -> {
                sqlParams.addValue(classFieldName, filterModelItem.filter, Types.OTHER)
                return "$dbColumnName = :$classFieldName"
            }
            "text" -> {

                val likeOrIlike = if (filterModelItem.caseSensitive) "like" else "ilike"

                when (filterType) {

                    "equals" -> {
                        sqlParams.addValue(classFieldName, filterModelItem.filter)
                        return "$dbColumnName = :$classFieldName"
                    }

                    "notEqual" -> {
                        sqlParams.addValue(classFieldName, filterModelItem.filter)
                        return "$dbColumnName != :$classFieldName"
                    }

                    "contains" -> {
                        sqlParams.addValue(classFieldName, "%${filterModelItem.filter}%")
                        return "$dbColumnName $likeOrIlike :$classFieldName"
                    }

                    "notContains" -> {
                        sqlParams.addValue(classFieldName, "%${filterModelItem.filter}%")
                        return "$dbColumnName not $likeOrIlike :$classFieldName"
                    }

                    "startsWith" -> {
                        sqlParams.addValue(classFieldName, "${filterModelItem.filter}%")
                        return "$dbColumnName $likeOrIlike :$classFieldName"
                    }

                    "endsWith" -> {
                        sqlParams.addValue(classFieldName, "%${filterModelItem.filter}")
                        return "$dbColumnName $likeOrIlike :$classFieldName"
                    }

                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")

                }

            }

            "date" -> {

                when (filterType) {

                    "equals" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateFromParamName = "${dbColumnName}_dateFrom"

                        val dateToParamName = "${dbColumnName}_dateTo"
                        val plusOneDay = dateFrom.plusDays(1)

                        sqlParams.addValue(dateFromParamName, dateFrom)
                        sqlParams.addValue(dateToParamName, plusOneDay)

                        return "$dbColumnName >= :$dateFromParamName and $dbColumnName < :$dateToParamName"

                    }

                    "notEqual" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateFromParamName = "${dbColumnName}_dateFrom"

                        val dateToParamName = "${dbColumnName}_dateTo"
                        val plusOneDay = dateFrom.plusDays(1)

                        sqlParams.addValue(dateFromParamName, dateFrom)
                        sqlParams.addValue(dateToParamName, plusOneDay)

                        return "$dbColumnName < :$dateFromParamName or $dbColumnName >= :$dateToParamName"

                    }

                    "greaterThan" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateFromParamName = "${dbColumnName}_dateFrom"

                        sqlParams.addValue(dateFromParamName, dateFrom.plusDays(1))

                        return "$dbColumnName >= :$dateFromParamName"

                    }

                    "lessThan" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateFromParamName = "${dbColumnName}_dateFrom"

                        sqlParams.addValue(dateFromParamName, dateFrom)

                        return "$dbColumnName < :$dateFromParamName"

                    }

                    "inRange" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateTo = filterModelItem.dateTo!!
                        val plusOneDay = dateTo.plusDays(1)

                        val dateFromParamName = "${dbColumnName}_dateFrom"
                        val dateToParamName = "${dbColumnName}_dateTo"

                        sqlParams.addValue(dateFromParamName, dateFrom)
                        sqlParams.addValue(dateToParamName, plusOneDay)

                        return "$dbColumnName >= :$dateFromParamName and $dbColumnName < :$dateToParamName"

                    }

                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")

                }

            }

            "number" -> {

                when (filterType) {

                    "equals" -> {

                        val filter = filterModelItem.filter!!.toDouble()

                        sqlParams.addValue(classFieldName, filter)

                        return "$dbColumnName = :$classFieldName"

                    }

                    "notEqual" -> {

                        val filter = filterModelItem.filter!!.toDouble()

                        sqlParams.addValue(classFieldName, filter)

                        return "$dbColumnName != :$classFieldName"

                    }

                    "greaterThan" -> {

                        val filter = filterModelItem.filter!!.toDouble()

                        sqlParams.addValue(classFieldName, filter)

                        return "$dbColumnName > :$classFieldName"

                    }

                    "greaterThanOrEqual" -> {

                        val filter = filterModelItem.filter!!.toDouble()

                        sqlParams.addValue(classFieldName, filter)

                        return "$dbColumnName >= :$classFieldName"

                    }

                    "lessThan" -> {

                        val filter = filterModelItem.filter!!.toDouble()

                        sqlParams.addValue(classFieldName, filter)

                        return "$dbColumnName < :$classFieldName"

                    }

                    "lessThanOrEqual" -> {

                        val filter = filterModelItem.filter!!.toDouble()

                        sqlParams.addValue(classFieldName, filter)

                        return "$dbColumnName <= :$classFieldName"

                    }

                    "inRange" -> {

                        val filter = filterModelItem.filter!!.toDouble()
                        val filterTo = filterModelItem.filterTo!!.toDouble()

                        val fromParamName = "${dbColumnName}_from"
                        val toParamName = "${dbColumnName}_to"

                        sqlParams.addValue(fromParamName, filter)
                        sqlParams.addValue(toParamName, filterTo)

                        return "$dbColumnName >= :$fromParamName and $dbColumnName < :$toParamName"

                    }

                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")

                }

            }

            else -> throw IllegalArgumentException("Unknown filter field type [$fieldType]")

        }

    }


    fun buildOrderByClause(searchModel: SearchModel): String {

        val sortModelItems = searchModel.sortModel

        if (sortModelItems.isEmpty()) {
            return ""
        }

        val clauses = sortModelItems.joinToString(", ") { sortModelItem ->

            val dbColumnName = fieldNameToColumnNameMapper.invoke(sortModelItem.fieldPath)
            "$dbColumnName ${sortModelItem.sortDirection}"

        }

        return "order by $clauses"

    }


    fun buildOffsetAndLimitFor(searchModel: SearchModel): String {

        val offset = searchModel.startRow
        val endRow = searchModel.endRow
        val limit = if (endRow == null) -1 else (endRow - offset)

        val offsetText = if (offset < 1) {
            ""
        } else {
            " offset $offset"
        }

        val limitText = if (limit < 1) {
            ""
        } else {
            " limit $limit"
        }

        return "$limitText$offsetText"

    }

}

package org.maiaframework.jdbc.search

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode
import org.maiaframework.json.JsonNodeExtensions.getDouble
import org.maiaframework.json.JsonNodeExtensions.getLocalDate
import org.maiaframework.json.JsonNodeExtensions.getString
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.jdbc.SqlParams
import java.sql.Types

class AgGridSearchModelConverter_old(
    private val fieldNameToColumnNameMapper: (String) -> String
) {



    fun buildWhereClauseFor(
        filterModel: JsonNode,
        sqlParams: SqlParams,
        typeDiscriminatorExpression: String? = null
    ): String {

        val expressions = filterModel.map { filterModelItem ->
            buildExpressionFor(filterModelItem, sqlParams)
        }.plus(typeDiscriminatorExpression)
            .filterNotNull()

        return expressions.mapIndexed { index, expression ->

            val whereOrAnd = if (index == 0) "where" else "and"
            "$whereOrAnd $expression"

        }.joinToString(" ")

    }


    private fun buildExpressionFor(
        filterModelItemNode: JsonNode,
        sqlParams: SqlParams
    ): String {

        return filterModelItemNode.properties().mapIndexed { index, entry ->

            val classFieldName = entry.key
            val fieldNode = entry.value as ObjectNode

            if (fieldNode.has("operator")) {

                filterModelMultiConditionsToSql(classFieldName, fieldNode, sqlParams, index)

            } else {

                filterModelConditionToSql(classFieldName, fieldNode, sqlParams, sqlParamName = classFieldName)

            }

        }.joinToString(" ")

    }


    private fun filterModelMultiConditionsToSql(
        classFieldName: String,
        fieldFilterModel: ObjectNode,
        sqlParams: SqlParams,
        index: Int
    ): String {

        val operator = fieldFilterModel.getString("operator").lowercase()
        val sqlParamName = "${classFieldName}_$index"

        return when (operator) {

            "or" -> {

                val conditionSqls = buildWhereExpressionsFor(classFieldName, fieldFilterModel, sqlParams, sqlParamName)
                conditionSqls.joinToString(prefix = "(", separator = " or ", postfix = ")")

            }

            "and" -> {

                val conditionSqls = buildWhereExpressionsFor(classFieldName, fieldFilterModel, sqlParams, sqlParamName)
                conditionSqls.joinToString(prefix = "(", separator = " and ", postfix = ")")

            }

            else -> throw IllegalArgumentException("Unknown conditional operator '$operator'")

        }

    }


    private fun buildWhereExpressionsFor(
        fieldName: String,
        fieldNode: ObjectNode,
        sqlParams: SqlParams,
        sqlParamNamePrefix: String
    ): List<String> {


        val expressions = mutableListOf<String>()

        fieldNode.properties().forEachIndexed { index, conditionEntry: Map.Entry<String, JsonNode> ->

            val sqlParamName = "${sqlParamNamePrefix}_$index"

            val conditionNameOrOperator = conditionEntry.key

            if (conditionNameOrOperator != "operator") {

                val conditionFilterModelNode = conditionEntry.value as ObjectNode
                val conditionSql = filterModelConditionToSql(fieldName, conditionFilterModelNode, sqlParams, sqlParamName)

                expressions.add(conditionSql)

            }

        }

        return expressions

    }


    private fun filterModelConditionToSql(
        classFieldName: String,
        fieldFilterModel: ObjectNode,
        sqlParams: SqlParams,
        sqlParamName: String
    ): String {

        val dbColumnName = fieldNameToColumnNameMapper.invoke(classFieldName)

        val fieldType = fieldFilterModel.getString("fieldType")
        val filterType = fieldFilterModel.getString("filterType")

        when (fieldType) {

            "id" -> {
                sqlParams.addValue(sqlParamName, fieldFilterModel.getString("filter"), Types.OTHER)
                return "$dbColumnName = :$sqlParamName"
            }
            "text" -> {

                val likeOrIlike = "like" // TODO if (filterModelItem.caseSensitive) "like" else "ilike"

                when (filterType) {

                    "equals" -> {
                        sqlParams.addValue(sqlParamName, fieldFilterModel.getString("filter"))
                        return "$dbColumnName = :$sqlParamName"
                    }

                    "notEqual" -> {
                        sqlParams.addValue(sqlParamName, fieldFilterModel.getString("filter"))
                        return "$dbColumnName != :$sqlParamName"
                    }

                    "contains" -> {
                        sqlParams.addValue(sqlParamName, "%${fieldFilterModel.getString("filter")}%")
                        return "$dbColumnName $likeOrIlike :$sqlParamName"
                    }

                    "notContains" -> {
                        sqlParams.addValue(sqlParamName, "%${fieldFilterModel.getString("filter")}%")
                        return "$dbColumnName not $likeOrIlike :$sqlParamName"
                    }

                    "startsWith" -> {
                        sqlParams.addValue(sqlParamName, "${fieldFilterModel.getString("filter")}%")
                        return "$dbColumnName $likeOrIlike :$sqlParamName"
                    }

                    "endsWith" -> {
                        sqlParams.addValue(sqlParamName, "%${fieldFilterModel.getString("filter")}")
                        return "$dbColumnName $likeOrIlike :$sqlParamName"
                    }

                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")

                }

            }

            "date" -> {

                when (filterType) {

                    "equals" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateFromParamName = "${sqlParamName}_dateFrom"

                        val dateToParamName = "${sqlParamName}_dateTo"
                        val plusOneDay = dateFrom.plusDays(1)

                        sqlParams.addValue(dateFromParamName, dateFrom)
                        sqlParams.addValue(dateToParamName, plusOneDay)

                        return "$dbColumnName >= :$dateFromParamName and $dbColumnName < :$dateToParamName"

                    }

                    "notEqual" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateFromParamName = "${sqlParamName}_dateFrom"

                        val dateToParamName = "${sqlParamName}_dateTo"
                        val plusOneDay = dateFrom.plusDays(1)

                        sqlParams.addValue(dateFromParamName, dateFrom)
                        sqlParams.addValue(dateToParamName, plusOneDay)

                        return "$dbColumnName < :$dateFromParamName or $dbColumnName >= :$dateToParamName"

                    }

                    "greaterThan" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateFromParamName = "${sqlParamName}_dateFrom"

                        sqlParams.addValue(dateFromParamName, dateFrom.plusDays(1))

                        return "$dbColumnName >= :$dateFromParamName"

                    }

                    "lessThan" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateFromParamName = "${sqlParamName}_dateFrom"

                        sqlParams.addValue(dateFromParamName, dateFrom)

                        return "$dbColumnName < :$dateFromParamName"

                    }

                    "inRange" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateTo = fieldFilterModel.getLocalDate("dateTo")
                        val plusOneDay = dateTo.plusDays(1)

                        val dateFromParamName = "${sqlParamName}_dateFrom"
                        val dateToParamName = "${sqlParamName}_dateTo"

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

                        val filter = fieldFilterModel.getDouble("filter")

                        sqlParams.addValue(sqlParamName, filter)

                        return "$dbColumnName = :$sqlParamName"

                    }

                    "notEqual" -> {

                        val filter = fieldFilterModel.getDouble("filter")

                        sqlParams.addValue(sqlParamName, filter)

                        return "$dbColumnName != :$sqlParamName"

                    }

                    "greaterThan" -> {

                        val filter = fieldFilterModel.getDouble("filter")

                        sqlParams.addValue(sqlParamName, filter)

                        return "$dbColumnName > :$sqlParamName"

                    }

                    "greaterThanOrEqual" -> {

                        val filter = fieldFilterModel.getDouble("filter")

                        sqlParams.addValue(sqlParamName, filter)

                        return "$dbColumnName >= :$sqlParamName"

                    }

                    "lessThan" -> {

                        val filter = fieldFilterModel.getDouble("filter")

                        sqlParams.addValue(sqlParamName, filter)

                        return "$dbColumnName < :$sqlParamName"

                    }

                    "lessThanOrEqual" -> {

                        val filter = fieldFilterModel.getDouble("filter")

                        sqlParams.addValue(sqlParamName, filter)

                        return "$dbColumnName <= :$sqlParamName"

                    }

                    "inRange" -> {

                        val filter = fieldFilterModel.getDouble("filter")
                        val filterTo = fieldFilterModel.getDouble("filterTo")

                        val fromParamName = "${sqlParamName}_from"
                        val toParamName = "${sqlParamName}_to"

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


    fun buildOrderByClause(searchModel: AgGridSearchModel): String {

        val sortModelItems = searchModel.sortModel

        if (sortModelItems.isEmpty()) {
            return ""
        }

        val clauses = sortModelItems.joinToString(", ") { sortModelItem ->

            val dbColumnName = fieldNameToColumnNameMapper.invoke(sortModelItem.colId)
            "$dbColumnName ${sortModelItem.sort}"

        }

        return "order by $clauses"

    }


    fun buildOffsetAndLimitFor(searchModel: AgGridSearchModel): String {

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

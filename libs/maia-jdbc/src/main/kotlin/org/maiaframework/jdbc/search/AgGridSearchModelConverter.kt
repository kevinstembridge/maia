package org.maiaframework.jdbc.search

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode
import org.maiaframework.json.JsonNodeExtensions.getArrayNode
import org.maiaframework.json.JsonNodeExtensions.getDouble
import org.maiaframework.json.JsonNodeExtensions.getLocalDate
import org.maiaframework.json.JsonNodeExtensions.getString
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.jdbc.JdbcCompatibleType
import org.maiaframework.jdbc.SqlParams
import java.sql.Types

class AgGridSearchModelConverter(
    private val fieldNameToColumnNameMapper: (String) -> String,
    private val fieldNameToJdbcTypeMapper: (String) -> JdbcCompatibleType
) {


    fun buildWhereClauseFor(
        filterModel: JsonNode,
        sqlParams: SqlParams,
        typeDiscriminatorExpression: String? = null
    ): String {

        val expressions = filterModel
            .properties()
            .mapIndexed { index, filterModelEntry ->
                buildExpressionFor(
                    filterModelEntry.key,
                    filterModelEntry.value as ObjectNode,
                    sqlParams,
                    index
                )
            }.plus(typeDiscriminatorExpression)
                .filterNotNull()

        return expressions.mapIndexed { index, expression ->

            val whereOrAnd = if (index == 0) "where" else "and"
            "$whereOrAnd $expression"

        }.joinToString(" ")

    }


    private fun buildExpressionFor(
        nodeName: String,
        filterModelItemNode: ObjectNode,
        sqlParams: SqlParams,
        index: Int
    ): String {

        return if (filterModelItemNode.has("operator")) {

            filterModelMultiConditionsToSql(nodeName, filterModelItemNode, sqlParams, index)

        } else {

            filterModelConditionToSql(nodeName, filterModelItemNode, sqlParams, sqlParamName = nodeName)

        }

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

        fieldNode.getArrayNode("conditions").forEachIndexed { index, conditionEntry: JsonNode ->

            val sqlParamName = "${sqlParamNamePrefix}_$index"

            val conditionFilterModelNode = conditionEntry as ObjectNode
            val conditionSql = filterModelConditionToSql(fieldName, conditionFilterModelNode, sqlParams, sqlParamName)

            expressions.add(conditionSql)

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
        val jdbcCompatibleType = fieldNameToJdbcTypeMapper.invoke(classFieldName)

        val fieldType = fieldFilterModel.getString("filterType")

        if (fieldType == "set") {

            // A null entry is "(Blanks)": it also matches null rows. The empty string matches only if listed.
            val entries = fieldFilterModel.get("values")?.toList() ?: emptyList()
            val values = entries.filterNot { it.isNull }.map { it.asString() }
            val includesBlanks = entries.any { it.isNull }

            sqlParams.addListOfStrings(sqlParamName, values)
            val columnExpression = "$dbColumnName${dbColumnNameSuffixFor(jdbcCompatibleType)}"
            val anyExpression = "$columnExpression = any(:$sqlParamName)"

            return if (includesBlanks) "($anyExpression or $dbColumnName is null)" else anyExpression

        }

        val filterType = fieldFilterModel.getString("type")

        if (filterType == "blank" || filterType == "notBlank") {
            return blankConditionToSql(filterType == "blank", dbColumnName, jdbcCompatibleType)
        }

        when (fieldType) {

            "id" -> {
                sqlParams.addValue(sqlParamName, fieldFilterModel.getString("filter"), Types.OTHER)
                return "$dbColumnName = :$sqlParamName"
            }
            "text" -> {

                // AG Grid sends a boolean column filter as a text filter of type true/false with no filter value
                if (filterType == "true" || filterType == "false") {
                    if (jdbcCompatibleType != JdbcCompatibleType.boolean) {
                        throw IllegalArgumentException("Filter type [$filterType] is only valid for a boolean column, but [$classFieldName] is [$jdbcCompatibleType]")
                    }
                    return "$dbColumnName = $filterType"
                }

                val filterTerm = fieldFilterModel.getString("filter")

                if (jdbcCompatibleType == JdbcCompatibleType.text_array) {
                    return textArrayConditionToSql(filterType, dbColumnName, filterTerm, sqlParams, sqlParamName)
                }

                val dbColumnNameSuffix = dbColumnNameSuffixFor(jdbcCompatibleType)
                val likeOrIlike = "ilike" // TODO if (filterModelItem.caseSensitive) "like" else "ilike"

                when (filterType) {

                    "equals" -> {
                        sqlParams.addValue(sqlParamName, filterTerm, jdbcCompatibleType.sqlType)
                        return "$dbColumnName = :$sqlParamName"
                    }

                    "notEqual" -> {
                        sqlParams.addValue(sqlParamName, filterTerm, jdbcCompatibleType.sqlType)
                        return "$dbColumnName != :$sqlParamName"
                    }

                    "contains" -> {
                        sqlParams.addValue(sqlParamName, "%${escapeLike(filterTerm)}%", jdbcCompatibleType.sqlType)
                        return "$dbColumnName$dbColumnNameSuffix $likeOrIlike :$sqlParamName"
                    }

                    "notContains" -> {
                        sqlParams.addValue(sqlParamName, "%${escapeLike(filterTerm)}%", jdbcCompatibleType.sqlType)
                        return "$dbColumnName$dbColumnNameSuffix not $likeOrIlike :$sqlParamName"
                    }

                    "startsWith" -> {
                        sqlParams.addValue(sqlParamName, "${escapeLike(filterTerm)}%", jdbcCompatibleType.sqlType)
                        return "$dbColumnName$dbColumnNameSuffix $likeOrIlike :$sqlParamName"
                    }

                    "endsWith" -> {
                        sqlParams.addValue(sqlParamName, "%${escapeLike(filterTerm)}", jdbcCompatibleType.sqlType)
                        return "$dbColumnName$dbColumnNameSuffix $likeOrIlike :$sqlParamName"
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

                        return "($dbColumnName < :$dateFromParamName or $dbColumnName >= :$dateToParamName)"

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

    /**
     * Blank is null, plus the empty string for text columns and the empty array for array columns; notBlank is
     * the exact complement.
     */
    private fun blankConditionToSql(blank: Boolean, dbColumnName: String, jdbcCompatibleType: JdbcCompatibleType): String {

        return when (jdbcCompatibleType) {
            JdbcCompatibleType.text, JdbcCompatibleType.varchar ->
                if (blank) "($dbColumnName is null or $dbColumnName = '')" else "($dbColumnName is not null and $dbColumnName != '')"
            JdbcCompatibleType.text_array,
            JdbcCompatibleType.boolean_array,
            JdbcCompatibleType.decimal_array,
            JdbcCompatibleType.integer_array,
            JdbcCompatibleType.smallint_array,
            JdbcCompatibleType.timestamp_array,
            JdbcCompatibleType.uuid_array ->
                if (blank) "($dbColumnName is null or cardinality($dbColumnName) = 0)" else "($dbColumnName is not null and cardinality($dbColumnName) > 0)"
            else ->
                if (blank) "$dbColumnName is null" else "$dbColumnName is not null"
        }

    }


    private fun textArrayConditionToSql(
        filterType: String,
        dbColumnName: String,
        filterTerm: String,
        sqlParams: SqlParams,
        sqlParamName: String
    ): String {

        fun anyElementIlike(pattern: String): String {
            sqlParams.addValue(sqlParamName, pattern, Types.VARCHAR)
            return "exists (select 1 from unnest($dbColumnName) as element where element ilike :$sqlParamName)"
        }

        return when (filterType) {
            "equals" -> {
                sqlParams.addValue(sqlParamName, filterTerm, Types.VARCHAR)
                ":$sqlParamName = ANY($dbColumnName)"
            }
            "notEqual" -> {
                sqlParams.addValue(sqlParamName, filterTerm, Types.VARCHAR)
                "NOT (:$sqlParamName = ANY($dbColumnName))"
            }
            "contains" -> anyElementIlike("%${escapeLike(filterTerm)}%")
            "notContains" -> "not " + anyElementIlike("%${escapeLike(filterTerm)}%")
            "startsWith" -> anyElementIlike("${escapeLike(filterTerm)}%")
            "endsWith" -> anyElementIlike("%${escapeLike(filterTerm)}")
            else -> throw IllegalArgumentException("Unknown filter type [$filterType] for array column")
        }

    }


    /** Makes `%`, `_` and the escape character itself match literally in a (i)like pattern. */
    private fun escapeLike(term: String): String {

        return term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    }


    private fun dbColumnNameSuffixFor(jdbcCompatibleType: JdbcCompatibleType): String {

        return when (jdbcCompatibleType) {
            JdbcCompatibleType.bigint -> ""
            JdbcCompatibleType.text_array -> ""
            JdbcCompatibleType.boolean -> ""
            JdbcCompatibleType.boolean_array -> ""
            JdbcCompatibleType.date -> ""
            JdbcCompatibleType.decimal -> ""
            JdbcCompatibleType.decimal_array -> ""
            JdbcCompatibleType.integer -> ""
            JdbcCompatibleType.integer_array -> ""
            JdbcCompatibleType.jsonb -> ""
            JdbcCompatibleType.smallint -> ""
            JdbcCompatibleType.smallint_array -> ""
            JdbcCompatibleType.text -> ""
            JdbcCompatibleType.timestamp -> ""
            JdbcCompatibleType.timestamp_array -> ""
            JdbcCompatibleType.timestamp_with_time_zone -> ""
            JdbcCompatibleType.uuid -> "::text"
            JdbcCompatibleType.uuid_array -> ""
            JdbcCompatibleType.varchar -> ""
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

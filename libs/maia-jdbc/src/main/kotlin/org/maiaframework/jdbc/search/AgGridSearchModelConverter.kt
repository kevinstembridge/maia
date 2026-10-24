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
import java.time.LocalDate

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

        val conditions = fieldNode.getArrayNode("conditions")

        if (conditions.isEmpty) {
            throw IllegalArgumentException("A compound filter on [$fieldName] needs at least one condition")
        }

        conditions.forEachIndexed { index, conditionEntry: JsonNode ->

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

        val column = FilterColumn(
            classFieldName,
            fieldNameToColumnNameMapper.invoke(classFieldName),
            fieldNameToJdbcTypeMapper.invoke(classFieldName),
            sqlParams,
            sqlParamName
        )

        return when (val filterType = fieldFilterModel.getString("filterType")) {
            "set" -> setConditionToSql(fieldFilterModel, column)
            "id" -> idConditionToSql(fieldFilterModel, column)
            "text", "number", "date" -> {
                val type = fieldFilterModel.getString("type")
                // Blank is only meaningful for the filter types that support it.
                if (type == "blank" || type == "notBlank") {
                    blankConditionToSql(type == "blank", column.dbColumnName, column.jdbcCompatibleType)
                } else {
                    when (filterType) {
                        "text" -> textConditionToSql(type, fieldFilterModel, column)
                        "number" -> numberConditionToSql(type, fieldFilterModel, column)
                        else -> dateConditionToSql(type, fieldFilterModel, column)
                    }
                }
            }
            else -> throw unknownFilterType("filter field type", filterType)
        }

    }


    /** The column a single filter condition applies to, and where its parameters are bound. */
    private class FilterColumn(
        val classFieldName: String,
        val dbColumnName: String,
        val jdbcCompatibleType: JdbcCompatibleType,
        val sqlParams: SqlParams,
        val sqlParamName: String
    ) {

        fun paramName(suffix: String): String = "${sqlParamName}_$suffix"

    }


    private fun unknownFilterType(kind: String, value: String, suffix: String = ""): IllegalArgumentException {

        return IllegalArgumentException("Unknown $kind [$value]$suffix")

    }


    private fun setConditionToSql(fieldFilterModel: ObjectNode, column: FilterColumn): String {

        // A null entry is "(Blanks)": it also matches null rows. The empty string matches only if listed.
        val entries = fieldFilterModel.get("values")?.toList() ?: emptyList()
        val values = entries.filterNot { it.isNull }.map { it.asString() }
        val includesBlanks = entries.any { it.isNull }

        column.sqlParams.addListOfStrings(column.sqlParamName, values)
        val matchExpression = setMatchExpression(column.dbColumnName, column.jdbcCompatibleType, column.sqlParamName)

        if (!includesBlanks) {
            return matchExpression
        }

        // For a scalar column "(Blanks)" is null only: the empty string matches only if listed.
        val blankExpression = if (column.jdbcCompatibleType.sqlType == Types.ARRAY) {
            blankConditionToSql(true, column.dbColumnName, column.jdbcCompatibleType)
        } else {
            "${column.dbColumnName} is null"
        }

        return "($matchExpression or $blankExpression)"

    }


    private fun idConditionToSql(fieldFilterModel: ObjectNode, column: FilterColumn): String {

        // An id filter needs no type, but if one is sent it must be "equals".
        val idFilterType = fieldFilterModel.get("type")?.takeUnless { it.isNull }?.asString()
        if (idFilterType != null && idFilterType != "equals") {
            throw unknownFilterType("filter type", idFilterType, " for an id filter")
        }

        column.sqlParams.addValue(column.sqlParamName, fieldFilterModel.getString("filter"), Types.OTHER)
        return "${column.dbColumnName} = :${column.sqlParamName}"

    }


    private fun textConditionToSql(type: String, fieldFilterModel: ObjectNode, column: FilterColumn): String {

        // AG Grid sends a boolean column filter as a text filter of type true/false with no filter value
        if (type == "true" || type == "false") {
            if (column.jdbcCompatibleType != JdbcCompatibleType.boolean) {
                throw IllegalArgumentException("Filter type [$type] is only valid for a boolean column, but [${column.classFieldName}] is [${column.jdbcCompatibleType}]")
            }
            return "${column.dbColumnName} = $type"
        }

        val filterTerm = fieldFilterModel.getString("filter")

        if (column.jdbcCompatibleType == JdbcCompatibleType.text_array) {
            return textArrayConditionToSql(type, filterTerm, column)
        }

        val columnExpression = "${column.dbColumnName}${dbColumnNameSuffixFor(column.jdbcCompatibleType)}"

        return when (type) {
            "equals" -> "${column.dbColumnName} = ${textParam(column, filterTerm)}"
            "notEqual" -> "${column.dbColumnName} != ${textParam(column, filterTerm)}"
            "contains" -> "$columnExpression ilike ${textParam(column, "%${escapeLike(filterTerm)}%")}"
            "notContains" -> "$columnExpression not ilike ${textParam(column, "%${escapeLike(filterTerm)}%")}"
            "startsWith" -> "$columnExpression ilike ${textParam(column, "${escapeLike(filterTerm)}%")}"
            "endsWith" -> "$columnExpression ilike ${textParam(column, "%${escapeLike(filterTerm)}")}"
            else -> throw unknownFilterType("filter type", type)
        }

    }


    /** Binds [value] as the column's own JDBC type and returns its placeholder. */
    private fun textParam(column: FilterColumn, value: String): String {

        column.sqlParams.addValue(column.sqlParamName, value, column.jdbcCompatibleType.sqlType)
        return ":${column.sqlParamName}"

    }


    private fun dateConditionToSql(type: String, fieldFilterModel: ObjectNode, column: FilterColumn): String {

        val col = column.dbColumnName
        val fromName = column.paramName("dateFrom")
        val toName = column.paramName("dateTo")
        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")

        return when (type) {

            "equals" -> {
                bindDateRange(column, dateFrom, dateFrom.plusDays(1))
                "$col >= :$fromName and $col < :$toName"
            }

            "notEqual" -> {
                bindDateRange(column, dateFrom, dateFrom.plusDays(1))
                "($col < :$fromName or $col >= :$toName)"
            }

            "greaterThan" -> {
                column.sqlParams.addValue(fromName, dateFrom.plusDays(1))
                "$col >= :$fromName"
            }

            "lessThan" -> {
                column.sqlParams.addValue(fromName, dateFrom)
                "$col < :$fromName"
            }

            "inRange" -> {
                bindDateRange(column, dateFrom, fieldFilterModel.getLocalDate("dateTo").plusDays(1))
                "$col >= :$fromName and $col < :$toName"
            }

            else -> throw unknownFilterType("filter type", type)

        }

    }


    private fun bindDateRange(column: FilterColumn, from: LocalDate, toExclusive: LocalDate) {

        column.sqlParams.addValue(column.paramName("dateFrom"), from)
        column.sqlParams.addValue(column.paramName("dateTo"), toExclusive)

    }


    private fun numberConditionToSql(type: String, fieldFilterModel: ObjectNode, column: FilterColumn): String {

        val col = column.dbColumnName

        fun compare(operator: String): String {
            column.sqlParams.addValue(column.sqlParamName, fieldFilterModel.getDouble("filter"))
            return "$col $operator :${column.sqlParamName}"
        }

        return when (type) {
            "equals" -> compare("=")
            "notEqual" -> compare("!=")
            "greaterThan" -> compare(">")
            "greaterThanOrEqual" -> compare(">=")
            "lessThan" -> compare("<")
            "lessThanOrEqual" -> compare("<=")
            "inRange" -> {
                val fromName = column.paramName("from")
                val toName = column.paramName("to")
                column.sqlParams.addValue(fromName, fieldFilterModel.getDouble("filter"))
                column.sqlParams.addValue(toName, fieldFilterModel.getDouble("filterTo"))
                "$col >= :$fromName and $col < :$toName"
            }
            else -> throw unknownFilterType("filter type", type)
        }

    }


    /**
     * The values are bound as text and compared in the column's own type, so int, decimal, date, instant and
     * boolean columns match like they do in Elasticsearch. An array column matches if any element is listed.
     */
    private fun setMatchExpression(dbColumnName: String, jdbcCompatibleType: JdbcCompatibleType, sqlParamName: String): String {

        return when (jdbcCompatibleType) {
            JdbcCompatibleType.text, JdbcCompatibleType.varchar -> "$dbColumnName = any(:$sqlParamName)"
            JdbcCompatibleType.uuid -> "$dbColumnName::text = any(:$sqlParamName)"
            JdbcCompatibleType.text_array,
            JdbcCompatibleType.boolean_array,
            JdbcCompatibleType.decimal_array,
            JdbcCompatibleType.integer_array,
            JdbcCompatibleType.smallint_array,
            JdbcCompatibleType.timestamp_array,
            JdbcCompatibleType.uuid_array ->
                "$dbColumnName && cast(:$sqlParamName as ${jdbcCompatibleType.postgresDataType})"
            else -> "$dbColumnName = any(cast(:$sqlParamName as ${jdbcCompatibleType.postgresDataType}[]))"
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


    private fun textArrayConditionToSql(type: String, filterTerm: String, column: FilterColumn): String {

        val col = column.dbColumnName
        val param = ":${column.sqlParamName}"

        fun anyElementIlike(pattern: String): String {
            column.sqlParams.addValue(column.sqlParamName, pattern, Types.VARCHAR)
            return "exists (select 1 from unnest($col) as element where element ilike $param)"
        }

        fun bindTerm() {
            column.sqlParams.addValue(column.sqlParamName, filterTerm, Types.VARCHAR)
        }

        return when (type) {
            "equals" -> {
                bindTerm()
                "$param = ANY($col)"
            }
            "notEqual" -> {
                bindTerm()
                // A null array counts as empty, so it is included, like notContains and Elasticsearch do.
                "NOT ($param = ANY(coalesce($col, '{}')))"
            }
            "contains" -> anyElementIlike("%${escapeLike(filterTerm)}%")
            "notContains" -> "not " + anyElementIlike("%${escapeLike(filterTerm)}%")
            "startsWith" -> anyElementIlike("${escapeLike(filterTerm)}%")
            "endsWith" -> anyElementIlike("%${escapeLike(filterTerm)}")
            else -> throw unknownFilterType("filter type", type, " for array column")
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

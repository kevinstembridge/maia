package org.maiaframework.showcase.filterparity


// Builders for the raw Filter model JSON used by the scenarios, so a scenario reads as its intent.


fun textCondition(type: String, value: String): String =
    """{"filterType": "text", "type": "$type", "filter": "$value"}"""


fun numberCondition(type: String, filter: Number): String =
    """{"filterType": "number", "type": "$type", "filter": $filter}"""


fun textEntry(field: String, type: String, value: String): String =
    """"$field": ${textCondition(type, value)}"""


fun numberEntry(field: String, type: String, filter: Number): String =
    """"$field": ${numberCondition(type, filter)}"""


fun compoundEntry(field: String, operator: String, vararg conditions: String): String =
    """"$field": {"filterType": "multi", "operator": "$operator", "conditions": [${conditions.joinToString(", ")}]}"""


fun compound(field: String, operator: String, vararg conditions: String): String =
    "{${compoundEntry(field, operator, *conditions)}}"


fun blankFilter(field: String, filterType: String, type: String): String =
    """{"$field": {"filterType": "$filterType", "type": "$type"}}"""


/** The value of a date filter entry, without the field name. */
fun dateFilter(type: String, dateFrom: String): String =
    """{"filterType": "date", "type": "$type", "dateFrom": "$dateFrom"}"""


/** The value of a date inRange filter entry, without the field name. */
fun dateRange(dateFrom: String, dateTo: String): String =
    """{"filterType": "date", "type": "inRange", "dateFrom": "$dateFrom", "dateTo": "$dateTo"}"""


/** [values] are raw JSON literals, e.g. `"\"a\""` or `"null"`. */
fun setFilter(field: String, vararg values: String): String =
    """{"$field": {"filterType": "set", "values": [${values.joinToString(",")}]}}"""


fun textFilter(field: String, type: String, value: String): String =
    """{"$field": {"filterType": "text", "type": "$type", "filter": "$value"}}"""


fun textEquals(field: String, value: String): String = textFilter(field, "equals", value)


fun numberFilter(field: String, type: String, filter: Number, filterTo: Number? = null): String {
    val to = if (filterTo == null) "" else """, "filterTo": $filterTo"""
    return """{"$field": {"filterType": "number", "type": "$type", "filter": $filter$to}}"""
}


fun booleanEntry(field: String, value: Boolean): String =
    """"$field": {"filterType": "text", "type": "$value"}"""


fun booleanFilter(field: String, value: Boolean): String =
    "{${booleanEntry(field, value)}}"


fun idEquals(field: String, value: String): String =
    """{"$field": {"filterType": "id", "type": "equals", "filter": "$value"}}"""


/**
 * Expected keys computed from the seed, for order-sensitive filters: filler rows added by other scenarios carry
 * the [SeedRow] defaults (0 / 0.0), so a hardcoded list would break whenever a row is added.
 */
fun keysWhere(predicate: (SeedRow) -> Boolean): Set<String> =
    AgGridFilterSeed.rows.filter(predicate).map { it.key }.toSet()

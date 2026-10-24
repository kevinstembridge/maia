package org.maiaframework.showcase.filterparity


/**
 * A Filter model (as AG Grid sends it, see GLOSSARY.md) and the seed rows it must return.
 *
 * [filterModelJson] is the raw JSON of the `filterModel`, keyed by DTO field name.
 * [expectedKeys] are [SeedRow.key]s; order is irrelevant. The same expectation is asserted against Postgres
 * and Elasticsearch, so a scenario passing on one backend and failing on the other is a parity bug.
 */
data class FilterScenario(
    val name: String,
    val filterModelJson: String,
    val expectedKeys: Set<String>,
)


/**
 * The single shared scenario table. To cover a new filter, add entries here (and seed rows to
 * [AgGridFilterSeed] if the existing rows do not exercise it); no test code changes are needed.
 */
object AgGridFilterScenarios {

    val all: List<FilterScenario> = listOf(

        FilterScenario(
            name = "text equals matches the exact value only",
            filterModelJson = textEquals("textRequired", "alpha"),
            expectedKeys = setOf("alpha"),
        ),

        FilterScenario(
            name = "text equals is case sensitive",
            filterModelJson = textEquals("textRequired", "Alpha"),
            expectedKeys = setOf("mixed-case"),
        ),

        FilterScenario(
            name = "text equals treats star literally",
            filterModelJson = textEquals("textRequired", "alpha*"),
            expectedKeys = setOf("star"),
        ),

        FilterScenario(
            name = "text equals treats question mark literally",
            filterModelJson = textEquals("textRequired", "alp?a"),
            expectedKeys = setOf("question"),
        ),

        FilterScenario(
            name = "text equals matches a multi-word value exactly",
            filterModelJson = textEquals("textRequired", "alpha beta"),
            expectedKeys = setOf("multi-word"),
        ),

        FilterScenario(
            name = "text equals on a nullable column skips nulls",
            filterModelJson = textEquals("textNullable", "alpha"),
            expectedKeys = setOf("alpha"),
        ),

        FilterScenario(
            name = "text equals with no match returns nothing",
            filterModelJson = textEquals("textRequired", "gamma"),
            expectedKeys = emptySet(),
        ),

        // Date filters. Pivot day is 2024-03-15; see the date rows in AgGridFilterSeed.
        // The notEqual and lessThan scenarios use the nullable columns so only the date rows match: the
        // empty-string seed row (on 2024-01-01) cannot be read back from Elasticsearch, because the empty
        // textRequired is not indexed and the document fails to deserialize.

        FilterScenario(
            name = "date notEqual combined with a text filter applies both",
            filterModelJson = """{"dateRequired": ${dateFilter("notEqual", "2024-03-15")}, "textRequired": {"filterType": "text", "type": "equals", "filter": "alpha"}}""",
            expectedKeys = setOf("alpha"),
        ),

        FilterScenario(
            name = "localdate equals matches the whole day",
            filterModelJson = """{"dateRequired": ${dateFilter("equals", "2024-03-15")}}""",
            expectedKeys = setOf("date-at-midnight", "date-end-of-day"),
        ),

        FilterScenario(
            name = "instant equals matches the whole UTC day",
            filterModelJson = """{"instantRequired": ${dateFilter("equals", "2024-03-15")}}""",
            expectedKeys = setOf("date-at-midnight", "date-end-of-day"),
        ),

        FilterScenario(
            name = "localdate notEqual excludes the day",
            filterModelJson = """{"dateNullable": ${dateFilter("notEqual", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight", "date-after-midnight"),
        ),

        FilterScenario(
            name = "instant notEqual excludes the UTC day",
            filterModelJson = """{"instantNullable": ${dateFilter("notEqual", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight", "date-after-midnight"),
        ),

        FilterScenario(
            name = "localdate greaterThan starts at the next day",
            filterModelJson = """{"dateRequired": ${dateFilter("greaterThan", "2024-03-15")}}""",
            expectedKeys = setOf("date-after-midnight"),
        ),

        FilterScenario(
            name = "instant greaterThan starts at the next UTC midnight",
            filterModelJson = """{"instantRequired": ${dateFilter("greaterThan", "2024-03-15")}}""",
            expectedKeys = setOf("date-after-midnight"),
        ),

        FilterScenario(
            name = "localdate lessThan ends before the day",
            filterModelJson = """{"dateNullable": ${dateFilter("lessThan", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight"),
        ),

        FilterScenario(
            name = "instant lessThan ends before UTC midnight of the day",
            filterModelJson = """{"instantNullable": ${dateFilter("lessThan", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight"),
        ),

        FilterScenario(
            name = "localdate inRange includes both end days",
            filterModelJson = """{"dateRequired": ${dateRange("2024-03-15", "2024-03-16")}}""",
            expectedKeys = setOf("date-at-midnight", "date-end-of-day", "date-after-midnight"),
        ),

        FilterScenario(
            name = "instant inRange includes both end days",
            filterModelJson = """{"instantRequired": ${dateRange("2024-03-14", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight", "date-at-midnight", "date-end-of-day"),
        ),

        FilterScenario(
            name = "date equals on a nullable column skips nulls",
            filterModelJson = """{"dateNullable": ${dateFilter("equals", "2024-03-15")}}""",
            expectedKeys = setOf("date-at-midnight"),
        ),

        FilterScenario(
            name = "date notEqual on a nullable column skips nulls",
            filterModelJson = """{"dateNullable": ${dateFilter("notEqual", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight", "date-after-midnight"),
        ),

        FilterScenario(
            name = "instant notEqual on a nullable column skips nulls",
            filterModelJson = """{"instantNullable": ${dateFilter("notEqual", "2024-03-15")}}""",
            expectedKeys = setOf("date-before-midnight", "date-after-midnight"),
        ),

    )


    private fun dateFilter(type: String, dateFrom: String): String =
        """{"filterType": "date", "type": "$type", "dateFrom": "$dateFrom"}"""


    private fun dateRange(dateFrom: String, dateTo: String): String =
        """{"filterType": "date", "type": "inRange", "dateFrom": "$dateFrom", "dateTo": "$dateTo"}"""


    private fun textEquals(field: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "equals", "filter": "$value"}}"""

}

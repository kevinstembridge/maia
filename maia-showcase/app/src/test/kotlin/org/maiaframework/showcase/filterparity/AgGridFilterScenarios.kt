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

        // ---- number filters ----

        FilterScenario(
            name = "number equals on an int column matches the exact value",
            filterModelJson = numberFilter("intRequired", "equals", 3),
            expectedKeys = setOf("upper-case"),
        ),

        FilterScenario(
            name = "number equals on a decimal column matches the exact value",
            filterModelJson = numberFilter("decimalRequired", "equals", 3.5),
            expectedKeys = setOf("upper-case"),
        ),

        FilterScenario(
            name = "number equals with no match returns nothing",
            filterModelJson = numberFilter("intRequired", "equals", 99),
            expectedKeys = emptySet(),
        ),

        FilterScenario(
            name = "number equals on a nullable column skips nulls",
            filterModelJson = numberFilter("intNullable", "equals", 6),
            expectedKeys = setOf("beta"),
        ),

        FilterScenario(
            name = "number notEqual on an int column excludes the value",
            filterModelJson = numberFilter("intRequired", "notEqual", 3),
            expectedKeys = setOf("alpha", "mixed-case", "star", "question", "beta", "multi-word", "empty-string"),
        ),

        FilterScenario(
            name = "number notEqual on a decimal column excludes the value",
            filterModelJson = numberFilter("decimalRequired", "notEqual", 3.5),
            expectedKeys = setOf("alpha", "mixed-case", "star", "question", "beta", "multi-word", "empty-string"),
        ),

        FilterScenario(
            name = "number notEqual on a nullable column excludes nulls",
            filterModelJson = numberFilter("intNullable", "notEqual", 1),
            expectedKeys = setOf("beta"),
        ),

        FilterScenario(
            name = "number greaterThan on an int column is exclusive",
            filterModelJson = numberFilter("intRequired", "greaterThan", 6),
            expectedKeys = setOf("multi-word", "empty-string"),
        ),

        FilterScenario(
            name = "number greaterThan on a decimal column is exclusive",
            filterModelJson = numberFilter("decimalRequired", "greaterThan", 6.5),
            expectedKeys = setOf("multi-word", "empty-string"),
        ),

        FilterScenario(
            name = "number greaterThan on a nullable column skips nulls",
            filterModelJson = numberFilter("intNullable", "greaterThan", 0),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "number greaterThanOrEqual on an int column is inclusive",
            filterModelJson = numberFilter("intRequired", "greaterThanOrEqual", 6),
            expectedKeys = setOf("beta", "multi-word", "empty-string"),
        ),

        FilterScenario(
            name = "number greaterThanOrEqual on a decimal column is inclusive",
            filterModelJson = numberFilter("decimalRequired", "greaterThanOrEqual", 6.5),
            expectedKeys = setOf("beta", "multi-word", "empty-string"),
        ),

        FilterScenario(
            name = "number lessThan on an int column is exclusive",
            filterModelJson = numberFilter("intRequired", "lessThan", 3),
            expectedKeys = setOf("alpha", "mixed-case"),
        ),

        FilterScenario(
            name = "number lessThan on a decimal column is exclusive",
            filterModelJson = numberFilter("decimalRequired", "lessThan", 3.5),
            expectedKeys = setOf("alpha", "mixed-case"),
        ),

        FilterScenario(
            name = "number lessThan on a nullable column skips nulls",
            filterModelJson = numberFilter("intNullable", "lessThan", 100),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "number lessThanOrEqual on an int column is inclusive",
            filterModelJson = numberFilter("intRequired", "lessThanOrEqual", 3),
            expectedKeys = setOf("alpha", "mixed-case", "upper-case"),
        ),

        FilterScenario(
            name = "number lessThanOrEqual on a decimal column is inclusive",
            filterModelJson = numberFilter("decimalRequired", "lessThanOrEqual", 3.5),
            expectedKeys = setOf("alpha", "mixed-case", "upper-case"),
        ),

        FilterScenario(
            name = "number inRange on an int column includes the lower bound and excludes the upper bound",
            filterModelJson = numberFilter("intRequired", "inRange", 3, 5),
            expectedKeys = setOf("upper-case", "star"),
        ),

        FilterScenario(
            name = "number inRange on a decimal column includes the lower bound and excludes the upper bound",
            filterModelJson = numberFilter("decimalRequired", "inRange", 3.5, 5.5),
            expectedKeys = setOf("upper-case", "star"),
        ),

        FilterScenario(
            name = "number inRange on a nullable column skips nulls",
            filterModelJson = numberFilter("intNullable", "inRange", 0, 100),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "number inRange with equal bounds returns nothing",
            filterModelJson = numberFilter("intRequired", "inRange", 3, 3),
            expectedKeys = emptySet(),
        ),

    )


    private fun numberFilter(field: String, type: String, filter: Number, filterTo: Number? = null): String {
        val to = if (filterTo == null) "" else """, "filterTo": $filterTo"""
        return """{"$field": {"filterType": "number", "type": "$type", "filter": $filter$to}}"""
    }


    private fun textEquals(field: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "equals", "filter": "$value"}}"""

}

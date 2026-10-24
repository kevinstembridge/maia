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

        FilterScenario(
            name = "set matches any listed value",
            filterModelJson = setFilter("textRequired", "\"alpha\"", "\"beta\""),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "set null entry also matches Blank rows",
            filterModelJson = setFilter("textNullable", "null", "\"beta\""),
            expectedKeys = setOf("mixed-case", "beta"),
        ),

        FilterScenario(
            name = "set with only a null entry matches null but not the empty string",
            filterModelJson = setFilter("textNullable", "null"),
            expectedKeys = setOf("mixed-case"),
        ),

        FilterScenario(
            name = "set empty list matches nothing",
            filterModelJson = setFilter("textNullable"),
            expectedKeys = emptySet(),
        ),

        FilterScenario(
            name = "set matches the empty string only when listed",
            filterModelJson = withoutEmptyStringRow(setFilter("textNullable", "\"\"")),
            expectedKeys = setOf("upper-case"),
        ),

        FilterScenario(
            name = "set with null and empty string listed matches both",
            filterModelJson = withoutEmptyStringRow(setFilter("textNullable", "null", "\"\"")),
            expectedKeys = setOf("mixed-case", "upper-case"),
        ),

    )


    /** [values] are raw JSON literals, e.g. `"\"a\""` or `"null"`. */
    private fun setFilter(field: String, vararg values: String): String =
        """{"$field": {"filterType": "set", "values": [${values.joinToString(",")}]}}"""


    /**
     * ANDs a second condition on `textRequired` that excludes the "empty-string" seed row. The app's
     * StringTrimmingDeserializer turns "" into null, so the ES client cannot decode that row (its
     * `textRequired` is a non-null String) and any ES search returning it fails. The restriction lists every
     * other `textRequired` value, so the row's `textNullable` of "" is still exercised via other rows.
     */
    private fun withoutEmptyStringRow(filterModelJson: String): String {
        val others = AgGridFilterSeed.rows.map { it.textRequired }.filter { it.isNotEmpty() }
        val restriction = setFilter("textRequired", *others.map { "\"$it\"" }.toTypedArray())
        return filterModelJson.dropLast(1) + "," + restriction.drop(1)
    }


    private fun textEquals(field: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "equals", "filter": "$value"}}"""

}

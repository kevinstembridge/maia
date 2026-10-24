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

    ).plus(textFilterScenarios())


    private fun textFilterScenarios(): List<FilterScenario> {

        val allKeys = AgGridFilterSeed.rows.map { it.key }.toSet()
        val alphaLike = setOf("alpha", "mixed-case", "upper-case", "star", "multi-word")

        return listOf(

            FilterScenario("text notEqual excludes the exact value only", textFilter("textRequired", "notEqual", "alpha"), allKeys - "alpha"),
            FilterScenario("text notEqual is case sensitive", textFilter("textRequired", "notEqual", "Alpha"), allKeys - "mixed-case"),

            FilterScenario("text contains ignores case", textFilter("textRequired", "contains", "ALPHA"), alphaLike),
            FilterScenario("text contains treats star literally", textFilter("textRequired", "contains", "*"), setOf("star")),
            FilterScenario("text contains treats question mark literally", textFilter("textRequired", "contains", "?"), setOf("question")),
            FilterScenario("text contains treats percent literally", textFilter("textRequired", "contains", "%"), setOf("percent")),
            FilterScenario("text contains treats underscore literally", textFilter("textRequired", "contains", "_"), setOf("underscore")),
            FilterScenario("text contains on a nullable column skips nulls", textFilter("textNullable", "contains", "AL"), setOf("alpha", "star", "question")),

            FilterScenario("text notContains ignores case", textFilter("textRequired", "notContains", "ALPHA"), allKeys - alphaLike),
            FilterScenario("text notContains treats star literally", textFilter("textRequired", "notContains", "*"), allKeys - "star"),
            FilterScenario("text notContains treats percent literally", textFilter("textRequired", "notContains", "%"), allKeys - "percent"),

            FilterScenario("text startsWith ignores case", textFilter("textRequired", "startsWith", "ALPHA"), alphaLike),
            FilterScenario("text startsWith treats percent literally", textFilter("textRequired", "startsWith", "5%o"), setOf("percent")),
            FilterScenario("text startsWith treats question mark literally", textFilter("textRequired", "startsWith", "alp?"), setOf("question")),
            FilterScenario("text startsWith treats star literally", textFilter("textRequired", "startsWith", "al*"), emptySet()),

            FilterScenario("text endsWith ignores case", textFilter("textRequired", "endsWith", "HA"), setOf("alpha", "mixed-case", "upper-case")),
            FilterScenario("text endsWith treats star literally", textFilter("textRequired", "endsWith", "*"), setOf("star")),
            FilterScenario("text endsWith treats question mark literally", textFilter("textRequired", "endsWith", "?a"), setOf("question")),
            FilterScenario("text endsWith treats underscore literally", textFilter("textRequired", "endsWith", "a_c"), setOf("underscore")),

            FilterScenario("array text equals matches any element exactly", textFilter("tags", "equals", "apple"), setOf("tagged-one")),
            FilterScenario("array text equals is case sensitive", textFilter("tags", "equals", "Apple"), emptySet()),
            FilterScenario("array text notEqual excludes rows with a matching element", textFilter("tags", "notEqual", "a"), allKeys - "alpha"),
            FilterScenario("array text contains matches any element ignoring case", textFilter("tags", "contains", "ANAN"), setOf("tagged-one")),
            FilterScenario("array text contains treats star literally", textFilter("tags", "contains", "*"), setOf("tagged-two")),
            FilterScenario("array text notContains excludes rows with a matching element", textFilter("tags", "notContains", "APP"), allKeys - "tagged-one"),
            FilterScenario("array text startsWith matches any element ignoring case", textFilter("tags", "startsWith", "b"), setOf("alpha", "tagged-one")),
            FilterScenario("array text endsWith treats question mark literally", textFilter("tags", "endsWith", "?x"), setOf("tagged-two")),

        )

    }


    private fun textFilter(field: String, type: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "$type", "filter": "$value"}}"""


    private fun textEquals(field: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "equals", "filter": "$value"}}"""

}

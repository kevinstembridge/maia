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
            name = "boolean true matches rows where the flag is true",
            filterModelJson = booleanFilter("flagRequired", true),
            expectedKeys = setOf("alpha"),
        ),

        FilterScenario(
            // Narrowed with a contains filter: the empty-string seed row cannot be read back from Elasticsearch
            // (the ES doc is indexed without its empty textRequired), so it must not be in any result set.
            name = "boolean false matches rows where the flag is false",
            filterModelJson = """{${booleanEntry("flagRequired", false)}, "textRequired": {"filterType": "text", "type": "contains", "filter": "alpha"}}""",
            expectedKeys = setOf("mixed-case", "upper-case", "star", "multi-word"),
        ),

        FilterScenario(
            name = "boolean true on a nullable column skips nulls",
            filterModelJson = booleanFilter("flagNullable", true),
            expectedKeys = setOf("alpha"),
        ),

        FilterScenario(
            name = "boolean false on a nullable column skips nulls",
            filterModelJson = booleanFilter("flagNullable", false),
            expectedKeys = setOf("beta"),
        ),

        FilterScenario(
            name = "id matches the exact id only",
            filterModelJson = idEquals("id", AgGridFilterSeed.idOf("beta").value),
            expectedKeys = setOf("beta"),
        ),

        FilterScenario(
            name = "id with no match returns nothing",
            filterModelJson = idEquals("id", "00000000-0000-0000-0000-000000000000"),
            expectedKeys = emptySet(),
        ),

    )


    private fun textEquals(field: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "equals", "filter": "$value"}}"""


    private fun booleanEntry(field: String, value: Boolean): String =
        """"$field": {"filterType": "text", "type": "$value"}"""


    private fun booleanFilter(field: String, value: Boolean): String =
        "{${booleanEntry(field, value)}}"


    private fun idEquals(field: String, value: String): String =
        """{"$field": {"filterType": "id", "type": "equals", "filter": "$value"}}"""

}

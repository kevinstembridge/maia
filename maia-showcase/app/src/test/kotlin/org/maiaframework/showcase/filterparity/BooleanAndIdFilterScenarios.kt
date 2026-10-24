package org.maiaframework.showcase.filterparity


/** Boolean filters (sent as a text filter of type true/false) and id filters. */
object BooleanAndIdFilterScenarios {

    val all: List<FilterScenario> = listOf(

        FilterScenario(
            name = "id filter without a type still matches",
            filterModelJson = """{"id": {"filterType": "id", "filter": "${AgGridFilterSeed.idOf("beta").value}"}}""",
            expectedKeys = setOf("beta"),
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

}

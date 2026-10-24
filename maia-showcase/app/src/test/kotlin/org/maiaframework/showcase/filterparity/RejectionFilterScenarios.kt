package org.maiaframework.showcase.filterparity


/** Unrecognised, unsupported or malformed Filter models are rejected by both backends with IllegalArgumentException, never matched loosely. */
object RejectionFilterScenarios {

    val all: List<FilterScenario> = listOf(

        FilterScenario(
            name = "unknown filterType is rejected",
            filterModelJson = """{"textRequired": {"filterType": "bogus", "type": "equals", "filter": "alpha"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "missing filterType is rejected",
            filterModelJson = """{"textRequired": {"type": "equals", "filter": "alpha"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "bigint filterType is rejected",
            filterModelJson = """{"intRequired": {"filterType": "bigint", "type": "equals", "filter": "3"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "multi filterType is rejected",
            filterModelJson = """{"textRequired": {"filterType": "multi", "filterModels": [null, {"filterType": "text", "type": "equals", "filter": "alpha"}]}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "boolean as its own filterType is rejected",
            filterModelJson = """{"flagRequired": {"filterType": "boolean", "type": "true"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "unknown text type is rejected",
            filterModelJson = textFilter("textRequired", "bogus", "alpha"),
            expectsRejection = true,
        ),

        FilterScenario(
            name = "text filter with no type is rejected",
            filterModelJson = """{"textRequired": {"filterType": "text", "filter": "alpha"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "unknown number type is rejected",
            filterModelJson = numberFilter("intRequired", "bogus", 3),
            expectsRejection = true,
        ),

        FilterScenario(
            name = "number filter with no type is rejected",
            filterModelJson = """{"intRequired": {"filterType": "number", "filter": 3}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "unknown date type is rejected",
            filterModelJson = """{"dateRequired": ${dateFilter("bogus", "2024-03-15")}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "date filter with no type is rejected",
            filterModelJson = """{"dateRequired": {"filterType": "date", "dateFrom": "2024-03-15"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "unknown id type is rejected",
            filterModelJson = """{"id": {"filterType": "id", "type": "bogus", "filter": "${AgGridFilterSeed.idOf("beta").value}"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "blank on an id filter is rejected",
            filterModelJson = """{"id": {"filterType": "id", "type": "blank"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "blank on an unsupported filterType is rejected",
            filterModelJson = """{"intRequired": {"filterType": "bigint", "type": "blank"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "unknown compound operator is rejected",
            filterModelJson = compound("textRequired", "XOR", """{"filterType": "text", "type": "equals", "filter": "alpha"}"""),
            expectsRejection = true,
        ),

        FilterScenario(
            name = "unknown type inside a compound condition is rejected",
            filterModelJson = compound(
                "textRequired", "OR",
                """{"filterType": "text", "type": "equals", "filter": "alpha"}""",
                """{"filterType": "text", "type": "bogus", "filter": "beta"}""",
            ),
            expectsRejection = true,
        ),

        FilterScenario(
            name = "compound filter with an empty conditions list is rejected",
            filterModelJson = compound("textRequired", "AND"),
            expectsRejection = true,
        ),

        FilterScenario(
            name = "compound filter with no conditions key is rejected",
            filterModelJson = """{"textRequired": {"filterType": "multi", "operator": "OR"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "text equals with no filter value is rejected",
            filterModelJson = """{"textRequired": {"filterType": "text", "type": "equals"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "text notEqual with no filter value is rejected",
            filterModelJson = """{"textRequired": {"filterType": "text", "type": "notEqual"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "text contains with no filter value is rejected",
            filterModelJson = """{"textRequired": {"filterType": "text", "type": "contains"}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "text equals with a non-textual filter value is rejected",
            filterModelJson = """{"textRequired": {"filterType": "text", "type": "equals", "filter": 3}}""",
            expectsRejection = true,
        ),

        FilterScenario(
            name = "array text equals with no filter value is rejected",
            filterModelJson = """{"tags": {"filterType": "text", "type": "equals"}}""",
            expectsRejection = true,
        ),

    )

}

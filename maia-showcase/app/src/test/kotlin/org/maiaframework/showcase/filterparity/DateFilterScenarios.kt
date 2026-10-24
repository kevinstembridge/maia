package org.maiaframework.showcase.filterparity


/** Date filters on LocalDate and Instant columns. Pivot day is 2024-03-15; see the date rows in [AgGridFilterSeed]. */
object DateFilterScenarios {

    // The notEqual and lessThan scenarios use the nullable columns so only the date rows match: the
    // empty-string seed row (on 2024-01-01) cannot be read back from Elasticsearch, because the empty
    // textRequired is not indexed and the document fails to deserialize.
    val all: List<FilterScenario> = listOf(

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

}

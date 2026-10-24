package org.maiaframework.showcase.filterparity


/** Set filters on every column type, including "(Blanks)" entries. */
object SetFilterScenarios {

    val all: List<FilterScenario> = listOf(

        FilterScenario(
            name = "set on an int column matches any listed value",
            filterModelJson = setFilter("intRequired", "\"3\"", "\"4\""),
            expectedKeys = setOf("upper-case", "star"),
        ),

        FilterScenario(
            name = "set on a nullable int column with a null entry also matches Blank rows",
            filterModelJson = setFilter("intNullable", "null", "\"6\""),
            expectedKeys = keysWhere { it.intNullable == null || it.intNullable == 6 },
        ),

        FilterScenario(
            name = "set on a decimal column matches any listed value",
            filterModelJson = setFilter("decimalRequired", "\"1.5\"", "\"6.5\""),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "set on a date column matches any listed day",
            filterModelJson = setFilter("dateRequired", "\"2024-03-15\"", "\"2024-03-16\""),
            expectedKeys = setOf("date-at-midnight", "date-end-of-day", "date-after-midnight"),
        ),

        FilterScenario(
            name = "set on an instant column matches the exact instant",
            filterModelJson = setFilter("instantRequired", "\"2024-03-15T00:00:00Z\""),
            expectedKeys = setOf("date-at-midnight"),
        ),

        FilterScenario(
            name = "set on a boolean column matches the listed value",
            filterModelJson = setFilter("flagNullable", "\"true\""),
            expectedKeys = setOf("alpha"),
        ),

        FilterScenario(
            name = "set on an array column matches rows with any listed element",
            filterModelJson = setFilter("tags", "\"apple\"", "\"a\""),
            expectedKeys = setOf("tagged-one", "alpha"),
        ),

        FilterScenario(
            name = "set matches any listed value",
            filterModelJson = setFilter("textRequired", "\"alpha\"", "\"beta\""),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "set null entry also matches Blank rows",
            filterModelJson = setFilter("textNullable", "null", "\"beta\""),
            expectedKeys = keysWhere { it.textNullable == null || it.textNullable == "beta" },
        ),

        FilterScenario(
            name = "set with only a null entry matches null but not the empty string",
            filterModelJson = setFilter("textNullable", "null"),
            expectedKeys = keysWhere { it.textNullable == null },
        ),

        FilterScenario(
            name = "set empty list matches nothing",
            filterModelJson = setFilter("textNullable"),
            expectedKeys = emptySet(),
        ),

        FilterScenario(
            name = "set matches the empty string only when listed",
            filterModelJson = setFilter("textNullable", "\"\""),
            expectedKeys = keysWhere { it.textNullable == "" },
        ),

        FilterScenario(
            name = "set with null and empty string listed matches both",
            filterModelJson = setFilter("textNullable", "null", "\"\""),
            expectedKeys = keysWhere { it.textNullable == null || it.textNullable == "" },
        ),

    )

}

package org.maiaframework.showcase.filterparity


/** Number filters on int and decimal columns. */
object NumberFilterScenarios {

    val all: List<FilterScenario> = listOf(

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
            expectedKeys = keysWhere { it.intRequired != 3 },
        ),

        FilterScenario(
            name = "number notEqual on a decimal column excludes the value",
            filterModelJson = numberFilter("decimalRequired", "notEqual", 3.5),
            expectedKeys = keysWhere { it.decimalRequired != 3.5 },
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
            expectedKeys = keysWhere { it.intRequired < 3 },
        ),

        FilterScenario(
            name = "number lessThan on a decimal column is exclusive",
            filterModelJson = numberFilter("decimalRequired", "lessThan", 3.5),
            expectedKeys = keysWhere { it.decimalRequired < 3.5 },
        ),

        FilterScenario(
            name = "number lessThan on a nullable column skips nulls",
            filterModelJson = numberFilter("intNullable", "lessThan", 100),
            expectedKeys = setOf("alpha", "beta"),
        ),

        FilterScenario(
            name = "number lessThanOrEqual on an int column is inclusive",
            filterModelJson = numberFilter("intRequired", "lessThanOrEqual", 3),
            expectedKeys = keysWhere { it.intRequired <= 3 },
        ),

        FilterScenario(
            name = "number lessThanOrEqual on a decimal column is inclusive",
            filterModelJson = numberFilter("decimalRequired", "lessThanOrEqual", 3.5),
            expectedKeys = keysWhere { it.decimalRequired <= 3.5 },
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

}

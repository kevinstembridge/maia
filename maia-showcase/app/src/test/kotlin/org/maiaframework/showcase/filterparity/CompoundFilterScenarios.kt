package org.maiaframework.showcase.filterparity


/** Compound AND/OR conditions on one column, and filters on several columns. */
object CompoundFilterScenarios {

    val all: List<FilterScenario> = listOf(

        FilterScenario(
            "compound AND of text conditions requires both",
            compound("textRequired", "AND", textCondition("contains", "alpha"), textCondition("notContains", "beta")),
            setOf("alpha", "mixed-case", "upper-case", "star"),
        ),

        FilterScenario(
            "compound OR of text conditions accepts either",
            compound("textRequired", "OR", textCondition("equals", "alpha"), textCondition("equals", "beta")),
            setOf("alpha", "beta"),
        ),

        FilterScenario(
            "compound OR of wildcard text conditions treats each literally",
            compound("textRequired", "OR", textCondition("contains", "%"), textCondition("contains", "_")),
            setOf("percent", "underscore"),
        ),

        FilterScenario(
            "compound operator is case insensitive",
            compound("textRequired", "or", textCondition("equals", "alpha"), textCondition("equals", "beta")),
            setOf("alpha", "beta"),
        ),

        FilterScenario(
            "compound AND of number conditions bounds a range",
            compound("intRequired", "AND", numberCondition("greaterThan", 2), numberCondition("lessThan", 6)),
            keysWhere { it.intRequired > 2 && it.intRequired < 6 },
        ),

        FilterScenario(
            "compound OR of number conditions accepts either end",
            compound("intRequired", "OR", numberCondition("lessThan", 2), numberCondition("greaterThan", 7)),
            keysWhere { it.intRequired < 2 || it.intRequired > 7 },
        ),

        FilterScenario(
            "compound AND of number conditions with a negative condition",
            compound("intRequired", "AND", numberCondition("greaterThanOrEqual", 1), numberCondition("notEqual", 3)),
            keysWhere { it.intRequired >= 1 && it.intRequired != 3 },
        ),

        FilterScenario(
            "compound OR of decimal conditions accepts either",
            compound("decimalRequired", "OR", numberCondition("equals", 1.5), numberCondition("equals", 6.5)),
            setOf("alpha", "beta"),
        ),

        FilterScenario(
            "compound OR on a nullable number column skips nulls",
            compound("intNullable", "OR", numberCondition("equals", 1), numberCondition("equals", 6)),
            setOf("alpha", "beta"),
        ),

        FilterScenario(
            "filters on different columns are ANDed",
            """{${textEntry("textRequired", "contains", "alpha")}, ${numberEntry("intRequired", "greaterThan", 2)}}""",
            setOf("upper-case", "star", "multi-word"),
        ),

        FilterScenario(
            "a compound filter is ANDed with a filter on another column",
            """{${compoundEntry("intRequired", "OR", numberCondition("equals", 1), numberCondition("equals", 6))}, ${textEntry("textNullable", "contains", "a")}}""",
            setOf("alpha", "beta"),
        ),

        FilterScenario(
            "compound filters on two columns are ANDed",
            """{${compoundEntry("textRequired", "OR", textCondition("equals", "alpha"), textCondition("equals", "beta"))}, ${compoundEntry("intRequired", "AND", numberCondition("greaterThan", 0), numberCondition("lessThan", 3))}}""",
            setOf("alpha"),
        ),

    )

}

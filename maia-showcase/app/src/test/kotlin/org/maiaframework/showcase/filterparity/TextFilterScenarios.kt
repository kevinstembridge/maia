package org.maiaframework.showcase.filterparity


/** Text filters on scalar and array columns, including the negative filters and the null rows they exclude. */
object TextFilterScenarios {

    private val allKeys: Set<String> = AgGridFilterSeed.rows.map { it.key }.toSet()


    private val alphaLike: Set<String> = setOf("alpha", "mixed-case", "upper-case", "star", "multi-word")


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

        FilterScenario("text notBlank is the complement of blank", blankFilter("textNullable", "text", "notBlank"), keysWhere { !it.textNullable.isNullOrEmpty() }),

        FilterScenario("text notBlank on a required column matches everything", blankFilter("textRequired", "text", "notBlank"), allKeys),

        // Negative filters exclude null rows on both backends. Empty-string rows are deliberately still included
        // (both backends agree): see docs/adr/0005-ag-grid-filter-parity-across-backends.md.
        FilterScenario("text notEqual on a nullable column excludes nulls", textFilter("textNullable", "notEqual", "alpha"), keysWhere { it.textNullable != null && it.textNullable != "alpha" }),

        FilterScenario("text notContains on a nullable column excludes nulls", textFilter("textNullable", "notContains", "AL"), keysWhere { it.textNullable != null && !it.textNullable!!.contains("al", ignoreCase = true) }),

        FilterScenario("text notContains on a nullable column keeps empty-string rows", textFilter("textNullable", "notContains", "zzz"), setOf("empty-string", "upper-case") + keysWhere { it.textNullable != null }),

    )

}

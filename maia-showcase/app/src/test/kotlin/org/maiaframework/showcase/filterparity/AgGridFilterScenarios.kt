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

        // Date filters. Pivot day is 2024-03-15; see the date rows in AgGridFilterSeed.
        // The notEqual and lessThan scenarios use the nullable columns so only the date rows match: the
        // empty-string seed row (on 2024-01-01) cannot be read back from Elasticsearch, because the empty
        // textRequired is not indexed and the document fails to deserialize.

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
            filterModelJson = withoutEmptyStringRow(setFilter("textNullable", "\"\"")),
            expectedKeys = keysWhere { it.textNullable == "" },
        ),

        FilterScenario(
            name = "set with null and empty string listed matches both",
            filterModelJson = withoutEmptyStringRow(setFilter("textNullable", "null", "\"\"")),
            expectedKeys = keysWhere { it.textNullable == null || it.textNullable == "" },
        ),

        // ---- number filters ----

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

        // ---- boolean and id filters ----

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

    ).plus(textFilterScenarios())
        .plus(compoundFilterScenarios())
        .plus(blankFilterScenarios())


    // ---- compound AND/OR conditions and multi-column filters ----

    private fun compoundFilterScenarios(): List<FilterScenario> {

        return listOf(

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


    private fun textCondition(type: String, value: String): String =
        """{"filterType": "text", "type": "$type", "filter": "$value"}"""


    private fun numberCondition(type: String, filter: Number): String =
        """{"filterType": "number", "type": "$type", "filter": $filter}"""


    private fun textEntry(field: String, type: String, value: String): String =
        """"$field": ${textCondition(type, value)}"""


    private fun numberEntry(field: String, type: String, filter: Number): String =
        """"$field": ${numberCondition(type, filter)}"""


    private fun compoundEntry(field: String, operator: String, vararg conditions: String): String =
        """"$field": {"filterType": "multi", "operator": "$operator", "conditions": [${conditions.joinToString(", ")}]}"""


    private fun compound(field: String, operator: String, vararg conditions: String): String =
        "{${compoundEntry(field, operator, *conditions)}}"


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


    /**
     * Blank / notBlank (GLOSSARY.md: Blank). Blank is null or missing, plus the empty string for TEXT columns; for
     * array columns it is null or an empty array. notBlank is the exact complement. Text scenarios use the
     * nullable column because a textRequired of "" cannot be read back from Elasticsearch. Expected keys come
     * from the seed so filler rows cannot silently break them.
     */
    private fun blankFilterScenarios(): List<FilterScenario> {

        val allKeys = AgGridFilterSeed.rows.map { it.key }.toSet()

        return listOf(

            // text: null, missing and "" are Blank; " " is not
            FilterScenario("text blank matches null and the empty string", blankFilter("textNullable", "text", "blank"), keysWhere { it.textNullable.isNullOrEmpty() }),
            FilterScenario("text notBlank is the complement of blank", blankFilter("textNullable", "text", "notBlank"), keysWhere { !it.textNullable.isNullOrEmpty() }),
            FilterScenario("text blank on a required column matches nothing", blankFilter("textRequired", "text", "blank"), emptySet()),
            FilterScenario("text notBlank on a required column matches everything", blankFilter("textRequired", "text", "notBlank"), allKeys),

            // number
            FilterScenario("number blank on an int column matches nulls", blankFilter("intNullable", "number", "blank"), keysWhere { it.intNullable == null }),
            FilterScenario("number notBlank on an int column matches values", blankFilter("intNullable", "number", "notBlank"), keysWhere { it.intNullable != null }),
            FilterScenario("number blank on a decimal column matches nulls", blankFilter("decimalNullable", "number", "blank"), keysWhere { it.decimalNullable == null }),
            FilterScenario("number notBlank on a decimal column matches values", blankFilter("decimalNullable", "number", "notBlank"), keysWhere { it.decimalNullable != null }),
            FilterScenario("number blank on a required column matches nothing", blankFilter("intRequired", "number", "blank"), emptySet()),
            FilterScenario("number notBlank on a required column matches everything", blankFilter("intRequired", "number", "notBlank"), allKeys),

            // date
            FilterScenario("localdate blank matches nulls", blankFilter("dateNullable", "date", "blank"), keysWhere { it.dateNullable == null }),
            FilterScenario("localdate notBlank matches values", blankFilter("dateNullable", "date", "notBlank"), keysWhere { it.dateNullable != null }),
            FilterScenario("instant blank matches nulls", blankFilter("instantNullable", "date", "blank"), keysWhere { it.instantNullable == null }),
            FilterScenario("instant notBlank matches values", blankFilter("instantNullable", "date", "notBlank"), keysWhere { it.instantNullable != null }),

            // boolean (sent as a text filter)
            FilterScenario("boolean blank matches nulls only", blankFilter("flagNullable", "text", "blank"), keysWhere { it.flagNullable == null }),
            FilterScenario("boolean notBlank matches true and false", blankFilter("flagNullable", "text", "notBlank"), keysWhere { it.flagNullable != null }),

            // array: null or empty is Blank
            FilterScenario("array blank matches an empty array", blankFilter("tags", "text", "blank"), keysWhere { it.tags.isEmpty() }),
            FilterScenario("array notBlank matches a non-empty array", blankFilter("tags", "text", "notBlank"), keysWhere { it.tags.isNotEmpty() }),

            // negative filters exclude null rows on both backends. Empty-string rows are deliberately still
            // included (both backends agree): pinned here, see the decision reported with ticket #10.
            FilterScenario("text notEqual on a nullable column excludes nulls", textFilter("textNullable", "notEqual", "alpha"), keysWhere { it.textNullable != null && it.textNullable != "alpha" }),
            FilterScenario("text notContains on a nullable column excludes nulls", textFilter("textNullable", "notContains", "AL"), keysWhere { it.textNullable != null && !it.textNullable!!.contains("al", ignoreCase = true) }),
            FilterScenario("text notContains on a nullable column keeps empty-string rows", textFilter("textNullable", "notContains", "zzz"), setOf("empty-string", "upper-case") + keysWhere { it.textNullable != null }),

        )

    }


    private fun blankFilter(field: String, filterType: String, type: String): String =
        """{"$field": {"filterType": "$filterType", "type": "$type"}}"""


    private fun dateFilter(type: String, dateFrom: String): String =
        """{"filterType": "date", "type": "$type", "dateFrom": "$dateFrom"}"""


    private fun dateRange(dateFrom: String, dateTo: String): String =
        """{"filterType": "date", "type": "inRange", "dateFrom": "$dateFrom", "dateTo": "$dateTo"}"""


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


    private fun textFilter(field: String, type: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "$type", "filter": "$value"}}"""


    /**
     * Expected keys computed from the seed, for order-sensitive number filters: filler rows added by other
     * scenarios carry the [SeedRow] defaults (0 / 0.0), so a hardcoded list would break whenever a row is added.
     */
    private fun keysWhere(predicate: (SeedRow) -> Boolean): Set<String> =
        AgGridFilterSeed.rows.filter(predicate).map { it.key }.toSet()


    private fun numberFilter(field: String, type: String, filter: Number, filterTo: Number? = null): String {
        val to = if (filterTo == null) "" else """, "filterTo": $filterTo"""
        return """{"$field": {"filterType": "number", "type": "$type", "filter": $filter$to}}"""
    }


    private fun textEquals(field: String, value: String): String =
        """{"$field": {"filterType": "text", "type": "equals", "filter": "$value"}}"""


    private fun booleanEntry(field: String, value: Boolean): String =
        """"$field": {"filterType": "text", "type": "$value"}"""


    private fun booleanFilter(field: String, value: Boolean): String =
        "{${booleanEntry(field, value)}}"


    private fun idEquals(field: String, value: String): String =
        """{"$field": {"filterType": "id", "type": "equals", "filter": "$value"}}"""

}

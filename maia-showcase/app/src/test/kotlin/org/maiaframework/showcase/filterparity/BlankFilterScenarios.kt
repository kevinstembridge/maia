package org.maiaframework.showcase.filterparity


/** Blank / notBlank (GLOSSARY.md: Blank). Blank is null or missing, plus the empty string for TEXT columns; for array columns it is null or an empty array. notBlank is the exact complement. Text scenarios use the nullable column because a textRequired of "" cannot be read back from Elasticsearch. Expected keys come from the seed so filler rows cannot silently break them. */
object BlankFilterScenarios {

    private val allKeys: Set<String> = AgGridFilterSeed.rows.map { it.key }.toSet()


    val all: List<FilterScenario> = listOf(

        FilterScenario("text blank matches null and the empty string", blankFilter("textNullable", "text", "blank"), keysWhere { it.textNullable.isNullOrEmpty() }),

        FilterScenario("text blank on a required column matches nothing", blankFilter("textRequired", "text", "blank"), emptySet()),

        FilterScenario("number blank on an int column matches nulls", blankFilter("intNullable", "number", "blank"), keysWhere { it.intNullable == null }),

        FilterScenario("number notBlank on an int column matches values", blankFilter("intNullable", "number", "notBlank"), keysWhere { it.intNullable != null }),

        FilterScenario("number blank on a decimal column matches nulls", blankFilter("decimalNullable", "number", "blank"), keysWhere { it.decimalNullable == null }),

        FilterScenario("number notBlank on a decimal column matches values", blankFilter("decimalNullable", "number", "notBlank"), keysWhere { it.decimalNullable != null }),

        FilterScenario("number blank on a required column matches nothing", blankFilter("intRequired", "number", "blank"), emptySet()),

        FilterScenario("number notBlank on a required column matches everything", blankFilter("intRequired", "number", "notBlank"), allKeys),

        FilterScenario("localdate blank matches nulls", blankFilter("dateNullable", "date", "blank"), keysWhere { it.dateNullable == null }),

        FilterScenario("localdate notBlank matches values", blankFilter("dateNullable", "date", "notBlank"), keysWhere { it.dateNullable != null }),

        FilterScenario("instant blank matches nulls", blankFilter("instantNullable", "date", "blank"), keysWhere { it.instantNullable == null }),

        FilterScenario("instant notBlank matches values", blankFilter("instantNullable", "date", "notBlank"), keysWhere { it.instantNullable != null }),

        FilterScenario("boolean blank matches nulls only", blankFilter("flagNullable", "text", "blank"), keysWhere { it.flagNullable == null }),

        FilterScenario("boolean notBlank matches true and false", blankFilter("flagNullable", "text", "notBlank"), keysWhere { it.flagNullable != null }),

        FilterScenario("array blank matches an empty array", blankFilter("tags", "text", "blank"), keysWhere { it.tags.isEmpty() }),

        FilterScenario("array notBlank matches a non-empty array", blankFilter("tags", "text", "notBlank"), keysWhere { it.tags.isNotEmpty() }),

    )

}

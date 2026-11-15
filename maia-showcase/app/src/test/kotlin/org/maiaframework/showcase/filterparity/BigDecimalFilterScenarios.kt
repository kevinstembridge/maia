package org.maiaframework.showcase.filterparity

import java.math.BigDecimal


/**
 * Number filters on BigDecimal columns. Seed rows with a non-zero amount are the "amount-*" rows; every other row
 * has amountRequired 0 and amountNullable null.
 *
 * [backends] is the one place to widen when another backend serves BigDecimal columns (now Postgres and Elasticsearch).
 */
object BigDecimalFilterScenarios {

    private val backends = setOf(FilterBackend.POSTGRES, FilterBackend.ELASTICSEARCH)

    /**
     * Elasticsearch's scaled_float converts through a double, so it cannot tell 1e14 from 1e14 + 0.0001 apart
     * (exact only while value * 10^scale < 2^53, about 9.0e11 at scale 4). See docs/adr/0006.
     */
    private val postgresOnly = setOf(FilterBackend.POSTGRES)


    val all: List<FilterScenario> = listOf(

        FilterScenario(
            name = "number equals on a BigDecimal column matches the exact value",
            filterModelJson = numberFilter("amountRequired", "equals", 10),
            expectedKeys = setOf("amount-ten"),
            backends = backends,
        ),

        FilterScenario(
            name = "number equals on a BigDecimal column is exact beyond double precision",
            filterModelJson = numberFilter("amountRequired", "equals", BigDecimal("100000000000000")),
            expectedKeys = setOf("amount-big"),
            backends = postgresOnly,
        ),

        FilterScenario(
            name = "number notEqual on a BigDecimal column is exact beyond double precision",
            filterModelJson = numberFilter("amountRequired", "notEqual", BigDecimal("100000000000000")),
            expectedKeys = keysWhere { it.key != "amount-big" },
            backends = postgresOnly,
        ),

        FilterScenario(
            name = "number greaterThan on a BigDecimal column is numeric and exclusive",
            filterModelJson = numberFilter("amountRequired", "greaterThan", 9),
            expectedKeys = setOf("amount-ten", "amount-big", "amount-big-plus"),
            backends = backends,
        ),

        FilterScenario(
            name = "number greaterThan on a BigDecimal column is exact beyond double precision",
            filterModelJson = numberFilter("amountRequired", "greaterThan", BigDecimal("100000000000000")),
            expectedKeys = setOf("amount-big-plus"),
            backends = postgresOnly,
        ),

        FilterScenario(
            name = "number lessThanOrEqual on a BigDecimal column is inclusive",
            filterModelJson = numberFilter("amountRequired", "lessThanOrEqual", 10),
            expectedKeys = keysWhere { it.key != "amount-big" && it.key != "amount-big-plus" },
            backends = backends,
        ),

        FilterScenario(
            name = "number inRange on a BigDecimal column includes the lower bound and excludes the upper bound",
            filterModelJson = numberFilter("amountRequired", "inRange", 9, 10),
            expectedKeys = setOf("amount-nine"),
            backends = backends,
        ),

        FilterScenario(
            name = "number inRange on a BigDecimal column compares 9 and 10 numerically",
            filterModelJson = numberFilter("amountRequired", "inRange", 5, 11),
            expectedKeys = setOf("amount-nine", "amount-ten"),
            backends = backends,
        ),

        FilterScenario(
            name = "number greaterThan on a nullable BigDecimal column skips nulls",
            filterModelJson = numberFilter("amountNullable", "greaterThan", 0),
            expectedKeys = setOf("amount-nine", "amount-ten"),
            backends = backends,
        ),

        FilterScenario(
            name = "number equals on a BigDecimal column is exact in the 4th decimal place",
            filterModelJson = numberFilter("amountRequired", "equals", BigDecimal("0.0001")),
            expectedKeys = setOf("amount-tiny"),
            backends = backends,
        ),

        FilterScenario(
            name = "number greaterThan on a BigDecimal column is exact in the 4th decimal place",
            filterModelJson = numberFilter("amountRequired", "greaterThan", BigDecimal("0.0001")),
            expectedKeys = setOf("amount-tiny-plus", "amount-nine", "amount-ten", "amount-big", "amount-big-plus"),
            backends = backends,
        ),

        FilterScenario(
            name = "number lessThan on a BigDecimal column is exact in the 4th decimal place",
            filterModelJson = numberFilter("amountRequired", "lessThan", BigDecimal("0.0002")),
            expectedKeys = keysWhere { it.key != "amount-tiny-plus" && it.key != "amount-nine" && it.key != "amount-ten" && it.key != "amount-big" && it.key != "amount-big-plus" },
            backends = backends,
        ),

    )

}

package org.maiaframework.showcase.filterparity


/**
 * A Filter model (as AG Grid sends it, see GLOSSARY.md) and the seed rows it must return.
 *
 * [filterModelJson] is the raw JSON of the `filterModel`, keyed by DTO field name.
 * [expectedKeys] are [SeedRow.key]s; order is irrelevant. The same expectation is asserted against Postgres
 * and Elasticsearch, so a scenario passing on one backend and failing on the other is a parity bug.
 *
 * A scenario with [expectsRejection] instead asserts that both backends throw IllegalArgumentException for
 * the Filter model (an unsupported or malformed one); [expectedKeys] is then unused.
 */
data class FilterScenario(
    val name: String,
    val filterModelJson: String,
    val expectedKeys: Set<String> = emptySet(),
    val expectsRejection: Boolean = false,
)


/**
 * The single shared scenario table. To cover a new filter, add entries here (and seed rows to
 * [AgGridFilterSeed] if the existing rows do not exercise it); no test code changes are needed.
 */
object AgGridFilterScenarios {

    val all: List<FilterScenario> =
        TextFilterScenarios.all +
            NumberFilterScenarios.all +
            DateFilterScenarios.all +
            SetFilterScenarios.all +
            BlankFilterScenarios.all +
            BooleanAndIdFilterScenarios.all +
            CompoundFilterScenarios.all +
            RejectionFilterScenarios.all

}

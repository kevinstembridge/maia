package org.maiaframework.showcase.filterparity

import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID


/**
 * One seeded `AgGridFilterSample` row. Rows are referred to by [key] in scenarios, so a scenario's
 * expected result never mentions a database id.
 *
 * Every column has a default so a row only states the columns that make it interesting. To add a row for a
 * new scenario, append a [SeedRow] to [AgGridFilterSeed.rows]; existing scenarios keep passing as long as the
 * new row does not match their filters (each scenario's expected keys are the contract).
 */
data class SeedRow(
    val key: String,
    val textRequired: String = "default",
    val textNullable: String? = null,
    val intRequired: Int = 0,
    val intNullable: Int? = null,
    val decimalRequired: Double = 0.0,
    val decimalNullable: Double? = null,
    val dateRequired: LocalDate = LocalDate.of(2024, 1, 1),
    val dateNullable: LocalDate? = null,
    val instantRequired: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    val instantNullable: Instant? = null,
    val flagRequired: Boolean = false,
    val flagNullable: Boolean? = null,
    val tags: List<String> = emptyList(),
) {

    /** Stable across runs and backends so ids can be compared without a lookup table. */
    val id: DomainId = DomainId(UUID.nameUUIDFromBytes(key.toByteArray()).toString())


    fun toEntity() = AgGridFilterSampleEntity(
        createdTimestamp = Instant.parse("2024-01-01T00:00:00Z"),
        dateNullable = dateNullable,
        dateRequired = dateRequired,
        decimalNullable = decimalNullable,
        decimalRequired = decimalRequired,
        flagNullable = flagNullable,
        flagRequired = flagRequired,
        id = id,
        instantNullable = instantNullable,
        instantRequired = instantRequired,
        intNullable = intNullable,
        intRequired = intRequired,
        tags = tags,
        textNullable = textNullable,
        textRequired = textRequired,
    )

}


object AgGridFilterSeed {

    val rows: List<SeedRow> = listOf(
        SeedRow("alpha", textRequired = "alpha", textNullable = "alpha", intRequired = 1, intNullable = 1, decimalRequired = 1.5, decimalNullable = 1.5, tags = listOf("a", "b"), flagRequired = true, flagNullable = true),
        SeedRow("mixed-case", textRequired = "Alpha", textNullable = null, intRequired = 2, decimalRequired = 2.5),
        SeedRow("upper-case", textRequired = "ALPHA", textNullable = "", intRequired = 3, decimalRequired = 3.5),
        SeedRow("star", textRequired = "alpha*", textNullable = "al*", intRequired = 4, decimalRequired = 4.5),
        SeedRow("question", textRequired = "alp?a", textNullable = "alp?a", intRequired = 5, decimalRequired = 5.5),
        SeedRow("beta", textRequired = "beta", textNullable = "beta", intRequired = 6, intNullable = 6, decimalRequired = 6.5, decimalNullable = 6.5, flagNullable = false),
        SeedRow("multi-word", textRequired = "alpha beta", textNullable = " ", intRequired = 7, decimalRequired = 7.5),
        SeedRow("empty-string", textRequired = "placeholder", /* a blank textRequired cannot be read back from ES: StringTrimmingDeserializer maps it to null */ textNullable = "", intRequired = 8, decimalRequired = 8.5),
    )

    private val byKey = rows.associateBy { it.key }

    init {
        require(byKey.size == rows.size) { "Seed row keys must be unique" }
    }

    fun idOf(key: String): DomainId = (byKey[key] ?: throw IllegalArgumentException("Unknown seed row [$key]")).id

}

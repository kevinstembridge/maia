package org.maiaframework.showcase.filterparity

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.domain.search.AgGridSortModelItem
import org.maiaframework.elasticsearch.index.IndexNameFactory
import org.maiaframework.jdbc.SqlParams
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleDao
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleDtoDao
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleEsBlotterRowDtoSearchService
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleEsEsIndexControl_v0001
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleEsEsIndexMeta_v0001
import org.maiaframework.showcase.filter_sample.AgGridFilterSampleEsV1IndexService
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.json.JsonMapper


/**
 * Runs every scenario in [AgGridFilterScenarios] against the seed rows in [AgGridFilterSeed] through the
 * generated search entry point of each backend (Postgres DAO search, Elasticsearch search service) and
 * asserts that both return the same rows. See docs/adr/0005-ag-grid-filter-parity-across-backends.md.
 *
 * Nothing here is specific to a filter type: add scenarios to the table, not to this class.
 */
class AgGridFilterParityIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var jsonMapper: JsonMapper


    @Autowired
    private lateinit var sampleDao: AgGridFilterSampleDao


    @Autowired
    private lateinit var postgresSearch: AgGridFilterSampleDtoDao


    @Autowired
    private lateinit var esSearch: AgGridFilterSampleEsBlotterRowDtoSearchService


    @Autowired
    private lateinit var esIndexService: AgGridFilterSampleEsV1IndexService


    @Autowired
    private lateinit var esIndexControl: AgGridFilterSampleEsEsIndexControl_v0001


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient


    @BeforeEach
    fun seedBothBackends() {

        this.sampleDao.deleteAll()
        AgGridFilterSeed.rows.forEach { this.sampleDao.insert(it.toEntity()) }

        val index = IndexNameFactory.indexNameFrom(AgGridFilterSampleEsEsIndexMeta_v0001.indexBaseNameAndVersion)

        if (this.elasticsearchClient.indices().exists { it.index(index.value) }.value()) {
            this.elasticsearchClient.indices().delete { it.index(index.value) }
        }

        this.esIndexControl.createIndex()
        this.esIndexService.refreshIndex(JobMetrics("test-refreshAgGridFilterSampleEsV1Index"))
        this.elasticsearchClient.indices().refresh { it.index(index.value) }

    }


    @Test
    fun `postgres session runs in UTC`() {

        val zone = this.jdbcOps.queryForObjectOrNull("show timezone", SqlParams()) { it.readString("TimeZone") }

        assertThat(zone).isEqualTo("UTC")

    }


    @Test
    fun `postgres sorts a BigDecimal column numerically`() {

        val searchModel = AgGridSearchModel(
            filterModel = this.jsonMapper.readTree(numberFilter("amountRequired", "greaterThan", 0)),
            sortModel = listOf(AgGridSortModelItem("amountRequired", "asc")),
            startRow = 0,
            endRow = 1000,
        )

        val ids = this.postgresSearch.search(searchModel).results.map { it.id }

        assertThat(ids).containsExactly(
            AgGridFilterSeed.idOf("amount-tiny"),
            AgGridFilterSeed.idOf("amount-tiny-plus"),
            AgGridFilterSeed.idOf("amount-nine"),
            AgGridFilterSeed.idOf("amount-ten"),
            AgGridFilterSeed.idOf("amount-big"),
            AgGridFilterSeed.idOf("amount-big-plus"),
        )

    }


    @Test
    fun `elasticsearch sorts a BigDecimal column numerically`() {

        val searchModel = AgGridSearchModel(
            filterModel = this.jsonMapper.readTree(numberFilter("amountRequired", "inRange", 0.00005, 11)),
            sortModel = listOf(AgGridSortModelItem("amountRequired", "asc")),
            startRow = 0,
            endRow = 1000,
        )

        val ids = this.esSearch.search(searchModel).hits.map { it.id }

        assertThat(ids).containsExactly(
            AgGridFilterSeed.idOf("amount-tiny"),
            AgGridFilterSeed.idOf("amount-tiny-plus"),
            AgGridFilterSeed.idOf("amount-nine"),
            AgGridFilterSeed.idOf("amount-ten"),
        )

    }


    @TestFactory
    fun `every scenario returns the same rows from both backends`(): List<DynamicTest> {

        return AgGridFilterScenarios.all.flatMap { scenario ->
            scenario.backends.map { backend ->
                DynamicTest.dynamicTest("${scenario.name} [$backend]") {
                    if (scenario.expectsRejection) {
                        assertThatThrownBy { search(backend, scenario) }.isInstanceOf(IllegalArgumentException::class.java)
                        return@dynamicTest
                    }
                    val expectedIds = scenario.expectedKeys.map { AgGridFilterSeed.idOf(it) }
                    assertThat(search(backend, scenario)).containsExactlyInAnyOrderElementsOf(expectedIds)
                }
            }
        }

    }


    private fun search(backend: FilterBackend, scenario: FilterScenario): List<DomainId> {

        val searchModel = AgGridSearchModel(
            filterModel = this.jsonMapper.readTree(scenario.filterModelJson),
            sortModel = emptyList(),
            startRow = 0,
            endRow = 1000,
        )

        return when (backend) {
            FilterBackend.POSTGRES -> this.postgresSearch.search(searchModel).results.map { it.id }
            FilterBackend.ELASTICSEARCH -> this.esSearch.search(searchModel).hits.map { it.id }
        }

    }


}

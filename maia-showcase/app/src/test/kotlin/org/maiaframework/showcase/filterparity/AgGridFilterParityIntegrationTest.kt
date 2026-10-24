package org.maiaframework.showcase.filterparity

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.search.AgGridSearchModel
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


    private enum class Backend { POSTGRES, ELASTICSEARCH }


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


    @TestFactory
    fun `every scenario returns the same rows from both backends`(): List<DynamicTest> {

        return AgGridFilterScenarios.all.flatMap { scenario ->
            Backend.entries.map { backend ->
                DynamicTest.dynamicTest("${scenario.name} [$backend]") {
                    val expectedIds = scenario.expectedKeys.map { AgGridFilterSeed.idOf(it) }
                    assertThat(search(backend, scenario)).containsExactlyInAnyOrderElementsOf(expectedIds)
                }
            }
        }

    }


    private fun search(backend: Backend, scenario: FilterScenario): List<DomainId> {

        val searchModel = AgGridSearchModel(
            filterModel = this.jsonMapper.readTree(scenario.filterModelJson),
            sortModel = emptyList(),
            startRow = 0,
            endRow = 1000,
        )

        return when (backend) {
            Backend.POSTGRES -> this.postgresSearch.search(searchModel).results.map { it.id }
            Backend.ELASTICSEARCH -> this.esSearch.search(searchModel).hits.map { it.id }
        }

    }


}

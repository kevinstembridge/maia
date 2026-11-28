package org.maiaframework.showcase.hazelcast_blotter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.AlphaUpdate_someStringRequestDto
import org.maiaframework.showcase.join.BravoAlphaHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoDao
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoEntity
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoHzLazyDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoUpdate_alphaRequestDto
import org.maiaframework.showcase.join.BravoUpdate_someStringRequestDto
import org.maiaframework.showcase.join.CharlieDao
import org.maiaframework.showcase.join.CharlieEntityTestBuilder
import org.maiaframework.showcase.join.DeltaCreateRequestDto
import org.maiaframework.showcase.join.DeltaCrudService
import org.maiaframework.showcase.join.DeltaHzDtoHazelcastRepo
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper


/**
 * Black-box tests of Hazelcast auto-refresh (ADR-0004, ADR-0008): writes made through the generated CrudServices change
 * what the fetch-all endpoints return, including through Entity refs and path fields.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastBlotterAutoRefreshIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var jsonMapper: JsonMapper


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


    @Autowired
    private lateinit var deltaCrudService: DeltaCrudService


    @Autowired
    private lateinit var charlieDao: CharlieDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @MockitoSpyBean
    private lateinit var bravoHzRepo: BravoHzDtoHazelcastRepo


    @Autowired
    private lateinit var bravoAlphaHzRepo: BravoAlphaHzDtoHazelcastRepo


    @Autowired
    private lateinit var deltaHzRepo: DeltaHzDtoHazelcastRepo


    @Autowired
    private lateinit var bravoHzLazyRepo: BravoHzLazyDtoHazelcastRepo


    private val appender = ListAppender<ILoggingEvent>()


    private val collectorLogger = LoggerFactory.getLogger("org.maiaframework.hazelcast.refresh.HazelcastRefreshCollector") as Logger


    @BeforeEach
    fun resetDatabase() {

        // cascades to bravo, charlie and delta
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        Mockito.reset(bravoHzRepo)
        bravoHzRepo.populate()
        bravoAlphaHzRepo.populate()
        deltaHzRepo.populate()

        appender.start()
        collectorLogger.addAppender(appender)

    }


    @AfterEach
    fun detachAppender() {

        collectorLogger.detachAppender(appender)

    }


    @Test
    fun `creating, updating and deleting a row changes the fetch-all result`() {

        val alpha = createAlpha("alpha")

        val bravo = createBravo(alpha, "created")
        assertThat(fetchAll("/api/bravo-hz-blotter/find-all").map { it["dtoStringFromBravo"].asString() }).containsExactly("created")

        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "updated"))
        assertThat(fetchAll("/api/bravo-hz-blotter/find-all").map { it["dtoStringFromBravo"].asString() }).containsExactly("updated")

        bravoCrudService.delete(bravo.id)
        assertThat(fetchAll("/api/bravo-hz-blotter/find-all")).isEmpty()

    }


    @Test
    fun `renaming a referenced Entity updates the ref display name and the path field of the cached rows`() {

        val alpha = createAlpha("before-rename")
        val bravo = createBravo(alpha, "bravo")

        alphaCrudService.updateSomeString(AlphaUpdate_someStringRequestDto(id_raw = alpha.id, someString_raw = "after-rename"))

        val row = fetchAll("/api/bravo-alpha-hz-blotter/find-all").single { it["id"].asString() == bravo.id.value }
        assertThat(row["alpha"]["displayName"].asString()).isEqualTo("after-rename")
        assertThat(row["dtoStringFromAlpha"].asString()).isEqualTo("after-rename")

    }


    @Test
    fun `renaming a referenced Entity updates the ref where the foreign key is the only link`() {

        val alpha = createAlpha("before-rename")
        val charlie = createCharlie(createBravo(createAlpha("other"), "other-bravo"))
        val delta = deltaCrudService.create(DeltaCreateRequestDto(alpha = alpha.id, charlie_raw = charlie.id, someString_raw = "delta"))

        assertThat(deltaRow(delta.id)["alpha"]["displayName"].asString()).isEqualTo("before-rename")

        alphaCrudService.updateSomeString(AlphaUpdate_someStringRequestDto(id_raw = alpha.id, someString_raw = "after-rename"))

        assertThat(deltaRow(delta.id)["alpha"]["displayName"].asString()).isEqualTo("after-rename")

    }


    @Test
    fun `changing a foreign key to a different Entity updates the ref`() {

        val first = createAlpha("first")
        val second = createAlpha("second")
        val bravo = createBravo(first, "bravo")

        bravoCrudService.updateAlpha(BravoUpdate_alphaRequestDto(id_raw = bravo.id, alpha_raw = second.id))

        val row = fetchAll("/api/bravo-alpha-hz-blotter/find-all").single { it["id"].asString() == bravo.id.value }
        assertThat(row["alpha"]["id"].asString()).isEqualTo(second.id.value)
        assertThat(row["alpha"]["displayName"].asString()).isEqualTo("second")
        assertThat(row["dtoStringFromAlpha"].asString()).isEqualTo("second")

    }


    @Test
    fun `a refresh failure is logged and does not fail the write`() {

        val alpha = createAlpha("alpha")
        val bravo = createBravo(alpha, "original")

        Mockito.doThrow(RuntimeException("simulated Hazelcast outage")).`when`(bravoHzRepo).refreshByIds(Mockito.anyCollection())

        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "updated"))

        assertThat(bravoDao.findByPrimaryKey(bravo.id).someString).isEqualTo("updated")
        assertThat(appender.list.filter { it.level == Level.ERROR }.map { it.formattedMessage })
            .anyMatch { it.contains("Hazelcast refresh failed") }

    }


    @Test
    fun `a refresh on an unpopulated lazy map leaves it unpopulated and the first fetch then reads current data`() {

        bravoHzLazyRepo.onContextRefreshed()
        val alpha = createAlpha("alpha")

        val bravo = createBravo(alpha, "while-unpopulated")
        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "still-unpopulated"))

        assertThat(bravoHzLazyRepo.isPopulated()).isFalse()

        assertThat(bravoHzLazyRepo.findAll().map { it.dtoStringFromBravo }).containsExactly("still-unpopulated")

    }


    private fun deltaRow(id: DomainId): JsonNode {

        return fetchAll("/api/delta-hz-blotter/find-all").single { it["id"].asString() == id.value }

    }


    private fun fetchAll(url: String): List<JsonNode> {

        val result = mockMvc.get().uri(url)
            .with(user("nigel").authorities(SimpleGrantedAuthority("WRITE")))
            .exchange()

        // MockMvc's security filter chain clears the thread's SecurityContext; restore the @WithMockUser one
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext())

        assertThat(result.response.status).isEqualTo(200)

        return jsonMapper.readTree(result.response.contentAsString).toList()

    }


    private fun createAlpha(someString: String): AlphaEntity {

        return alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = someString))

    }


    private fun createBravo(alpha: AlphaEntity, someString: String): BravoEntity {

        return bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = 1, someString_raw = someString))

    }


    private fun createCharlie(bravo: BravoEntity) = CharlieEntityTestBuilder(bravoId = bravo.id).build().also { charlieDao.insert(it) }


}

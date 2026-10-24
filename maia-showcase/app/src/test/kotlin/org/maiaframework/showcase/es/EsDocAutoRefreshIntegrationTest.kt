package org.maiaframework.showcase.es

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.GetRequest
import co.elastic.clients.elasticsearch.core.GetResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.domain.search.AgGridSortModelItem
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.elasticsearch.index.IndexNameFactory
import org.maiaframework.elasticsearch.index.model.IndexResolvedName
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.AlphaTypeaheadEsIndexControl_v0001
import org.maiaframework.showcase.join.AlphaTypeaheadEsIndexMeta_v0001
import org.maiaframework.showcase.join.AlphaUpdate_someIntRequestDto
import org.maiaframework.showcase.join.AlphaUpdate_someStringRequestDto
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoDao
import org.maiaframework.showcase.join.BravoEntity
import org.maiaframework.showcase.join.BravoEsEsIndexControl_v0001
import org.maiaframework.showcase.join.BravoEsBlotterRowDtoSearchService
import org.maiaframework.showcase.join.BravoEsEsIndexMeta_v0001
import org.maiaframework.showcase.join.BravoEsV1IndexService
import org.maiaframework.showcase.join.BravoUpdate_alphaRequestDto
import org.maiaframework.showcase.join.BravoUpdate_someStringRequestDto
import org.maiaframework.showcase.many_to_many.LeftJoinRequestDto
import org.maiaframework.showcase.many_to_many.LeftManyCreateRequestDto
import org.maiaframework.showcase.many_to_many.LeftManyCrudService
import org.maiaframework.showcase.many_to_many.LeftManyDao
import org.maiaframework.showcase.many_to_many.LeftManyEntity
import org.maiaframework.showcase.many_to_many.LeftManyEntityMeta
import org.maiaframework.showcase.many_to_many.LeftManyEsEsIndexControl_v0001
import org.maiaframework.showcase.many_to_many.LeftManyEsEsIndexMeta_v0001
import org.maiaframework.showcase.many_to_many.LeftManyEsV1IndexService
import org.maiaframework.showcase.many_to_many.LeftManyUpdateRequestDto
import org.maiaframework.showcase.many_to_many.LeftToRightComplexDao
import org.maiaframework.showcase.many_to_many.LeftToRightComplexEntity
import org.maiaframework.showcase.many_to_many.RightJoinRequestDto
import org.maiaframework.showcase.many_to_many.RightManyCreateRequestDto
import org.maiaframework.showcase.many_to_many.RightManyCrudService
import org.maiaframework.showcase.many_to_many.RightManyDao
import org.maiaframework.showcase.many_to_many.RightManyEntity
import org.maiaframework.showcase.many_to_many.RightManyEntityMeta
import org.maiaframework.showcase.many_to_many.RightManyUpdateRequestDto
import org.maiaframework.showcase.many_to_many.RightManyUpdate_someStringRequestDto
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.node.JsonNodeFactory
import java.time.Instant


/**
 * Black-box tests of the automatic Elasticsearch refresh of searchable-DTO documents
 * (`BravoEs` and `LeftManyEs`) driven by CrudService events and flushed by `EsRefreshCollector`
 * after commit, on the calling thread. Once a CrudService call returns, the ES write has happened,
 * so a real-time GET by id needs no waiting.
 *
 * [EsIndexOps] is a Mockito spy so that the number of ES write calls per index can be counted and
 * an ES failure can be injected.
 */
@WithMockUser(authorities = ["WRITE"])
class EsDocAutoRefreshIntegrationTest : AbstractBlackBoxTest() {


    @MockitoSpyBean
    private lateinit var esIndexOps: EsIndexOps


    @Autowired
    private lateinit var elasticsearchClient: ElasticsearchClient


    @Autowired
    private lateinit var bravoEsIndexControl: BravoEsEsIndexControl_v0001


    @Autowired
    private lateinit var leftManyEsIndexControl: LeftManyEsEsIndexControl_v0001


    @Autowired
    private lateinit var alphaTypeaheadEsIndexControl: AlphaTypeaheadEsIndexControl_v0001


    @Autowired
    private lateinit var bravoEsSearchService: BravoEsBlotterRowDtoSearchService


    @Autowired
    private lateinit var bravoEsIndexService: BravoEsV1IndexService


    @Autowired
    private lateinit var leftManyEsIndexService: LeftManyEsV1IndexService


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var leftManyCrudService: LeftManyCrudService


    @Autowired
    private lateinit var leftManyDao: LeftManyDao


    @Autowired
    private lateinit var rightManyCrudService: RightManyCrudService


    @Autowired
    private lateinit var rightManyDao: RightManyDao


    @Autowired
    private lateinit var leftToRightComplexDao: LeftToRightComplexDao


    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager


    private val bravoIndex: IndexResolvedName = IndexNameFactory.indexNameFrom(BravoEsEsIndexMeta_v0001.indexBaseNameAndVersion)


    private val leftManyIndex: IndexResolvedName = IndexNameFactory.indexNameFrom(LeftManyEsEsIndexMeta_v0001.indexBaseNameAndVersion)


    private val alphaTypeaheadIndex: IndexResolvedName = IndexNameFactory.indexNameFrom(AlphaTypeaheadEsIndexMeta_v0001.indexBaseNameAndVersion)


    @BeforeEach
    fun resetDatabaseAndIndices() {

        // cascades to bravo, charlie and the many-to-many join tables
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        jdbcOps.update("truncate table ${LeftManyEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        jdbcOps.update("truncate table ${RightManyEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")

        recreateIndex(bravoIndex) { bravoEsIndexControl.createIndex() }
        recreateIndex(leftManyIndex) { leftManyEsIndexControl.createIndex() }

        // Alpha CrudService events also write to the typeahead index; make sure it exists with its
        // real mapping rather than being auto-created with a dynamic one.
        if (!indexExists(alphaTypeaheadIndex)) {
            alphaTypeaheadEsIndexControl.createIndex()
        }

        Mockito.clearInvocations(esIndexOps)

    }


    // ─── BravoEs: root entity ─────────────────────────────────────────────────


    @Test
    fun `creating a Bravo indexes its document`() {

        val alpha = createAlpha("alpha-one")

        val bravo = createBravo(alpha, someString = "bravo-one", someInt = 42)

        val doc = getDoc(bravoIndex, bravo.id)
        assertThat(doc.found()).isTrue()
        assertThat(doc.source()!!["dtoStringFromBravo"]).isEqualTo("bravo-one")
        assertThat(doc.source()!!["dtoStringFromAlpha"]).isEqualTo("alpha-one")
        assertThat(doc.source()!!["dtoIntFromBravo"]).isEqualTo(42)

    }


    @Test
    fun `updating Bravo someString refreshes its document`() {

        val alpha = createAlpha("alpha-one")
        val bravo = createBravo(alpha, someString = "before")

        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "after"))

        assertThat(getDoc(bravoIndex, bravo.id).source()!!["dtoStringFromBravo"]).isEqualTo("after")

    }


    @Test
    fun `changing Bravo alpha refreshes its document with the new alpha's string`() {

        val alpha1 = createAlpha("alpha-A1")
        val alpha2 = createAlpha("alpha-A2")
        val bravo = createBravo(alpha1)

        assertThat(getDoc(bravoIndex, bravo.id).source()!!["dtoStringFromAlpha"]).isEqualTo("alpha-A1")

        bravoCrudService.updateAlpha(BravoUpdate_alphaRequestDto(alpha_raw = alpha2.id, id_raw = bravo.id))

        assertThat(getDoc(bravoIndex, bravo.id).source()!!["dtoStringFromAlpha"]).isEqualTo("alpha-A2")

    }


    @Test
    fun `deleting a Bravo removes its document`() {

        val alpha = createAlpha("alpha-one")
        val bravo = createBravo(alpha)
        assertThat(getDoc(bravoIndex, bravo.id).found()).isTrue()

        bravoCrudService.delete(bravo.id)

        assertThat(bravoDao.findByPrimaryKeyOrNull(bravo.id)).isNull()
        assertThat(getDoc(bravoIndex, bravo.id).found()).isFalse()

    }


    // ─── BravoEs: path entity (Alpha) ─────────────────────────────────────────


    @Test
    fun `updating Alpha someString refreshes every referencing Bravo document in one bulk call`() {

        val alpha = createAlpha("old-alpha-string")
        val otherAlpha = createAlpha("other-alpha")
        val bravos = (1..5).map { createBravo(alpha, someString = "bravo-$it") }
        val unrelatedBravo = createBravo(otherAlpha)

        val versionsBefore = bravos.associate { it.id to getDoc(bravoIndex, it.id).version() }
        val unrelatedVersionBefore = getDoc(bravoIndex, unrelatedBravo.id).version()
        Mockito.clearInvocations(esIndexOps)

        alphaCrudService.updateSomeString(AlphaUpdate_someStringRequestDto(id_raw = alpha.id, someString_raw = "new-alpha-string"))

        bravos.forEach { bravo ->
            val doc = getDoc(bravoIndex, bravo.id)
            assertThat(doc.source()!!["dtoStringFromAlpha"]).isEqualTo("new-alpha-string")
            assertThat(doc.version()).isEqualTo(versionsBefore[bravo.id]!! + 1)
        }

        assertThat(getDoc(bravoIndex, unrelatedBravo.id).version()).isEqualTo(unrelatedVersionBefore)

        val bulkUpserts = bulkUpsertCallsFor(bravoIndex)
        assertThat(bulkUpserts).hasSize(1)
        assertThat(bulkUpserts.single().map { it.id }).containsExactlyInAnyOrderElementsOf(bravos.map { it.id.value })
        assertThat(writeCallsFor(bravoIndex)).isEqualTo(1)

    }


    @Test
    fun `updating an Alpha field not read by the BravoEs document causes no ES write`() {

        val alpha = createAlpha("alpha-one", someInt = 1)
        val bravo = createBravo(alpha)
        val versionBefore = getDoc(bravoIndex, bravo.id).version()
        Mockito.clearInvocations(esIndexOps)

        alphaCrudService.updateSomeInt(AlphaUpdate_someIntRequestDto(id_raw = alpha.id, someInt_raw = 2))

        assertThat(writeCallsFor(bravoIndex)).isZero()
        assertThat(getDoc(bravoIndex, bravo.id).version()).isEqualTo(versionBefore)

    }


    // ─── Transactions and failures ────────────────────────────────────────────


    @Test
    fun `a rolled back transaction writes nothing to ES`() {

        val alpha = createAlpha("alpha-one")
        val bravo = createBravo(alpha, someString = "committed")
        val versionBefore = getDoc(bravoIndex, bravo.id).version()
        Mockito.clearInvocations(esIndexOps)

        assertThatThrownBy {
            TransactionTemplate(transactionManager).executeWithoutResult {
                bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "rolled-back"))
                throw IllegalStateException("force rollback")
            }
        }.isInstanceOf(IllegalStateException::class.java).hasMessage("force rollback")

        assertThat(bravoDao.findByPrimaryKey(bravo.id).someString).isEqualTo("committed")

        val doc = getDoc(bravoIndex, bravo.id)
        assertThat(doc.source()!!["dtoStringFromBravo"]).isEqualTo("committed")
        assertThat(doc.version()).isEqualTo(versionBefore)
        assertThat(writeCallsFor(bravoIndex)).isZero()

    }


    @Test
    fun `an ES failure after commit is swallowed, the DB change stays, and refreshIndex reconciles the stale doc`() {

        val alpha = createAlpha("alpha-one")
        val bravo = createBravo(alpha, someString = "original")

        Mockito.doThrow(RuntimeException("simulated ES outage"))
            .doCallRealMethod()
            .`when`(esIndexOps).bulkUpsert(ArgumentMatchers.anyList())

        bravoCrudService.updateSomeString(BravoUpdate_someStringRequestDto(id_raw = bravo.id, someString_raw = "updated"))

        assertThat(bravoDao.findByPrimaryKey(bravo.id).someString).isEqualTo("updated")
        assertThat(getDoc(bravoIndex, bravo.id).source()!!["dtoStringFromBravo"]).isEqualTo("original")

        bravoEsIndexService.refreshIndex(JobMetrics("test-refreshBravoEsV1Index"))

        assertThat(getDoc(bravoIndex, bravo.id).source()!!["dtoStringFromBravo"]).isEqualTo("updated")

    }


    @Test
    fun `refreshIndex restores a missing doc and removes a doc with no source row`() {

        val alpha = createAlpha("alpha-one")
        val kept = createBravo(alpha, someString = "kept")
        val lost = createBravo(alpha, someString = "lost")
        val bogusId = DomainId.newId()

        elasticsearchClient.delete { r -> r.index(bravoIndex.value).id(lost.id.value) }
        elasticsearchClient.index { r ->
            r.index(bravoIndex.value).id(bogusId.value).document(
                mapOf(
                    "createdTimestamp" to Instant.now().toString(),
                    "dtoIntFromBravo" to 1,
                    "dtoStringFromAlpha" to "bogus",
                    "dtoStringFromBravo" to "bogus",
                    "id" to bogusId.value
                )
            )
        }
        // removeDeletedRecordsFromIndex finds stale docs via search, which only sees refreshed segments
        refreshIndexSegments(bravoIndex)

        assertThat(getDoc(bravoIndex, lost.id).found()).isFalse()
        assertThat(getDoc(bravoIndex, bogusId).found()).isTrue()

        bravoEsIndexService.refreshIndex(JobMetrics("test-refreshBravoEsV1Index"))

        assertThat(getDoc(bravoIndex, lost.id).source()!!["dtoStringFromBravo"]).isEqualTo("lost")
        assertThat(getDoc(bravoIndex, kept.id).found()).isTrue()
        assertThat(getDoc(bravoIndex, bogusId).found()).isFalse()

        refreshIndexSegments(bravoIndex)
        assertThat(elasticsearchClient.count { r -> r.index(bravoIndex.value) }.count()).isEqualTo(2)

    }


    // ─── LeftManyEs: many-to-many ─────────────────────────────────────────────
    // LeftMany and RightMany updates refresh on every update by design (the m2m membership is not a field
    // of either entity), so there is deliberately no "unwatched field causes no write" test for them.


    @Test
    fun `creating a LeftMany with two rights indexes both rights`() {

        val right1 = createRightMany("right-1")
        val right2 = createRightMany("right-2")

        val left = createLeftMany("left", right1, right2)

        assertThat(rightEntitiesOf(left.id)).containsExactlyInAnyOrder(
            right1.id.value to "right-1",
            right2.id.value to "right-2"
        )
        assertThat(getDoc(leftManyIndex, left.id).source()!!["someStringFromLeft"]).isEqualTo("left")

    }


    @Test
    fun `renaming a RightMany refreshes the LeftMany docs that reference it`() {

        val right1 = createRightMany("right-1")
        val right2 = createRightMany("right-2")
        val left = createLeftMany("left", right1, right2)
        val unrelatedLeft = createLeftMany("unrelated-left", right2)
        val unrelatedVersion = getDoc(leftManyIndex, unrelatedLeft.id).version()

        rightManyCrudService.updateSomeString(
            RightManyUpdate_someStringRequestDto(
                id_raw = right1.id,
                someString_raw = "right-1-renamed",
                version_raw = rightManyDao.findByPrimaryKey(right1.id).version
            )
        )

        assertThat(rightEntitiesOf(left.id)).containsExactlyInAnyOrder(
            right1.id.value to "right-1-renamed",
            right2.id.value to "right-2"
        )
        assertThat(getDoc(leftManyIndex, unrelatedLeft.id).version()).isEqualTo(unrelatedVersion)

    }


    @Test
    fun `removing a link by updating the LeftMany refreshes its doc`() {

        val right1 = createRightMany("right-1")
        val right2 = createRightMany("right-2")
        val left = createLeftMany("left", right1, right2)

        val keptJoin = leftToRightComplexDao.findEffectiveByLeft(left.id).single { it.right == right2.id }
        val leftEntity = leftManyDao.findByPrimaryKey(left.id)

        leftManyCrudService.update(
            LeftManyUpdateRequestDto(
                id_raw = left.id,
                rightEntities_raw = listOf(
                    RightJoinRequestDto(
                        effectiveFrom = keptJoin.effectiveFrom,
                        effectiveTo = keptJoin.effectiveTo,
                        id = keptJoin.id,
                        rightEntityId_raw = right2.id,
                        someIntOnComplex_raw = keptJoin.someIntOnComplex
                    )
                ),
                rightSimpleEntityIds_raw = emptyList(),
                rightSystemEffectiveEntities_raw = emptyList(),
                rightSystemSingleEffectiveEntities_raw = emptyList(),
                rightUserEffectiveEntities_raw = emptyList(),
                someInt_raw = leftEntity.someInt,
                someString_raw = leftEntity.someString,
                version_raw = leftEntity.version
            )
        )

        assertThat(rightEntitiesOf(left.id)).containsExactly(right2.id.value to "right-2")

    }


    @Test
    fun `editing membership from the RightMany side refreshes both the left that lost and the left that gained it`() {

        val right = createRightMany("right")
        val losingLeft = createLeftMany("losing-left", right)
        val gainingLeft = createLeftMany("gaining-left")

        assertThat(rightEntitiesOf(losingLeft.id)).containsExactly(right.id.value to "right")
        assertThat(rightEntitiesOf(gainingLeft.id)).isEmpty()

        val rightEntity = rightManyDao.findByPrimaryKey(right.id)

        rightManyCrudService.update(
            RightManyUpdateRequestDto(
                id_raw = right.id,
                leftEntities_raw = listOf(
                    LeftJoinRequestDto(
                        effectiveFrom = null,
                        effectiveTo = null,
                        id = null,
                        leftEntityId_raw = gainingLeft.id,
                        someIntOnComplex_raw = 7
                    )
                ),
                leftSimpleEntityIds_raw = emptyList(),
                leftSystemEffectiveEntities_raw = emptyList(),
                leftSystemSingleEffectiveEntities_raw = emptyList(),
                leftUserEffectiveEntities_raw = emptyList(),
                someInt_raw = rightEntity.someInt,
                someString_raw = rightEntity.someString,
                version_raw = rightEntity.version
            )
        )

        assertThat(rightEntitiesOf(losingLeft.id)).isEmpty()
        assertThat(rightEntitiesOf(gainingLeft.id)).containsExactly(right.id.value to "right")

    }


    @Test
    fun `deleting a LeftMany removes its document`() {

        val left = createLeftMany("left")
        assertThat(getDoc(leftManyIndex, left.id).found()).isTrue()

        leftManyCrudService.delete(left.id)

        assertThat(leftManyDao.findByPrimaryKeyOrNull(left.id)).isNull()
        assertThat(getDoc(leftManyIndex, left.id).found()).isFalse()

    }


    @Test
    fun `a join row inserted directly through the DAO raises no event and is reconciled only by refreshIndex`() {

        val right = createRightMany("right")
        val left = createLeftMany("left")

        leftToRightComplexDao.insert(
            LeftToRightComplexEntity.newInstance(
                effectiveFrom = Instant.now(),
                effectiveTo = null,
                left = left.id,
                right = right.id,
                someIntOnComplex = 1
            )
        )

        // documented limitation: no CrudService event, so the doc is stale until the scheduled job runs
        assertThat(rightEntitiesOf(left.id)).isEmpty()

        leftManyEsIndexService.refreshIndex(JobMetrics("test-refreshLeftManyEsV1Index"))

        assertThat(rightEntitiesOf(left.id)).containsExactly(right.id.value to "right")

    }


    // ─── Index creation and blotter filtering ─────────────────────────────────


    @Test
    fun `a refresh into a missing index creates it from the generated mapping instead of a dynamic one`() {

        val alpha = createAlpha("Alpha One")
        elasticsearchClient.indices().delete { r -> r.index(bravoIndex.value) }
        assertThat(indexExists(bravoIndex)).isFalse()

        val bravo = createBravo(alpha, someString = "bravo-one")

        assertThat(indexExists(bravoIndex)).isTrue()
        assertThat(getDoc(bravoIndex, bravo.id).source()!!["dtoStringFromBravo"]).isEqualTo("bravo-one")

        val properties = elasticsearchClient.indices().getMapping { r -> r.index(bravoIndex.value) }
            .get(bravoIndex.value)!!.mappings().properties()

        // a dynamic mapping would give text plus a sub-field named "keyword", and no date mapping
        assertThat(properties.getValue("dtoStringFromBravo").text().fields()).containsKey("raw")
        assertThat(properties.getValue("createdTimestamp").isDate).isTrue()

    }


    @Test
    fun `the refreshIndex job also creates a missing index from the generated mapping`() {

        val alpha = createAlpha("alpha-one")
        createBravo(alpha)
        elasticsearchClient.indices().delete { r -> r.index(bravoIndex.value) }

        bravoEsIndexService.refreshIndex(JobMetrics("test-refreshBravoEsV1Index"))

        val properties = elasticsearchClient.indices().getMapping { r -> r.index(bravoIndex.value) }
            .get(bravoIndex.value)!!.mappings().properties()
        assertThat(properties.getValue("dtoStringFromBravo").text().fields()).containsKey("raw")

    }


    @Test
    fun `the blotter filters a multi-word mixed-case string exactly and sorts by it`() {

        val alphaOne = createAlpha("Alpha One")
        val alphaTwo = createAlpha("Alpha Two")
        val bravoOne = createBravo(alphaOne, someString = "Bravo Zulu")
        createBravo(alphaTwo, someString = "Bravo Alpha")
        refreshIndexSegments(bravoIndex)

        val filter = JsonNodeFactory.instance.objectNode().apply {
            putObject("dtoStringFromAlpha").put("filterType", "text").put("type", "equals").put("filter", "Alpha One")
        }

        val filtered = bravoEsSearchService.search(AgGridSearchModel(filter, emptyList(), 0, 10))
        assertThat(filtered.hits.map { it.id }).containsExactly(bravoOne.id)
        assertThat(bravoEsSearchService.count(AgGridSearchModel(filter, emptyList(), 0, 10))).isEqualTo(1)

        val sorted = bravoEsSearchService.search(
            AgGridSearchModel(JsonNodeFactory.instance.objectNode(), listOf(AgGridSortModelItem("dtoStringFromBravo", "asc")), 0, 10)
        )
        assertThat(sorted.hits.map { it.dtoStringFromBravo }).containsExactly("Bravo Alpha", "Bravo Zulu")

    }


    // ─── Several live versions ────────────────────────────────────────────────


    @Test
    fun `every live version of a document is refreshed when a contributing entity changes`() {

        val right = createRightMany("right")
        val left = createLeftMany("left", right)
        assertThat(rightEntitiesOf(left.id, leftManyIndex)).containsExactly(right.id.value to "right")

        val rightEntity = rightManyDao.findByPrimaryKey(right.id)
        Mockito.clearInvocations(esIndexOps)

        rightManyCrudService.updateSomeString(
            RightManyUpdate_someStringRequestDto(
                id_raw = right.id,
                someString_raw = "right-renamed",
                version_raw = rightEntity.version
            )
        )

        assertThat(rightEntitiesOf(left.id, leftManyIndex)).containsExactly(right.id.value to "right-renamed")
        assertThat(bulkUpsertCallsFor(leftManyIndex)).hasSize(1)

    }


    // ─── helpers ──────────────────────────────────────────────────────────────


    private fun createAlpha(someString: String, someInt: Int = 1): AlphaEntity {

        return alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = someInt, someString_raw = someString))

    }


    private fun createBravo(alpha: AlphaEntity, someString: String = "bravo", someInt: Int = 1): BravoEntity {

        return bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = someInt, someString_raw = someString))

    }


    private fun createRightMany(someString: String): RightManyEntity {

        return rightManyCrudService.create(
            RightManyCreateRequestDto(
                leftEntities_raw = emptyList(),
                leftSimpleEntityIds_raw = emptyList(),
                leftSystemEffectiveEntities_raw = emptyList(),
                leftSystemSingleEffectiveEntities_raw = emptyList(),
                leftUserEffectiveEntities_raw = emptyList(),
                someInt_raw = 1,
                someString_raw = someString
            )
        )

    }


    private fun createLeftMany(someString: String, vararg rights: RightManyEntity): LeftManyEntity {

        return leftManyCrudService.create(
            LeftManyCreateRequestDto(
                rightEntities_raw = rights.map { right ->
                    RightJoinRequestDto(
                        effectiveFrom = null,
                        effectiveTo = null,
                        id = null,
                        rightEntityId_raw = right.id,
                        someIntOnComplex_raw = 1
                    )
                },
                rightSimpleEntityIds_raw = emptyList(),
                rightSystemEffectiveEntities_raw = emptyList(),
                rightSystemSingleEffectiveEntities_raw = emptyList(),
                rightUserEffectiveEntities_raw = emptyList(),
                someInt_raw = 1,
                someString_raw = someString
            )
        )

    }


    private fun rightEntitiesOf(leftId: DomainId, index: IndexResolvedName = leftManyIndex): List<Pair<Any?, Any?>> {

        val doc = getDoc(index, leftId)
        assertThat(doc.found()).describedAs("LeftManyEs doc $leftId exists in ${index.value}").isTrue()

        @Suppress("UNCHECKED_CAST")
        val rights = doc.source()!!["rightEntities"] as List<Map<String, Any?>>
        return rights.map { it["id"] to it["name"] }

    }


    @Suppress("UNCHECKED_CAST")
    private fun getDoc(index: IndexResolvedName, id: DomainId): GetResponse<Map<String, Any?>> {

        return elasticsearchClient.get(
            { r: GetRequest.Builder -> r.index(index.value).id(id.value) },
            Map::class.java
        ) as GetResponse<Map<String, Any?>>

    }


    private fun indexExists(index: IndexResolvedName): Boolean {

        return elasticsearchClient.indices().exists { r -> r.index(index.value) }.value()

    }


    private fun recreateIndex(index: IndexResolvedName, create: () -> Unit) {

        if (indexExists(index)) {
            elasticsearchClient.indices().delete { r -> r.index(index.value) }
        }

        create()

    }


    private fun refreshIndexSegments(index: IndexResolvedName) {

        elasticsearchClient.indices().refresh { r -> r.index(index.value) }

    }


    /**
     * The item lists of every `bulkUpsert` call on the [EsIndexOps] spy whose items target [index].
     */
    private fun bulkUpsertCallsFor(index: IndexResolvedName): List<List<EsDocHolder<*>>> {

        return Mockito.mockingDetails(esIndexOps).invocations
            .filter { it.method.name == "bulkUpsert" }
            .map { invocation ->
                @Suppress("UNCHECKED_CAST")
                invocation.arguments[0] as List<EsDocHolder<*>>
            }
            .filter { items -> items.any { it.indexName == index } }

    }


    /**
     * Counts every write call (bulkUpsert, upsert, deleteById, deleteByIds) on the [EsIndexOps] spy that
     * targets [index].
     */
    private fun writeCallsFor(index: IndexResolvedName): Int {

        return Mockito.mockingDetails(esIndexOps).invocations.count { invocation ->

            val args = invocation.arguments

            when (invocation.method.name) {
                "bulkUpsert" -> (args[0] as List<*>).any { (it as EsDocHolder<*>).indexName == index }
                "upsert" -> (args[0] as EsDocHolder<*>).indexName == index
                "deleteById", "deleteByIds" -> args[1] == index
                else -> false
            }

        }

    }


}

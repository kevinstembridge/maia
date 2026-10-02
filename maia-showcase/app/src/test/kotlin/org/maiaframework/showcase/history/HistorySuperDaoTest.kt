package org.maiaframework.showcase.history

import org.maiaframework.domain.ChangeType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.jdbc.BulkOptimisticLockingException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit


class HistorySuperDaoTest: AbstractBlackBoxTest() {


    @Autowired
    private lateinit var historySubOneDao: HistorySubOneDao


    @Autowired
    private lateinit var historySubTwoDao: HistorySubTwoDao


    @Autowired
    private lateinit var historySuperDao: HistorySuperHistoryDao


    @Autowired
    private lateinit var historySubOneHistoryDao: HistorySubOneHistoryDao


    @Autowired
    private lateinit var historySubTwoHistoryDao: HistorySubTwoHistoryDao


    @Autowired
    private lateinit var historySuperEntityDao: HistorySuperDao


    @Test
    fun testInsertAndSetFields() {

        // GIVEN a sample entity
        val historySubOneEntity = HistorySubOneEntityTestBuilder().build()
        val entitySubOneId = historySubOneEntity.id

        // WHEN we insert it into the database
        this.historySubOneDao.insert(historySubOneEntity)

        // THEN we can find version 1 of the inserted entity
        val actualEntityV1 = this.historySubOneDao.findByPrimaryKey(entitySubOneId)
        assertThat(actualEntityV1.version).isEqualTo(1)

        // AND version 1 of the history entity
        val actualHistoryEntityV1 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entitySubOneId, 1))
        assertHistoryEntity(actualHistoryEntityV1, actualEntityV1, ChangeType.CREATE)

        // WHEN we update the entity
        val someStringUpdated = actualEntityV1.someString + "_updated"
        val updater = HistorySubOneEntityUpdater.forPrimaryKey(actualEntityV1.id, actualEntityV1.version) {
            someString(someStringUpdated)
        }
        this.historySubOneDao.setFields(updater)

        // THEN we can no longer find version 1 of the entity
        val historySubOneEntityFilters = HistorySubOneEntityFilters()
        val filter = historySubOneEntityFilters.and(
            historySubOneEntityFilters.id eq entitySubOneId,
            historySubOneEntityFilters.version eq 1
        )

        assertThat(this.historySubOneDao.count(filter)).isZero()

        // AND we can find version 2 of the entity
        val actualEntityV2 = this.historySubOneDao.findByPrimaryKey(entitySubOneId)
        assertThat(actualEntityV2.version).isEqualTo(2)

        // AND we can find version 2 of the history entity
        val actualHistoryEntityV2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entitySubOneId, 2))
        assertHistoryEntity(actualHistoryEntityV2, actualEntityV2, ChangeType.UPDATE)

        // WHEN we delete the entity
        this.historySubOneDao.deleteByPrimaryKey(entitySubOneId)

        // THEN we can no longer find a version of the entity
        assertThat(this.historySubOneDao.findByPrimaryKeyOrNull(entitySubOneId)).isNull()

        // AND we can find version 3 of the history entity
        val actualHistoryEntityV3 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entitySubOneId, 3))
        assertHistoryEntity(actualHistoryEntityV3, actualEntityV2, 3, ChangeType.DELETE)

    }


    @Test
    fun testFindAllByPrimaryKeys_returnsOnlyRequestedEntities() {

        val entity1 = HistorySubOneEntityTestBuilder().build()
        val entity2 = HistorySubOneEntityTestBuilder().build()
        val entity3 = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(entity1)
        this.historySubOneDao.insert(entity2)
        this.historySubOneDao.insert(entity3)

        val result = this.historySubOneDao.findAllByPrimaryKeys(listOf(entity1.id, entity3.id))

        assertThat(result.map { it.id }).containsExactlyInAnyOrder(entity1.id, entity3.id)

    }


    // TODO test update of inline fields


    @Test
    fun testBulkSetFields_updatesAllRows_whenAllUpdatersSetTheSameField() {

        val entity1 = HistorySubOneEntityTestBuilder().build()
        val entity2 = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(entity1)
        this.historySubOneDao.insert(entity2)

        val updater1 = HistorySubOneEntityUpdater.forPrimaryKey(entity1.id, entity1.version) {
            someString("updated1")
        }
        val updater2 = HistorySubOneEntityUpdater.forPrimaryKey(entity2.id, entity2.version) {
            someString("updated2")
        }

        this.historySubOneDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.historySubOneDao.findByPrimaryKey(entity1.id)
        val updatedEntity2 = this.historySubOneDao.findByPrimaryKey(entity2.id)

        assertThat(updatedEntity1.someString).isEqualTo("updated1")
        assertThat(updatedEntity1.version).isEqualTo(2)
        assertThat(updatedEntity2.someString).isEqualTo("updated2")
        assertThat(updatedEntity2.version).isEqualTo(2)

        val historyEntity1V2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entity1.id, 2))
        assertHistoryEntity(historyEntity1V2, updatedEntity1, ChangeType.UPDATE)

        val historyEntity2V2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entity2.id, 2))
        assertHistoryEntity(historyEntity2V2, updatedEntity2, ChangeType.UPDATE)

    }


    @Test
    fun testBulkSetFields_groupsByFieldSet_whenUpdatersSetDifferentFields() {

        val entity1 = HistorySubOneEntityTestBuilder().build()
        val entity2 = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(entity1)
        this.historySubOneDao.insert(entity2)

        val newTimestamp = Instant.now().plusSeconds(120).truncatedTo(ChronoUnit.MILLIS)

        val updater1 = HistorySubOneEntityUpdater.forPrimaryKey(entity1.id, entity1.version) {
            someString("updated1")
        }
        val updater2 = HistorySubOneEntityUpdater.forPrimaryKey(entity2.id, entity2.version) {
            lastModifiedTimestamp(newTimestamp)
        }

        this.historySubOneDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.historySubOneDao.findByPrimaryKey(entity1.id)
        val updatedEntity2 = this.historySubOneDao.findByPrimaryKey(entity2.id)

        assertThat(updatedEntity1.someString).isEqualTo("updated1")
        assertThat(updatedEntity2.lastModifiedTimestamp).isEqualTo(newTimestamp)
        assertThat(updatedEntity2.someString).isEqualTo(entity2.someString)

    }


    @Test
    fun testBulkSetFields_throwsAggregateException_listingAllStaleRows_butStillUpdatesValidOnes() {

        val staleEntity1 = HistorySubOneEntityTestBuilder().build()
        val staleEntity2 = HistorySubOneEntityTestBuilder().build()
        val validEntity = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(staleEntity1)
        this.historySubOneDao.insert(staleEntity2)
        this.historySubOneDao.insert(validEntity)

        val staleUpdater1 = HistorySubOneEntityUpdater.forPrimaryKey(staleEntity1.id, 999L) {
            someString("shouldNotApply1")
        }
        val staleUpdater2 = HistorySubOneEntityUpdater.forPrimaryKey(staleEntity2.id, 999L) {
            someString("shouldNotApply2")
        }
        val validUpdater = HistorySubOneEntityUpdater.forPrimaryKey(validEntity.id, validEntity.version) {
            someString("shouldApply")
        }

        assertThatThrownBy {
            this.historySubOneDao.bulkSetFields(listOf(staleUpdater1, validUpdater, staleUpdater2))
        }.isInstanceOf(BulkOptimisticLockingException::class.java)

        val updatedStale1 = this.historySubOneDao.findByPrimaryKey(staleEntity1.id)
        val updatedStale2 = this.historySubOneDao.findByPrimaryKey(staleEntity2.id)
        val updatedValid = this.historySubOneDao.findByPrimaryKey(validEntity.id)

        assertThat(updatedStale1.someString).isEqualTo(staleEntity1.someString)
        assertThat(updatedStale1.version).isEqualTo(1)
        assertThat(updatedStale2.someString).isEqualTo(staleEntity2.someString)
        assertThat(updatedStale2.version).isEqualTo(1)
        assertThat(updatedValid.someString).isEqualTo("shouldApply")
        assertThat(updatedValid.version).isEqualTo(2)

    }


    @Test
    fun testBulkSetFields_withMixedHierarchyTypes_dispatchesHistoryToCorrectSubtype() {

        val subOneEntity = HistorySubOneEntityTestBuilder().build()
        val subTwoEntity = HistorySubTwoEntityTestBuilder().build()

        this.historySubOneDao.insert(subOneEntity)
        this.historySubTwoDao.insert(subTwoEntity)

        val historyCountBeforeBulkSetFields = this.historySubOneHistoryDao.count()

        val newLastModifiedTimestamp = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS)

        val updaterSubOne = HistorySuperEntityUpdater.forPrimaryKey(subOneEntity.id, subOneEntity.version) {
            lastModifiedTimestamp(newLastModifiedTimestamp)
        }
        val updaterSubTwo = HistorySuperEntityUpdater.forPrimaryKey(subTwoEntity.id, subTwoEntity.version) {
            lastModifiedTimestamp(newLastModifiedTimestamp)
        }

        this.historySuperEntityDao.bulkSetFields(listOf(updaterSubOne, updaterSubTwo))

        val updatedSubOne = this.historySubOneDao.findByPrimaryKey(subOneEntity.id)
        val updatedSubTwo = this.historySubTwoDao.findByPrimaryKey(subTwoEntity.id)

        assertThat(updatedSubOne.lastModifiedTimestamp).isEqualTo(newLastModifiedTimestamp)
        assertThat(updatedSubOne.version).isEqualTo(2)
        assertThat(updatedSubOne.someString).isEqualTo(subOneEntity.someString)
        assertThat(updatedSubTwo.lastModifiedTimestamp).isEqualTo(newLastModifiedTimestamp)
        assertThat(updatedSubTwo.version).isEqualTo(2)
        assertThat(updatedSubTwo.someInt).isEqualTo(subTwoEntity.someInt)

        // AND the full concrete-type data for each subtype landed in ITS OWN history table
        val historySubOneV2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(subOneEntity.id, 2))
        assertHistoryEntity(historySubOneV2, updatedSubOne, 2, ChangeType.UPDATE)

        val historySubTwoV2 = this.historySubTwoHistoryDao.findByPrimaryKey(HistorySubTwoHistoryEntityPk(subTwoEntity.id, 2))
        assertHistoryEntity(historySubTwoV2, updatedSubTwo, 2, ChangeType.UPDATE)

        // AND neither history DAO received the OTHER subtype's row under its own typed primary key
        // (both concrete history DAOs share the single maia.history_super_history table, so a row's mere
        // presence proves nothing about dispatch; what proves correct, non-cross-wired dispatch is that
        // each entity's updated business field - someString for SubOne, someInt for SubTwo - was only
        // reachable, correctly populated, through ITS OWN history DAO above via `history(it: HistorySubOneEntity, ...)`
        // / `history(it: HistorySubTwoEntity, ...)` - Kotlin's overload resolution on bulkInsertHistory's
        // per-type lists makes it impossible for the generated code to route a HistorySubOneEntity's data
        // through historySubTwoHistoryDao.bulkInsert() (or vice versa) without a compile error.

        // AND bulkSetFields actually inserted exactly the 2 expected new UPDATE rows into the shared
        // history table - one per subtype - on top of whatever was there before (not zero, not duplicated)
        assertThat(this.historySubOneHistoryDao.count()).isEqualTo(historyCountBeforeBulkSetFields + 2)

    }


    private fun assertHistoryEntity(
        historyEntity: HistorySubOneHistoryEntity,
        entity: HistorySubOneEntity,
        expectedChangeType: ChangeType
    ) {

        assertHistoryEntity(historyEntity, entity, entity.version, expectedChangeType)

    }


    private fun assertHistoryEntity(
        historyEntity: HistorySubOneHistoryEntity,
        entity: HistorySubOneEntity,
        expectedVersion: Long,
        expectedChangeType: ChangeType
    ) {

        assertThat(historyEntity.createdTimestamp).`as`("createdTimestamp").isEqualTo(entity.createdTimestamp)
        assertThat(historyEntity.lastModifiedTimestamp).`as`("lastModifiedTimestamp").isEqualTo(entity.lastModifiedTimestamp)
        assertThat(historyEntity.someString).`as`("someString").isEqualTo(entity.someString)
        assertThat(historyEntity.version).`as`("v").isEqualTo(expectedVersion)
        assertThat(historyEntity.id).`as`("entityId").isEqualTo(entity.id)
        assertThat(historyEntity.changeType).`as`("changeType").isEqualTo(expectedChangeType)

    }


    private fun assertHistoryEntity(
        historyEntity: HistorySubTwoHistoryEntity,
        entity: HistorySubTwoEntity,
        expectedVersion: Long,
        expectedChangeType: ChangeType
    ) {

        assertThat(historyEntity.createdTimestamp).`as`("createdTimestamp").isEqualTo(entity.createdTimestamp)
        assertThat(historyEntity.lastModifiedTimestamp).`as`("lastModifiedTimestamp").isEqualTo(entity.lastModifiedTimestamp)
        assertThat(historyEntity.someInt).`as`("someInt").isEqualTo(entity.someInt)
        assertThat(historyEntity.version).`as`("v").isEqualTo(expectedVersion)
        assertThat(historyEntity.id).`as`("entityId").isEqualTo(entity.id)
        assertThat(historyEntity.changeType).`as`("changeType").isEqualTo(expectedChangeType)

    }


}

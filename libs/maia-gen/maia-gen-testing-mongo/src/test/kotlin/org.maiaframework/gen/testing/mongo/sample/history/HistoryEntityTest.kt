package mahana.gen.testing.mongo.sample.history

import org.maiaframework.domain.ChangeType
import org.maiaframework.gen.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.Test


class HistoryEntityTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var dao: HistorySampleDao

    @Autowired
    private lateinit var historyDao: HistorySampleHistoryDao


    @Test
    fun testInsertAndSetFields() {

        // GIVEN a sample entity
        val historySampleEntity = HistorySampleEntityTestBuilder().build()
        val entityId = historySampleEntity.id

        // WHEN we insert it into the database
        this.dao.insert(historySampleEntity)

        // THEN we can find version 1 of the inserted entity
        val actualEntityV1 = this.dao.findById(entityId)
        assertThat(actualEntityV1.version).isEqualTo(1)

        // AND version 1 of the history entity
        val actualHistoryEntityV1 = this.historyDao.findOneByEntityIdAndVersion(entityId, 1)
        assertHistoryEntity(actualHistoryEntityV1, actualEntityV1, ChangeType.CREATE)

        // WHEN we update the entity
        val someIntUpdated = actualEntityV1.someInt + 1
        val someStringUpdated = actualEntityV1.someString + "_updated"
        val updater = HistorySampleEntityUpdater.forIdAndVersion(actualEntityV1.idAndVersion) {
            someInt(someIntUpdated)
            someString(someStringUpdated)
        }.build()
        this.dao.setFields(updater)

        // THEN we can no longer find version 1 of the entity
        val filter = HistorySampleEntityFilters.and(HistorySampleEntityFilters.id().eq(entityId), HistorySampleEntityFilters.version().eq(1))
        assertThat(this.dao.count(filter)).isZero()

        // AND we can find version 2 of the entity
        val actualEntityV2 = this.dao.findById(entityId)
        assertThat(actualEntityV2.version).isEqualTo(2)

        // AND we can find version 2 of the history entity
        val actualHistoryEntityV2 = this.historyDao.findOneByEntityIdAndVersion(entityId, 2)
        assertHistoryEntity(actualHistoryEntityV2, actualEntityV2, ChangeType.UPDATE)

        // WHEN we delete the entity
        this.dao.deleteById(entityId)

        // THEN we can no longer find a version of the entity
        assertThat(this.dao.findByIdOrNull(entityId)).isNull()

        // AND we can find version 3 of the history entity
        val actualHistoryEntityV3 = this.historyDao.findOneByEntityIdAndVersion(entityId, 3)
        assertHistoryEntity(actualHistoryEntityV3, actualEntityV2, 3, ChangeType.DELETE)

    }


    @Test
    fun testUpsertByUniqueFields() {

        // GIVEN a sample entity
        val historySampleEntity = HistorySampleEntityTestBuilder().build()
        val entityId = historySampleEntity.id

        // WHEN we insert it into the database
        this.dao.insert(historySampleEntity)

        // THEN we can find version 1 of the inserted entity
        val actualEntityV1 = this.dao.findById(entityId)
        assertThat(actualEntityV1.version).isEqualTo(1)

        // AND version 1 of the history entity
        val actualHistoryEntityV1 = this.historyDao.findOneByEntityIdAndVersion(entityId, 1)
        assertHistoryEntity(actualHistoryEntityV1, actualEntityV1, ChangeType.CREATE)

        // WHEN we update the entity
        val someIntUpdated = actualEntityV1.someInt + 1

        val entityToUpsert = HistorySampleEntity(
            actualEntityV1.createdTimestampUtc,
            actualEntityV1.id,
            someIntUpdated,
            actualEntityV1.someString,
            actualEntityV1.version
        )

        this.dao.upsertBySomeString(entityToUpsert)

        // THEN we can no longer find version 1 of the entity
        val filter = HistorySampleEntityFilters.and(HistorySampleEntityFilters.id().eq(entityId), HistorySampleEntityFilters.version().eq(1))
        assertThat(this.dao.count(filter)).isZero()

        // AND we can find version 2 of the entity
        val actualEntityV2 = this.dao.findById(entityId)
        assertThat(actualEntityV2.version).isEqualTo(2)

        // AND we can find version 2 of the history entity
        val actualHistoryEntityV2 = this.historyDao.findOneByEntityIdAndVersion(entityId, 2)
        assertHistoryEntity(actualHistoryEntityV2, actualEntityV2, ChangeType.UPDATE)

    }

    // TODO test update of inline fields


    private fun assertHistoryEntity(
        historyEntity: HistorySampleHistoryEntity,
        entity: HistorySampleEntity,
        expectedChangeType: ChangeType
    ) {

        assertHistoryEntity(historyEntity, entity, entity.version, expectedChangeType)

    }


    private fun assertHistoryEntity(
        historyEntity: HistorySampleHistoryEntity,
        entity: HistorySampleEntity,
        expectedVersion: Long,
        expectedChangeType: ChangeType
    ) {

        assertThat(historyEntity.createdTimestampUtc).isEqualTo(entity.createdTimestampUtc)
        assertThat(historyEntity.someInt).isEqualTo(entity.someInt)
        assertThat(historyEntity.someString).isEqualTo(entity.someString)
        assertThat(historyEntity.version).isEqualTo(expectedVersion)
        assertThat(historyEntity.entityId).isEqualTo(entity.id)
        assertThat(historyEntity.changeType).isEqualTo(expectedChangeType)

    }


}

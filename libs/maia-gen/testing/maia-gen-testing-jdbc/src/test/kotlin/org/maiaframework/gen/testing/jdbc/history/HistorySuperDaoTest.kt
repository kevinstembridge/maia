package org.maiaframework.gen.testing.jdbc.history

import org.maiaframework.domain.ChangeType
import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.sample.history.HistorySubOneDao
import org.maiaframework.gen.testing.sample.history.HistorySubOneEntity
import org.maiaframework.gen.testing.sample.history.HistorySubOneEntityFilters
import org.maiaframework.gen.testing.sample.history.HistorySubOneEntityUpdater
import org.maiaframework.gen.testing.sample.history.HistorySubOneHistoryDao
import org.maiaframework.gen.testing.sample.history.HistorySubOneHistoryEntity
import org.maiaframework.gen.testing.sample.history.HistorySubTwoDao
import org.maiaframework.gen.testing.sample.history.HistorySubTwoEntity
import org.maiaframework.gen.testing.sample.history.HistorySubTwoHistoryDao
import org.maiaframework.gen.testing.sample.history.HistorySubTwoHistoryEntity
import org.maiaframework.gen.testing.sample.history.HistorySuperHistoryDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class HistorySuperDaoTest: AbstractJdbcTest() {


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
        val actualHistoryEntityV1 = this.historySubOneHistoryDao.findByPrimaryKey(entitySubOneId, 1)
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
        val actualHistoryEntityV2 = this.historySubOneHistoryDao.findByPrimaryKey(entitySubOneId, 2)
        assertHistoryEntity(actualHistoryEntityV2, actualEntityV2, ChangeType.UPDATE)

        // WHEN we delete the entity
        this.historySubOneDao.deleteByPrimaryKey(entitySubOneId)

        // THEN we can no longer find a version of the entity
        assertThat(this.historySubOneDao.findByPrimaryKeyOrNull(entitySubOneId)).isNull()

        // AND we can find version 3 of the history entity
        val actualHistoryEntityV3 = this.historySubOneHistoryDao.findByPrimaryKey(entitySubOneId, 3)
        assertHistoryEntity(actualHistoryEntityV3, actualEntityV2, 3, ChangeType.DELETE)

    }


    // TODO test update of inline fields


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

        assertThat(historyEntity.createdTimestampUtc).`as`("createdTimestampUtc").isEqualTo(entity.createdTimestampUtc)
        assertThat(historyEntity.lastModifiedTimestampUtc).`as`("lastModifiedTimestampUtc").isEqualTo(entity.lastModifiedTimestampUtc)
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

        assertThat(historyEntity.createdTimestampUtc).`as`("createdTimestampUtc").isEqualTo(entity.createdTimestampUtc)
        assertThat(historyEntity.lastModifiedTimestampUtc).`as`("lastModifiedTimestampUtc").isEqualTo(entity.lastModifiedTimestampUtc)
        assertThat(historyEntity.someInt).`as`("someInt").isEqualTo(entity.someInt)
        assertThat(historyEntity.version).`as`("v").isEqualTo(expectedVersion)
        assertThat(historyEntity.id).`as`("entityId").isEqualTo(entity.id)
        assertThat(historyEntity.changeType).`as`("changeType").isEqualTo(expectedChangeType)

    }


}

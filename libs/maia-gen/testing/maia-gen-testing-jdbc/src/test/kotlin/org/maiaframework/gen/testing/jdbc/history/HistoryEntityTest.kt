package org.maiaframework.gen.testing.jdbc.history

import org.maiaframework.domain.ChangeType
import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.sample.history.HistorySampleDao
import org.maiaframework.gen.testing.sample.history.HistorySampleEntity
import org.maiaframework.gen.testing.sample.history.HistorySampleEntityFilters
import org.maiaframework.gen.testing.sample.history.HistorySampleEntityUpdater
import org.maiaframework.gen.testing.sample.history.HistorySampleHistoryDao
import org.maiaframework.gen.testing.sample.history.HistorySampleHistoryEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class HistoryEntityTest: AbstractJdbcTest() {


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
        val actualEntityV1 = this.dao.findByPrimaryKey(entityId)
        assertThat(actualEntityV1.version).isEqualTo(1)

        // AND version 1 of the history entity
        val actualHistoryEntityV1 = this.historyDao.findByPrimaryKey(entityId, 1)
        assertHistoryEntity(actualHistoryEntityV1, actualEntityV1, ChangeType.CREATE)

        // WHEN we update the entity
        val someIntUpdated = actualEntityV1.someInt + 1
        val someStringUpdated = actualEntityV1.someString + "_updated"
        val updater = HistorySampleEntityUpdater.forPrimaryKey(actualEntityV1.id, actualEntityV1.version) {
            someInt(someIntUpdated)
            someString(someStringUpdated)
        }
        this.dao.setFields(updater)

        // THEN we can no longer find version 1 of the entity
        val historySampleEntityFilters = HistorySampleEntityFilters()
        val filter = historySampleEntityFilters.and(
            historySampleEntityFilters.id eq entityId,
            historySampleEntityFilters.version eq 1
        )

        assertThat(this.dao.count(filter)).isZero()

        // AND we can find version 2 of the entity
        val actualEntityV2 = this.dao.findByPrimaryKey(entityId)
        assertThat(actualEntityV2.version).isEqualTo(2)

        // AND we can find version 2 of the history entity
        val actualHistoryEntityV2 = this.historyDao.findByPrimaryKey(entityId, 2)
        assertHistoryEntity(actualHistoryEntityV2, actualEntityV2, ChangeType.UPDATE)

        // WHEN we delete the entity
        this.dao.deleteByPrimaryKey(entityId)

        // THEN we can no longer find a version of the entity
        assertThat(this.dao.findByPrimaryKeyOrNull(entityId)).isNull()

        // AND we can find version 3 of the history entity
        val actualHistoryEntityV3 = this.historyDao.findByPrimaryKey(entityId, 3)
        assertHistoryEntity(actualHistoryEntityV3, actualEntityV2, 3, ChangeType.DELETE)

    }


    @Test
    fun testUpsertByUniqueFields() {

        // GIVEN a sample entity
        val entity = HistorySampleEntityTestBuilder().build()
        val entityId = entity.id

        // WHEN we insert it into the database
        this.dao.insert(entity)

        // THEN we can find version 1 of the inserted entity
        val actualEntityV1 = this.dao.findByPrimaryKey(entityId)
        assertThat(actualEntityV1.version).isEqualTo(1)

        // AND version 1 of the history entity
        val actualHistoryEntityV1 = this.historyDao.findByPrimaryKey(entityId, 1)
        assertHistoryEntity(actualHistoryEntityV1, actualEntityV1, ChangeType.CREATE)

        // WHEN we update the entity
        val someIntUpdated = actualEntityV1.someInt + 1

        val entityToUpsert = HistorySampleEntityTestBuilder(
            someString = entity.someString,
            someInt = someIntUpdated,
        ).build()

        val entityResultOfUpsert = this.dao.upsertBySomeString(entityToUpsert)

        assertThat(entityResultOfUpsert.id).isEqualTo(entity.id)
        assertThat(entityResultOfUpsert.version).isEqualTo(2)
        assertThat(entityResultOfUpsert.someInt).isEqualTo(someIntUpdated)

        // THEN we can no longer find version 1 of the entity
        val historySampleEntityFilters = HistorySampleEntityFilters()
        val filterForVersion1 = historySampleEntityFilters.and(
            historySampleEntityFilters.id eq entityId,
            historySampleEntityFilters.version eq 1
        )

        assertThat(this.dao.count(filterForVersion1)).isZero()

        // AND we can find version 2 of the entity
        val actualEntityV2 = this.dao.findByPrimaryKey(entityId)
        assertThat(actualEntityV2.version).isEqualTo(2)

        // AND we can find version 2 of the history entity
        val actualHistoryEntityV2 = this.historyDao.findByPrimaryKey(entityId, 2)
        assertHistoryEntity(actualHistoryEntityV2, actualEntityV2, ChangeType.UPDATE)

    }


    @Test
    @Disabled
    // TODO
    fun testUpsertByIdAndVersion() {

        // GIVEN a sample entity
        val initialVersion = 3L
        val incrementedVersion = initialVersion + 1
        val entity = HistorySampleEntityTestBuilder(version = initialVersion).build()
        val entityId = entity.id

        // WHEN we insert it into the database
        this.dao.insert(entity)

        // THEN we can find the initial version of the inserted entity
        val actualEntityV1 = this.dao.findByPrimaryKey(entityId)
        assertThat(actualEntityV1.version).isEqualTo(initialVersion)

        // AND the initial version of the history entity
        val actualHistoryEntityV1 = this.historyDao.findByPrimaryKey(entityId, initialVersion)
        assertHistoryEntity(actualHistoryEntityV1, actualEntityV1, ChangeType.CREATE)

        // WHEN we update the entity
        val entityToUpsert = HistorySampleEntityTestBuilder(
            id = entityId,
            version = entity.version
        ).build()

//        val entityResultOfUpsert = this.dao.upsertByIdAndVersion(entityToUpsert)

//        assertThat(entityResultOfUpsert.id).isEqualTo(entity.id)
//        assertThat(entityResultOfUpsert.version).isEqualTo(incrementedVersion)
//        assertThat(entityResultOfUpsert.someInt).isEqualTo(entityToUpsert.someInt)
//        assertThat(entityResultOfUpsert.someString).isEqualTo(entityToUpsert.someString)
//        assertThat(entityResultOfUpsert.lastModifiedTimestampUtc).isEqualTo(entityToUpsert.lastModifiedTimestampUtc)

        // THEN we can no longer find the initial version of the entity
        val historySampleEntityFilters = HistorySampleEntityFilters()
        val filterForVersion1 = historySampleEntityFilters.and(
            historySampleEntityFilters.id eq entityId,
            historySampleEntityFilters.version eq initialVersion
        )

        assertThat(this.dao.count(filterForVersion1)).isZero()

        // AND we can find initial version + 1 of the entity
        val actualEntityV2 = this.dao.findByPrimaryKey(entityId)
        assertThat(actualEntityV2.version).isEqualTo(incrementedVersion)

        // AND we can find the incremented version of the history entity
        val actualHistoryEntityV2 = this.historyDao.findByPrimaryKey(entityId, incrementedVersion)
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

        assertThat(historyEntity.createdTimestampUtc).`as`("createdTimestampUtc").isEqualTo(entity.createdTimestampUtc)
        assertThat(historyEntity.lastModifiedTimestampUtc).`as`("lastModifiedTimestampUtc").isEqualTo(entity.lastModifiedTimestampUtc)
        assertThat(historyEntity.someInt).`as`("someInt").isEqualTo(entity.someInt)
        assertThat(historyEntity.someString).`as`("someString").isEqualTo(entity.someString)
        assertThat(historyEntity.version).`as`("v").isEqualTo(expectedVersion)
        assertThat(historyEntity.id).`as`("entityId").isEqualTo(entity.id)
        assertThat(historyEntity.changeType).`as`("changeType").isEqualTo(expectedChangeType)

    }


}

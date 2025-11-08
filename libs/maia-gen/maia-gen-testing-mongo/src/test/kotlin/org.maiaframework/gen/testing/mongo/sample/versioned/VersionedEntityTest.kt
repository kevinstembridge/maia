package mahana.gen.testing.mongo.sample.versioned

import org.maiaframework.dao.mongo.OptimisticLockingException
import org.maiaframework.domain.DomainId
import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class VersionedEntityTest : AbstractIntegrationTest() {


    @Autowired
    private lateinit var dao: SomeVersionedDao


    @Test
    fun testIt() {

        // GIVEN some entity
        val someEntity = SomeVersionedEntity.newInstance(anyInt(), anyString())
        val entityId = someEntity.id

        // WHEN we insert it for the first time
        this.dao.insert(someEntity)

        // THEN it has the correct version number
        val foundEntity = this.dao.findById(entityId)
        assertThat(foundEntity.version).isEqualTo(1)

        // WHEN we update the entity
        val updater1 = SomeVersionedEntityUpdater.forIdAndVersion(entityId, 1L) {
            someInt(foundEntity.someInt + 1)
        }.build()

        this.dao.setFields(updater1)

        // THEN the version number is incremented
        val foundUpdatedEntity = this.dao.findById(entityId)
        assertThat(foundUpdatedEntity.version).isEqualTo(2)

        // WHEN we try to update with the previous version number

        val updater2 = SomeVersionedEntityUpdater.forIdAndVersion(entityId, 1) {
            someInt(foundUpdatedEntity.someInt + 1)
        }.build()

        try {
            this.dao.setFields(updater2)
        } catch (e: OptimisticLockingException) {

            // THEN we get an optimistic locking exception
            assertThat(e.collectionName.value).isEqualTo("some_versioned")
            assertThat(e.id).isEqualTo(entityId)
            assertThat(e.staleVersion).isEqualTo(1)

        }

        // WHEN we try to delete with the previous version number

        try {
            this.dao.deleteByIdAndVersion(entityId, 1)
        } catch (e: OptimisticLockingException) {

            // THEN we get an optimistic locking exception
            assertThat(e.collectionName.value).isEqualTo("some_versioned")
            assertThat(e.id).isEqualTo(entityId)
            assertThat(e.staleVersion).isEqualTo(1)

        }

        // WHEN we delete with the latest version number

        this.dao.deleteByIdAndVersion(entityId, 2)

        // THEN the entity is deleted

        val expectedDeletedEntity: SomeVersionedEntity? = this.dao.findByIdOrNull(entityId)

        assertThat(expectedDeletedEntity).isNull()

    }


    @Test
    fun testUpsert() {

        // GIVEN some entity
        val testStartTime = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val entityId = DomainId.newId()
        val createdTimestampUtcToBeIgnored = Instant.now().minusSeconds(100)
        val lastModifiedTimestampUtcToBeIgnored = createdTimestampUtcToBeIgnored

        val someEntity = SomeVersionedEntity(
            createdTimestampUtcToBeIgnored,
            entityId,
            lastModifiedTimestampUtcToBeIgnored,
            anyInt(),
            anyString(),
            123
        )

        // WHEN we insert it for the first time
        this.dao.upsertBySomeInt(someEntity)

        // THEN it has the correct version number
        val foundEntity = this.dao.findById(entityId)
        assertThat(foundEntity.version).isEqualTo(1)
        // AND the provided createdTimestampUtc and lastModifiedTimestampUtc were ignored
        assertThat(foundEntity.createdTimestampUtc).isAfterOrEqualTo(testStartTime)
        assertThat(foundEntity.lastModifiedTimestampUtc).isAfterOrEqualTo(testStartTime)

        // WHEN we try to update it via the upsert method

        val entityForUpdate = SomeVersionedEntity(
            createdTimestampUtcToBeIgnored,
            entityId,
            lastModifiedTimestampUtcToBeIgnored,
            someEntity.someInt,
            someEntity.someString + "_updated",
            1000
        )

        this.dao.upsertBySomeInt(entityForUpdate)

        // THEN
        val foundUpdatedEntity = this.dao.findById(entityId)

        assertThat(foundUpdatedEntity.createdTimestampUtc).isEqualTo(foundEntity.createdTimestampUtc)
        assertThat(foundUpdatedEntity.lastModifiedTimestampUtc).isAfterOrEqualTo(testStartTime)
        assertThat(foundUpdatedEntity.version).isEqualTo(2)
        assertThat(foundUpdatedEntity.someInt).isEqualTo(someEntity.someInt)
        assertThat(foundUpdatedEntity.someString).isEqualTo(someEntity.someString + "_updated")

    }


}

package org.maiaframework.showcase.composite_pk

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.domain.ChangeType
import org.maiaframework.jdbc.BulkOptimisticLockingException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired


class CompositePrimaryKeyDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var compositePrimaryKeyDao: CompositePrimaryKeyDao


    @Autowired
    private lateinit var compositePrimaryKeyHistoryDao: CompositePrimaryKeyHistoryDao


    @Test
    fun testBulkSetFields_updatesRowsByCompositeKey_andInsertsHistory() {

        val entity1 = CompositePrimaryKeyEntityTestBuilder().build()
        val entity2 = CompositePrimaryKeyEntityTestBuilder().build()

        this.compositePrimaryKeyDao.insert(entity1)
        this.compositePrimaryKeyDao.insert(entity2)

        val updater1 = CompositePrimaryKeyEntityUpdater.forPrimaryKey(entity1.primaryKey, entity1.version) {
            someModifiableString("updated1")
        }
        val updater2 = CompositePrimaryKeyEntityUpdater.forPrimaryKey(entity2.primaryKey, entity2.version) {
            someModifiableString("updated2")
        }

        this.compositePrimaryKeyDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.compositePrimaryKeyDao.findByPrimaryKey(entity1.primaryKey)
        val updatedEntity2 = this.compositePrimaryKeyDao.findByPrimaryKey(entity2.primaryKey)

        assertThat(updatedEntity1.someModifiableString).isEqualTo("updated1")
        assertThat(updatedEntity1.version).isEqualTo(2)
        assertThat(updatedEntity2.someModifiableString).isEqualTo("updated2")
        assertThat(updatedEntity2.version).isEqualTo(2)

        val historyEntity1V2 = this.compositePrimaryKeyHistoryDao.findByPrimaryKey(
            CompositePrimaryKeyHistoryEntityPk(entity1.someInt, entity1.someString, 2)
        )
        assertThat(historyEntity1V2.someModifiableString).isEqualTo("updated1")
        assertThat(historyEntity1V2.changeType).isEqualTo(ChangeType.UPDATE)

        val historyEntity2V2 = this.compositePrimaryKeyHistoryDao.findByPrimaryKey(
            CompositePrimaryKeyHistoryEntityPk(entity2.someInt, entity2.someString, 2)
        )
        assertThat(historyEntity2V2.someModifiableString).isEqualTo("updated2")
        assertThat(historyEntity2V2.changeType).isEqualTo(ChangeType.UPDATE)

    }


    @Test
    fun testBulkSetFields_throwsAggregateException_whenCompositeKeyRowIsStale() {

        val staleEntity = CompositePrimaryKeyEntityTestBuilder().build()
        val validEntity = CompositePrimaryKeyEntityTestBuilder().build()

        this.compositePrimaryKeyDao.insert(staleEntity)
        this.compositePrimaryKeyDao.insert(validEntity)

        val staleUpdater = CompositePrimaryKeyEntityUpdater.forPrimaryKey(staleEntity.primaryKey, 999L) {
            someModifiableString("shouldNotApply")
        }
        val validUpdater = CompositePrimaryKeyEntityUpdater.forPrimaryKey(validEntity.primaryKey, validEntity.version) {
            someModifiableString("shouldApply")
        }

        assertThatThrownBy {
            this.compositePrimaryKeyDao.bulkSetFields(listOf(staleUpdater, validUpdater))
        }.isInstanceOf(BulkOptimisticLockingException::class.java)

        val updatedStale = this.compositePrimaryKeyDao.findByPrimaryKey(staleEntity.primaryKey)
        val updatedValid = this.compositePrimaryKeyDao.findByPrimaryKey(validEntity.primaryKey)

        assertThat(updatedStale.someModifiableString).isEqualTo(staleEntity.someModifiableString)
        assertThat(updatedStale.version).isEqualTo(1)
        assertThat(updatedValid.someModifiableString).isEqualTo("shouldApply")
        assertThat(updatedValid.version).isEqualTo(2)

    }


}

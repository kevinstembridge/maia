package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

class LeftCrudRightSimpleJoinTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var leftDao: LeftManyDao


    @Autowired
    private lateinit var rightDao: RightManyDao


    @Autowired
    private lateinit var leftToRightSimpleDao: LeftToRightSimpleDao


    @Autowired
    private lateinit var manyToManyJoinDao: LeftToRightManyToManyJoinDao


    @Autowired
    private lateinit var crudService: LeftManyCrudService


    private lateinit var leftEntity: LeftManyEntity


    private lateinit var rightEntity1: RightManyEntity


    private lateinit var rightEntity2: RightManyEntity


    private lateinit var rightEntity3: RightManyEntity


    @BeforeEach
    fun setUp() {

        leftEntity = LeftManyEntityTestBuilder().build()
        rightEntity1 = RightManyEntityTestBuilder(someString = "alpha").build()
        rightEntity2 = RightManyEntityTestBuilder(someString = "beta").build()
        rightEntity3 = RightManyEntityTestBuilder(someString = "gamma").build()

        truncateTable(LeftToRightSimpleEntityMeta.SCHEMA_AND_TABLE_NAME)
        manyToManyJoinDao.deleteAll()
        leftDao.deleteAll()
        rightDao.deleteAll()
        leftDao.insert(leftEntity)
        rightDao.bulkInsert(listOf(rightEntity1, rightEntity2, rightEntity3))

    }


    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `update preserves unchanged join and diffs added and removed associations`() {

        // Establish an initial association set: rightEntity1, rightEntity2
        crudService.update(
            LeftManyUpdateRequestDto(
                id_raw = leftEntity.id,
                rightEntities_raw = emptyList(),
                rightSimpleEntityIds_raw = listOf(rightEntity1.id, rightEntity2.id),
                rightSystemEffectiveEntities_raw = emptyList(),
                rightUserEffectiveEntities_raw = emptyList(),
                someInt_raw = leftEntity.someInt,
                someString_raw = leftEntity.someString,
                version_raw = leftEntity.version,
            )
        )

        val joinsAfterFirstUpdate = leftToRightSimpleDao.findByLeftSimple(leftEntity.id)
        assertThat(joinsAfterFirstUpdate).hasSize(2)

        val unchangedJoin = joinsAfterFirstUpdate.first { it.rightSimple == rightEntity1.id }
        val removedJoin = joinsAfterFirstUpdate.first { it.rightSimple == rightEntity2.id }

        // Second update: keep rightEntity1, drop rightEntity2, add rightEntity3
        crudService.update(
            LeftManyUpdateRequestDto(
                id_raw = leftEntity.id,
                rightEntities_raw = emptyList(),
                rightSimpleEntityIds_raw = listOf(rightEntity1.id, rightEntity3.id),
                rightSystemEffectiveEntities_raw = emptyList(),
                rightUserEffectiveEntities_raw = emptyList(),
                someInt_raw = leftEntity.someInt,
                someString_raw = leftEntity.someString,
                version_raw = leftEntity.version + 1
            )
        )

        val joinsAfterSecondUpdate = leftToRightSimpleDao.findByLeftSimple(leftEntity.id)
        assertThat(joinsAfterSecondUpdate.map { it.rightSimple }).containsExactlyInAnyOrder(rightEntity1.id, rightEntity3.id)

        // The join row for rightEntity1 must be the SAME row (not recreated)
        val preservedJoin = joinsAfterSecondUpdate.first { it.rightSimple == rightEntity1.id }
        assertThat(preservedJoin.id).isEqualTo(unchangedJoin.id)
        assertThat(preservedJoin.createdTimestampUtc).isEqualTo(unchangedJoin.createdTimestampUtc)

        // The join row for rightEntity2 must have been deleted
        assertThat(leftToRightSimpleDao.existsByPrimaryKey(removedJoin.id)).isFalse()

        // A new join row must exist for rightEntity3
        val newJoin = joinsAfterSecondUpdate.first { it.rightSimple == rightEntity3.id }
        assertThat(newJoin.leftSimple).isEqualTo(leftEntity.id)

    }


}

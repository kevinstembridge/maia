package org.maiaframework.showcase.many_to_many

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


class ManyToManyEffectiveRangeCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var rightManyDao: RightManyDao


    @Autowired
    private lateinit var esIndexOps: EsIndexOps


    @Autowired
    private lateinit var rightManyTypeaheadEsIndex: RightManyTypeaheadEsIndex


    @Autowired
    private lateinit var leftManyDao: LeftManyDao

    @Autowired
    private lateinit var leftToRightComplexDao: LeftToRightComplexDao


    private val rightGamma = RightManyEntityTestBuilder(someString = "right-gamma").build()


    // rightDelta is indexed so the typeahead returns multiple results, confirming the test
    // selects by name rather than by position.
    private val rightDelta = RightManyEntityTestBuilder(someString = "right-delta").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()
        rightManyDao.deleteAll()
        rightManyDao.bulkInsert(listOf(rightGamma, rightDelta))
        listOf(rightGamma, rightDelta).forEach { entity ->
            esIndexOps.upsert(EsDocHolder(
                id = entity.id.toString(),
                doc = RightManyTypeaheadV1EsDoc(id = entity.id, someString = entity.someString),
                indexName = rightManyTypeaheadEsIndex.indexName()
            ))
        }

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(leftManyBlotterPage)

        leftManyBlotterPage.clickAddButton()

        val beforeCreate = Instant.now()

        leftManyCreatePage.apply {
            assertOnPage()
            fillCreateForm()
            clickAddRightJoinEntityButton()
            searchAndSelectRightJoinEntityInMiniForm("right-gamma")
            clickConfirmAddRightJoinInMiniForm()
            clickSubmitButton()
        }

        leftManyViewPage.assertOnPage()

        val leftId = leftManyDao.findAllAsSequence().toList().single().id
        val joinAfterCreate = leftToRightComplexDao.findByLeft(leftId).single()
        assertThat(joinAfterCreate.effectiveFrom).isBetween(beforeCreate, Instant.now())
        assertThat(joinAfterCreate.effectiveTo).isNull()

        leftManyViewPage.clickEditButton()

        val beforeRemove = Instant.now()

        leftManyEditPage.apply {
            assertOnPage()
            assertRightJoinEntryVisible("right-gamma")
            removeRightJoinEntry("right-gamma")
            fillEditForm()
            clickSubmitButton()
        }

        leftManyViewPage.assertOnPage()

        val joinAfterRemove = leftToRightComplexDao.findByLeft(leftId).single()
        assertThat(joinAfterRemove.effectiveTo).isBetween(beforeRemove, Instant.now())

        `navigate to the`(leftManyBlotterPage)

        leftManyBlotterPage.apply {
            assertTableContainsValue("testleft_edited")
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertTableDoesNotContainValue("testleft_edited")
        }

    }


}

package org.maiaframework.showcase.many_to_many

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

        leftManyCreatePage.apply {
            assertOnPage()
            fillCreateForm()
            clickAddRightJoinEntityButton()
            searchAndSelectRightJoinEntityInMiniForm("right-gamma")
            clickConfirmAddRightJoinInMiniForm()
            clickSubmitButton()
        }

        leftManyViewPage.apply {
            assertOnPage()
            clickEditButton()
        }

        leftManyEditPage.apply {
            assertOnPage()
            assertRightJoinEntryVisible("right-gamma")
            removeRightJoinEntry("right-gamma")
            fillEditForm()
            clickSubmitButton()
        }

        leftManyViewPage.assertOnPage()

        `navigate to the`(leftManyBlotterPage)

        leftManyBlotterPage.apply {
            assertTableContainsValue("testleft_edited")
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertTableDoesNotContainValue("testleft_edited")
        }

    }


}

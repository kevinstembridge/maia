package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.auth.Authority
import org.maiaframework.showcase.testing.fixtures.UserFixture
import org.springframework.beans.factory.annotation.Autowired


class LeftManyCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var rightManyDao: RightManyDao


    @Autowired
    private lateinit var esIndexOps: EsIndexOps


    @Autowired
    private lateinit var rightManyTypeaheadEsIndex: RightManyTypeaheadEsIndex


    private val rightAlpha = RightManyEntityTestBuilder(someString = "right-alpha").build()
    private val rightBeta = RightManyEntityTestBuilder(someString = "right-beta").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()
        rightManyDao.deleteAll()
        rightManyDao.bulkInsert(listOf(rightAlpha, rightBeta))
        listOf(rightAlpha, rightBeta).forEach { entity ->
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

        // Create with two right entity chips selected
        leftManyBlotterPage.clickAddButton()

        leftManyCreatePage.apply {
            assertOnPage()
            fillCreateForm()
            clickAddRightEntityButton()
            searchAndSelectRightEntityInMiniForm("right-alpha")
            clickConfirmAddInMiniForm()
            clickAddRightEntityButton()
            searchAndSelectRightEntityInMiniForm("right-beta")
            clickConfirmAddInMiniForm()
            clickSubmitButton()
        }

        // Edit: verify both chips are pre-populated, remove both, then save.
        // Removing both chips clears all join records so the subsequent delete works
        // without triggering the "check foreign key references" dialog.
        leftManyViewPage.apply {
            assertOnPage()
            clickEditButton()
        }

        leftManyEditPage.apply {
            assertOnPage()
            assertJoinEntryVisible("right-alpha")
            assertJoinEntryVisible("right-beta")
            removeJoinEntry("right-alpha")
            removeJoinEntry("right-beta")
            fillEditForm()
            clickSubmitButton()
        }

        leftManyViewPage.assertOnPage()

        `navigate to the`(leftManyBlotterPage)

        leftManyBlotterPage.apply {
            assertTableContainsValue("testleft_edited")

            // Cancel path
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testleft_edited")

            // Confirm delete path
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testleft_edited")
        }

    }


}

package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
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

        leftManyBlotterPage.apply {
            // Create with two right entity chips selected
            clickAddButton()
            fillCreateForm()
            searchAndSelectRightEntity("right-alpha")
            searchAndSelectRightEntity("right-beta")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertTableContainsValue("testleft")

            // Edit: verify both chips are pre-populated, remove both, then save.
            // Removing both chips clears all join records so the subsequent delete works
            // without triggering the "check foreign key references" dialog.
            clickEditButtonForFirstRow()
            assertChipVisible("right-alpha")
            assertChipVisible("right-beta")
            removeChip("right-alpha")
            removeChip("right-beta")
            fillEditForm()
            clickSubmitButton()
            assertEditDialogClosed()
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

package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.elasticsearch.EsDocHolder
import org.maiaframework.elasticsearch.index.EsIndexOps
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


class LeftSearchableCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var rightDao: RightDao

    @Autowired
    private lateinit var esIndexOps: EsIndexOps

    @Autowired
    private lateinit var rightTypeaheadEsIndex: RightTypeaheadEsIndex

    private val rightAlpha = RightEntityTestBuilder(someString = "right-alpha").build()


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()
        fixtures.resetDatabaseState()
        rightDao.deleteAll()
        rightDao.insert(rightAlpha)
        esIndexOps.upsert(EsDocHolder(
            id = rightAlpha.id.toString(),
            doc = RightTypeaheadV1EsDoc(id = rightAlpha.id, someString = rightAlpha.someString),
            indexName = rightTypeaheadEsIndex.indexName()
        ))

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(leftSearchableBlotterPage)

        leftSearchableBlotterPage.apply {
            // Create with a right entity chip selected
            clickAddButton()
            fillCreateForm()
            searchAndSelectRightEntity("right-alpha")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertTableContainsValue("testleft")

            // Edit: verify the right entity chip is pre-populated, remove it, then save.
            // Removing the chip clears the join record so the subsequent delete works
            // without triggering the "check foreign key references" dialog.
            clickEditButtonForFirstRow()
            assertChipVisible("right-alpha")
            removeChip("right-alpha")
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

package org.maiaframework.showcase.all_field_types

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class AllFieldTypesCrudPlaywrightTest : AbstractPlaywrightTest() {


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()

    }


    @Test
    fun `user with edit permission can navigate to the all field types page`() {

        `log in as admin user`()
        `navigate to the`(allFieldTypesBlotterPage)

    }


}

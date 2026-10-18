package org.maiaframework.showcase.dialog

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest

class DialogFormPlaywrightTest : AbstractPlaywrightTest() {


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        fixtures.resetDatabaseState()

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `submitting a valid dialog form hands the values to the host and closes the dialog`() {

        `log in as admin user`()

        `navigate to the`(dialogFormPage)

        dialogFormPage.apply {
            openFeedbackDialog()
            fillFeedbackForm(comment = "Works well", rating = "4")
            clickSendButton()
            assertDialogIsClosed()
            assertFeedbackReceived("Received: Works well (4)")
        }

    }


    @Test
    fun `an invalid dialog form is not submitted`() {

        `log in as admin user`()

        `navigate to the`(dialogFormPage)

        dialogFormPage.apply {
            openFeedbackDialog()
            fillFeedbackForm(comment = "Too high", rating = "9")
            clickSendButton()
            assertDialogIsOpen()
            assertValidationErrorIsShown("Rating must not be more than 5.")
        }

    }


    @Test
    fun `cancelling closes the dialog without submitting`() {

        `log in as admin user`()

        `navigate to the`(dialogFormPage)

        dialogFormPage.apply {
            openFeedbackDialog()
            clickCancelButton()
            assertDialogIsClosed()
        }

    }


}

package org.maiaframework.mail

import org.maiaframework.domain.contact.EmailAddress
import org.assertj.core.api.Assertions.assertThat
import org.springframework.core.io.DefaultResourceLoader
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.Instant
import java.util.*

data class EmailAsserter(
        val recipientEmailAddresses: List<EmailAddress>,
        private val fromAddress: EmailAddress,
        private val ccAddress: List<EmailAddress>,
        private val bccAddress: List<EmailAddress>,
        private val subject: String?,
        private val contentType: String?,
        private val body: String?,
        val timestampUtc: Instant,
        private val templateEngine: TemplateEngine
) {


    fun expectSubject(expectedSubject: String): EmailAsserter {

        assertThat(this.subject).isEqualTo(expectedSubject)
        return this

    }


    fun expectToAddress(expectedToAddress: EmailAddress): EmailAsserter {

        return expectToAddress(expectedToAddress.value)

    }


    fun expectToAddress(expectedToAddress: String): EmailAsserter {

        assertThat(this.recipientEmailAddresses).isNotEmpty
        assertThat(this.recipientEmailAddresses.first().value).isEqualTo(expectedToAddress)
        return this

    }


    fun expectCcAddress(expectedCcAddress: String?): EmailAsserter {

        assertThat(this.ccAddress.first().value).isEqualTo(expectedCcAddress)
        return this

    }


    fun expectCcAddressIsBlank(): EmailAsserter {

        assertThat(this.ccAddress).isEmpty()
        return this

    }


    fun expectBccAddress(expectedBccAddress: String): EmailAsserter {

        assertThat(this.bccAddress.first().value).isEqualTo(expectedBccAddress)
        return this

    }


    fun expectBccAddressIsBlank(): EmailAsserter {

        assertThat(this.bccAddress).isEmpty()
        return this

    }


    fun expectFromAddress(expectedFromAddress: String): EmailAsserter {

        assertThat(this.fromAddress.value).isEqualTo(expectedFromAddress)
        return this

    }


    fun expectBodyContains(expectedBody: String): EmailAsserter {

        assertThat(this.body).contains(expectedBody)
        return this

    }


    fun expectBodyMatches(emailTemplateContext: EmailTemplateContext): EmailAsserter {

        val context: Map<String, *> = emailTemplateContext.context
        val expectedContent =  getMessageBody(emailTemplateContext.location.value, context)

        assertThat(this.body?.trim()).isEqualTo(expectedContent)
        return this

    }


    private fun getMessageBody(templateLocation: String, context: Map<String, *>): String {

        val thymeleafContext = Context(Locale.getDefault(), context)
        return templateEngine.process(templateLocation, thymeleafContext)

    }


    fun expectContentType(expectedContentType: String): EmailAsserter {

        assertThat(this.contentType).isEqualTo(expectedContentType)
        return this

    }

    companion object {

        val resourceLoader = DefaultResourceLoader()

    }


}

package org.maiaframework.mail

import org.maiaframework.common.logging.getLogger
import org.maiaframework.json.JsonFacade
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGridAPI
import org.maiaframework.domain.contact.EmailAddress
//import org.springframework.classify.BinaryExceptionClassifier
import org.springframework.context.annotation.Profile
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.util.backoff.ExponentialBackOff
import org.thymeleaf.TemplateEngine
import java.time.Instant

@Component
@Profile("emailStub")
class SendGridApiStub(private val jsonFacade: JsonFacade, private val templateEngine: TemplateEngine) : SendGridAPI {


    private val logger = getLogger<SendGridApiStub>()


    private val emails = mutableListOf<EmailAsserter>()


    private val retryTemplate = RetryTemplate(RetryPolicy.builder()
        .backOff(ExponentialBackOff())
        .includes(AssertionError::class.java, Exception::class.java)
        .build())


    override fun initialize(auth: String?, host: String?) {
        TODO("Not yet implemented")
    }

    override fun getLibraryVersion(): String {
        TODO("Not yet implemented")
    }

    override fun getVersion(): String {
        TODO("Not yet implemented")
    }

    override fun setVersion(version: String?) {
        TODO("Not yet implemented")
    }

    override fun getRequestHeaders(): MutableMap<String, String> {
        TODO("Not yet implemented")
    }

    override fun addRequestHeader(key: String?, value: String?): MutableMap<String, String> {
        TODO("Not yet implemented")
    }

    override fun removeRequestHeader(key: String?): MutableMap<String, String> {
        TODO("Not yet implemented")
    }

    override fun getHost(): String {
        TODO("Not yet implemented")
    }

    override fun setHost(host: String?) {
        TODO("Not yet implemented")
    }

    override fun makeCall(request: Request?): Response {
        TODO("Not yet implemented")
    }

    override fun api(request: Request): Response {

        val requestBody = this.jsonFacade.readValue(request.body, SendGridMail::class.java)
        val subject = requestBody.subject

        val personalizations: Personalization = requestBody.personalizations.first()
        val recipientEmailAddresses = personalizations.to?.map { EmailAddress(it.email) } ?: emptyList()
        val ccEmailAddresses = personalizations.cc?.map { EmailAddress(it.email) } ?: emptyList()
        val bccEmailAddresses = personalizations.bcc?.map { EmailAddress(it.email) } ?: emptyList()

        val fromAddress = EmailAddress(requestBody.from.email)
        val contentType = requestBody.content.firstOrNull()?.type
        val messageBody = requestBody.content.firstOrNull()?.value

        val emailAsserter = EmailAsserter(
                recipientEmailAddresses,
                fromAddress,
                ccEmailAddresses,
                bccEmailAddresses,
                subject,
                contentType,
                messageBody,
                Instant.now(),
                this.templateEngine
        )

        this.logger.info("Captured new email: $emailAsserter")
        this.emails.add(emailAsserter)

        return Response(200, "{}", emptyMap())

    }


    fun getLatestEmail(): EmailAsserter {

        return this.retryTemplate.execute {
            getLatestEmailWithoutRetry()
        }

    }


    fun getLatestEmailWithoutRetry(): EmailAsserter {

        return this.emails.lastOrNull()
                ?: throw AssertionError("No emails have been sent yet.")

    }


    fun getEmailsSince(fromInstant: Instant): List<EmailAsserter> {

        return this.emails.filter { it.timestampUtc.isAfter(fromInstant) }
                .sortedBy { it.timestampUtc }

    }


}

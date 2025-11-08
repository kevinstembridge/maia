package org.maiaframework.sendgrid

import org.maiaframework.common.BlankStringException
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGridAPI
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import org.maiaframework.domain.contact.EmailAddress
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UncheckedIOException


class SendGridEmailService(private val sendGrid: SendGridAPI) {


    fun send(
            recipientEmailAddress: EmailAddress,
            fromAddress: EmailAddress,
            ccAddress: EmailAddress?,
            bccAddress: EmailAddress?,
            subject: String,
            contentType: String,
            messageBody: String
    ) {

        BlankStringException.throwIfBlank(subject, "subject")
        BlankStringException.throwIfBlank(messageBody, "body")

        LOGGER.info("Sending email with subject '{}' to {}", subject, recipientEmailAddress)

        val from = Email(fromAddress.value)
        val to = Email(recipientEmailAddress.value)
        val content = Content(contentType, messageBody)
        val mail = Mail(from, subject, to, content)
        addCcToMail(ccAddress, mail)
        addBccToMail(bccAddress, mail)

        try {

            val request = Request().apply {
                method = Method.POST
                endpoint = "mail/send"
                body = mail.build()
            }

            this.sendGrid.api(request)

        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }


    private fun addBccToMail(bcc: EmailAddress?, mail: Mail) {

        bcc?.let { mail.getPersonalization()[0].addBcc(Email(it.value)) }

    }


    private fun addCcToMail(cc: EmailAddress?, mail: Mail) {

        cc?.let { mail.getPersonalization()[0].addCc(Email(it.value)) }

    }


    companion object {

        private val LOGGER = LoggerFactory.getLogger(SendGridEmailService::class.java)

    }


}

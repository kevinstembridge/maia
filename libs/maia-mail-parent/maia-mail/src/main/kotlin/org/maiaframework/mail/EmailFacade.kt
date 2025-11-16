package org.maiaframework.mail

import org.maiaframework.common.logging.getLogger
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.sendgrid.SendGridEmailService
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale


class EmailFacade(
    private val emailService: SendGridEmailService,
    private val thymeleafTemplateEngine: TemplateEngine
) {


    private val logger = getLogger<EmailFacade>()


    fun send(
        recipientEmailAddress: EmailAddress,
        fromAddress: EmailAddress,
        ccAddress: EmailAddress? = null,
        bccAddress: EmailAddress? = null,
        subject: String,
        templateContext: EmailTemplateContext
    ) {

        val messageBody = getMessageBody(templateContext)

        this.emailService.send(
            recipientEmailAddress,
            fromAddress,
            ccAddress,
            bccAddress,
            subject,
            "text/html",
            messageBody
        )

    }


    private fun getMessageBody(emailTemplateContext: EmailTemplateContext): String {

        try {
            val thymeleafContext = Context(Locale.getDefault(), emailTemplateContext.context)
            return thymeleafTemplateEngine.process(emailTemplateContext.location.value, thymeleafContext)
        } catch (e: Exception) {
            logger.error("Error processing email body template", e)
            throw e
        }

    }


}

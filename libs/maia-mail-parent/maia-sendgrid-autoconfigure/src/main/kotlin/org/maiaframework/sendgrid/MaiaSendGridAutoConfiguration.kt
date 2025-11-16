package org.maiaframework.sendgrid

import com.sendgrid.SendGridAPI
import org.maiaframework.mail.EmailFacade
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.TemplateEngine

@Configuration
@ConditionalOnClass(SendGridEmailService::class)
class MaiaSendGridAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun emailService(sendGridApi: SendGridAPI): SendGridEmailService {

        return SendGridEmailService(sendGridApi)

    }


    @Bean
    @ConditionalOnMissingBean
    fun emailFacade(emailService: SendGridEmailService, templateEngine: TemplateEngine): EmailFacade {

        return EmailFacade(emailService, templateEngine)

    }


}

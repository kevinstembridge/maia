package org.maiaframework.sendgrid

import com.sendgrid.SendGridAPI
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(SendGridEmailService::class)
class MaiaSendGridAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun emailService(sendGridApi: SendGridAPI): SendGridEmailService {

        return SendGridEmailService(sendGridApi)

    }


}

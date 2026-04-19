package org.maiaframework.job

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(MaiaJobEndpoint::class)
class MaiaJobWebAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun maiaJobEndpoint(jobService: MaiaJobService): MaiaJobEndpoint {

        return MaiaJobEndpoint(jobService)

    }


}

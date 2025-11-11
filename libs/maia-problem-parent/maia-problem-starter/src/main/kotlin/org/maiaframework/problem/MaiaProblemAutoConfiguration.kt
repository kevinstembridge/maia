package org.maiaframework.problem

import org.maiaframework.props.Props
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@AutoConfiguration
@ConditionalOnClass(MaiaProblems::class)
class MaiaProblemAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun maiaProblems(props: Props): MaiaProblems {

        return MaiaProblems { props.getString("org.maiaframework.problems.type_prefix") }

    }


}

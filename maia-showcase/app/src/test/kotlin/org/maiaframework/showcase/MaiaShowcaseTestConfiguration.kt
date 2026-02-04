package org.maiaframework.showcase

import com.hazelcast.config.Config
import com.hazelcast.config.YamlConfigBuilder
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.problem.MaiaProblems
import org.maiaframework.props.Props
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.mock.env.MockEnvironment

//@Configuration
class MaiaShowcaseTestConfiguration {


    @Bean
    fun jdbcOps(namedParameterJdbcOperations: NamedParameterJdbcOperations): JdbcOps {

        return JdbcOps(namedParameterJdbcOperations)

    }


    @Bean
    fun createNewConfig(
    ): Config {

        val config: Config = YamlConfigBuilder().build()

        config.apply {
//            addMapConfig(loginEmailAddressByUserIdCacheConfig)
//            addMapConfig(userDetailsByLoginEmailAddressCacheConfig)
        }

        return config

    }


    @Bean
    fun props(): Props {

        return Props(InMemoryPropsRepo(), MockEnvironment())

    }


    @Bean
    fun maiaProblems(): MaiaProblems {
        return MaiaProblems { "some_prefix" }
    }


}

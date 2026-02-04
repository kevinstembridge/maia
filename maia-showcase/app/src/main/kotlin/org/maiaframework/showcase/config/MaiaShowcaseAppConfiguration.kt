package org.maiaframework.showcase.config

import org.maiaframework.jdbc.JdbcOps
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations


@Configuration
@ComponentScan(basePackages = ["org.maiaframework.json"])
class MaiaShowcaseAppConfiguration {


    @Bean
    fun jdbcOps(jdbcOps: NamedParameterJdbcOperations): JdbcOps {

        return JdbcOps(jdbcOps)

    }


}

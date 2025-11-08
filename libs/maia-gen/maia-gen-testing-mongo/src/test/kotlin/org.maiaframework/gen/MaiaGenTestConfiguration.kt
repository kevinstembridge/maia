package mahana.gen


import org.maiaframework.dao.mongo.MongoClientFacade
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

import java.net.UnknownHostException


@Configuration
@ComponentScan(basePackages = ["org.maiaframework.gen", "org.maiaframework.json"])
class MaiaGenTestConfiguration {


    @Value("\${mahana.gen.mongoClientUri}")
    private lateinit var mongoClientUri: String


    @Value("\${mahana.gen.defaultDatabaseName}")
    private lateinit var defaultDatabaseName: String


    @Bean
    @Throws(UnknownHostException::class)
    fun mongoClient(): MongoClient {

        return MongoClients.create(this.mongoClientUri)

    }


    @Bean
    @Throws(UnknownHostException::class)
    fun mongoClientFacade(): MongoClientFacade {

        return MongoClientFacade(mongoClient(), this.defaultDatabaseName)

    }


    companion object {

        @Bean
        fun propertyPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {

            return PropertySourcesPlaceholderConfigurer()

        }

    }


}

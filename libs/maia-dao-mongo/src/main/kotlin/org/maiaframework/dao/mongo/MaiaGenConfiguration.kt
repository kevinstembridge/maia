package org.maiaframework.dao.mongo

import org.maiaframework.dao.mongo.gridfs.GridFsFacade
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.UnknownHostException


@Configuration
class MaiaGenConfiguration {


    @Autowired
    lateinit var maiaGenProperties: MaiaGenProperties


    @Bean
    @Throws(UnknownHostException::class)
    fun mongoClient(): MongoClient {

        val mongoClientURI = this.maiaGenProperties.mongoClientUri
        return MongoClients.create(mongoClientURI)

    }


    @Bean
    @Throws(UnknownHostException::class)
    fun mongoClientFacade(): MongoClientFacade {

        return MongoClientFacade(mongoClient(), this.maiaGenProperties.defaultDatabaseName)

    }


    @Bean
    @Throws(UnknownHostException::class)
    fun gridFsFacade(): GridFsFacade {

        return GridFsFacade(mongoClient(), this.maiaGenProperties.defaultDatabaseName)

    }


}

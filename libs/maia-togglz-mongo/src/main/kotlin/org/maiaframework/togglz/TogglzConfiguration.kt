package org.maiaframework.togglz

import org.maiaframework.dao.mongo.MaiaGenProperties
import com.mongodb.client.MongoClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.togglz.core.context.StaticFeatureManagerProvider
import org.togglz.core.manager.FeatureManager
import org.togglz.core.manager.FeatureManagerBuilder
import org.togglz.core.repository.StateRepository
import org.togglz.core.repository.cache.CachingStateRepository
import org.togglz.core.spi.FeatureProvider
import org.togglz.core.user.UserProvider


@Configuration
class TogglzConfiguration {


    @Autowired
    private lateinit var mahanagenProperties: MaiaGenProperties


    @Autowired
    private lateinit var mongoClient: MongoClient


    @Bean
    fun mongoStateRepository(): StateRepository {

        val defaultDatabaseName = this.mahanagenProperties.defaultDatabaseName

        val mongoStateRepository = MaiaTogglzStateRepository(
                this.mongoClient,
                defaultDatabaseName,
                "togglz")

        return CachingStateRepository(mongoStateRepository)

    }


    @Bean
    @Primary
    fun myFeatureManager(
            stateRepository: StateRepository,
            userProvider: UserProvider,
            featureProvider: FeatureProvider
    ): FeatureManager {

        val featureManager = FeatureManagerBuilder()
                .featureProvider(featureProvider)
                .stateRepository(stateRepository)
                .userProvider(userProvider)
                .build()

        StaticFeatureManagerProvider.setFeatureManager(featureManager)
//        FeatureManagerProvider.featureMgr = featureManager
        return featureManager

    }

}

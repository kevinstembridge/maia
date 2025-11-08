package org.maiaframework.dao.mongo.migration


import org.maiaframework.dao.mongo.MongoClientFacade
import org.maiaframework.dao.mongo.MongoCollectionFacade
import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*


@Component
class MaiaGenMongoMigrationEntryDao @Autowired
constructor(mongoClientFacade: MongoClientFacade) {

    private val mongoCollectionFacade: MongoCollectionFacade


    init {

        this.mongoCollectionFacade = MongoCollectionFacade(COLLECTION_NAME, mongoClientFacade)

    }


    fun notExistsById(migrationId: MaiaGenMigrationId): Boolean {

        val query = Document("migrationId", migrationId.value)
        return this.mongoCollectionFacade.count(query) == 0L

    }


    fun insert(migrationId: MaiaGenMigrationId, migration: MaiaGenMongoMigration) {

        val document = Document()

        document["migrationId"] = migrationId.value
        document["description"] = migration.changeDescription
        document["ts"] = Date()

        this.mongoCollectionFacade.insert(document)

    }

    companion object {

        private val COLLECTION_NAME = CollectionName("maiaGenMigrationEntry")
    }


}

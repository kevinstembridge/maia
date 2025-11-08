package org.maiaframework.dao.mongo


import org.maiaframework.common.logging.getLogger
import org.maiaframework.domain.types.CollectionName
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.TransactionBody
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.result.DeleteResult
import org.bson.Document
import org.bson.conversions.Bson


class MongoClientFacade(private val mongoClient: MongoClient, defaultDatabaseName: String) {

    private val logger = getLogger<MongoClientFacade>()


    val defaultDatabase: MongoDatabase


    init {

        require(defaultDatabaseName.isNotBlank()) { "defaultDatabaseName must not be blank" }

        this.defaultDatabase = mongoClient.getDatabase(defaultDatabaseName)
        logger.info("Established connection to database '$defaultDatabaseName'")

    }


    fun <T> withTxn(txnBody: TransactionBody<T>): T {

        val clientSession = this.mongoClient.startSession()

        clientSession.use { s ->
            return s.withTransaction(txnBody)
        }

    }


    fun count(collectionName: CollectionName): Long {

        return getCollection(collectionName).countDocuments()

    }


    fun count(collectionName: CollectionName, query: Bson): Long {

        return getCollection(collectionName).countDocuments(query)

    }


    fun exists(collectionName: CollectionName, query: Bson): Boolean {

        return getCollection(collectionName).countDocuments(query) > 0

    }


    fun findOne(collectionName: CollectionName, filter: Bson): Document {

        return getCollection(collectionName).find(filter).first()!!

    }


    fun getCollection(collectionName: CollectionName): MongoCollection<Document> {

        return this.defaultDatabase.getCollection(collectionName.value)

    }


    fun insert(collectionName: CollectionName, document: Document) {

        getCollection(collectionName).insertOne(document)

    }


    fun insertMany(collectionName: CollectionName, documents: List<Document>) {

        getCollection(collectionName).insertMany(documents)

    }


    fun insertMany(collectionName: CollectionName, documents: List<Document>, options: InsertManyOptions) {

        getCollection(collectionName).insertMany(documents, options)

    }


    fun deleteMany(collectionName: CollectionName, query: Bson): DeleteResult {

        return getCollection(collectionName).deleteMany(query)

    }


    fun ensureCollectionIsCapped(collectionName: CollectionName, sizeInBytes: Long) {

        if (collectionExists(collectionName)) {

            if (collectionIsNotCapped(collectionName)) {

                logger.info("Converting collection '$collectionName' to capped")

                val convertToCappedCommandDoc = Document()
                    .append("convertToCapped", collectionName.value)
                    .append("size", sizeInBytes)

                val result: Document = this.defaultDatabase.runCommand(convertToCappedCommandDoc)
                logger.info("result = $result")

            }

        } else {

            createCappedCollection(collectionName, sizeInBytes)

        }

    }


    private fun collectionExists(collectionName: CollectionName): Boolean {

        return this.defaultDatabase.listCollectionNames().find { it == collectionName.value } != null

    }


    private fun collectionIsNotCapped(collectionName: CollectionName): Boolean {

        return isCollectionCapped(collectionName) == false

    }


    private fun isCollectionCapped(collectionName: CollectionName): Boolean {

        val collStatsCommandDoc = Document("collStats", collectionName.value)
        val resultDoc = this.defaultDatabase.runCommand(collStatsCommandDoc)
        return resultDoc.getBoolean("capped") ?: false

    }


    private fun createCappedCollection(
        collectionName: CollectionName,
        sizeInBytes: Long
    ) {

        val createCollectionOptions = CreateCollectionOptions()
        createCollectionOptions.capped(true)
        createCollectionOptions.sizeInBytes(sizeInBytes)

        logger.info("Creating new capped collection '$collectionName'")

        this.defaultDatabase.createCollection(collectionName.value, createCollectionOptions)

    }


}

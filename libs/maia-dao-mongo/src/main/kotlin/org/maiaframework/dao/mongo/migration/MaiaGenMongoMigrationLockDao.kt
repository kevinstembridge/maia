package org.maiaframework.dao.mongo.migration

import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import org.maiaframework.dao.mongo.MongoClientFacade
import org.maiaframework.dao.mongo.MongoCollectionFacade
import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Date

@Component
class MaiaGenMongoMigrationLockDao(mongoClientFacade: MongoClientFacade) {


    private val mongoCollectionFacade: MongoCollectionFacade = MongoCollectionFacade(COLLECTION_NAME, mongoClientFacade)


    private val hostName: String
        get() {

            try {
                return InetAddress.getLocalHost().hostName
            } catch (e: UnknownHostException) {
                return "Unknown"
            }

        }


    private val processName: String
        get() = ManagementFactory.getRuntimeMXBean().name


    init {

        createIndex()

    }


    private fun createIndex() {

        val sortAscending = 1
        val indexKeys = Document(UNIQUE_FIELD_NAME, sortAscending)
        val indexOptions = IndexOptions().unique(true).name("maiaGenMigrationLock_idx")

        this.mongoCollectionFacade.createIndex(indexKeys, indexOptions)

    }


    @Throws(MaiaGenMongoMigrationLockNotAvailableException::class)
    fun obtainLock() {

        val lockDocument = createLockDocument()

        try {
            this.mongoCollectionFacade.insert(lockDocument)
        } catch (e: MongoWriteException) {

            val error = e.error
            val errorCode = error.code

            if (errorCode == 11000) {
                throwLockNotAvailableException()
            } else {
                throw e
            }

        }

    }


    private fun createLockDocument(): Document {

        val insertDocument = Document()
        insertDocument[UNIQUE_FIELD_NAME] = UNIQUE_FIELD_VALUE
        insertDocument[CREATED_TIMESTAMP_FIELD_NAME] = Date()
        insertDocument[HOSTNAME_FIELD_NAME] = hostName
        insertDocument[PROCESS_NAME_FIELD_NAME] = processName
        return insertDocument

    }


    @Throws(MaiaGenMongoMigrationLockNotAvailableException::class)
    private fun throwLockNotAvailableException() {

        val existingDocument = this.mongoCollectionFacade.findOne(Document(UNIQUE_FIELD_NAME, UNIQUE_FIELD_VALUE))
        val lockCreatedTimestampUtc = existingDocument.getDate(CREATED_TIMESTAMP_FIELD_NAME).toInstant()
        val hostname = existingDocument.getString(HOSTNAME_FIELD_NAME)
        val processName = existingDocument.getString(PROCESS_NAME_FIELD_NAME)
        throw MaiaGenMongoMigrationLockNotAvailableException(lockCreatedTimestampUtc, hostname, processName)

    }


    fun releaseLock() {

        this.mongoCollectionFacade.deleteOne(Document(UNIQUE_FIELD_NAME, true))

    }

    companion object {


        private val COLLECTION_NAME = CollectionName("maiaGenMigrationLock")
        private const val UNIQUE_FIELD_NAME = "locked"
        private const val UNIQUE_FIELD_VALUE = true
        private const val CREATED_TIMESTAMP_FIELD_NAME = "c-ts"
        private const val HOSTNAME_FIELD_NAME = "hostname"
        private const val PROCESS_NAME_FIELD_NAME = "processName"

    }


}

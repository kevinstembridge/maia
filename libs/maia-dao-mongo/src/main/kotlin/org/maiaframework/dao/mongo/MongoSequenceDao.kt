package org.maiaframework.dao.mongo

import org.maiaframework.domain.types.CollectionName
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import org.bson.Document

class MongoSequenceDao(collectionName: CollectionName, sequenceName: String, mongoClientFacade: MongoClientFacade) {

    private val collectionFacade: MongoCollectionFacade = MongoCollectionFacade(collectionName, mongoClientFacade)
    private val findBySequenceName: Document = Document("_id", sequenceName)
    private val incrementSequenceValue = Document("\$inc", Document(SEQUENCE_VALUE_FIELD_NAME, 1))
    private val options: FindOneAndUpdateOptions = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)


    val nextId: Long
        get() = collectionFacade.findOneAndUpdate(
                findBySequenceName,
                incrementSequenceValue,
                options,
                { document -> document.getLong(SEQUENCE_VALUE_FIELD_NAME) })


    constructor(sequenceName: String, mongoClientFacade: MongoClientFacade) : this(DEFAULT_COLLECTION_NAME, sequenceName, mongoClientFacade)


    init {

        primeFirstValueIfNotPresent(sequenceName)

    }


    private fun primeFirstValueIfNotPresent(sequenceName: String) {

        val filterDoc = Document("_id", sequenceName)

        val updateDoc = Document("\$setOnInsert", Document(SEQUENCE_VALUE_FIELD_NAME, 0L))

        val findOneAndUpdateOptions = FindOneAndUpdateOptions()
        findOneAndUpdateOptions.upsert(true)

        this.collectionFacade.findOneAndUpdateOrNull(filterDoc, updateDoc, findOneAndUpdateOptions)

    }


    companion object {

        private val DEFAULT_COLLECTION_NAME = CollectionName("maiaSequence")
        private const val SEQUENCE_VALUE_FIELD_NAME = "sequenceValue"

    }


}

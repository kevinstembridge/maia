package org.maiaframework.dao.mongo


import org.maiaframework.domain.mongo.DocumentNotFoundException
import org.maiaframework.dao.mongo.search.MongoAggregationSearchRequest
import org.maiaframework.domain.search.mongo.MongoPageableSearchRequest
import org.maiaframework.domain.search.mongo.MongoSearchRequest
import org.maiaframework.domain.search.SearchResultPage
import org.maiaframework.domain.types.CollectionName
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.DistinctIterable
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.*
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*
import java.util.function.Function


class MongoCollectionFacade(
    val collectionName: CollectionName,
    private val mongoClientFacade: MongoClientFacade,
    private val droppable: Boolean = false
) {


    private val logger = LoggerFactory.getLogger(MongoCollectionFacade::class.java)


    val collection: MongoCollection<Document>
        get() = this.mongoClientFacade.getCollection(this.collectionName)


    fun insert(document: Document) {

        this.mongoClientFacade.insert(this.collectionName, document)

    }


    fun insertMany(documents: List<Document>) {

        this.mongoClientFacade.insertMany(this.collectionName, documents)

    }


    fun insertMany(documents: List<Document>, options: InsertManyOptions) {

        this.mongoClientFacade.insertMany(this.collectionName, documents, options)

    }


    fun <T> findByIdOrNull(id: ObjectId, documentMapper: (Document) -> T): T? {

        val query = Document("_id", id)
        return findOneOrNull(query, documentMapper)

    }


    fun <T> findById(id: ObjectId, documentMapper: (Document) -> T): T {

        val query = Document("_id", id)
        return findOneOrNull(query, documentMapper) ?: throw DocumentNotFoundException(this.collectionName, query)

    }


    fun findOne(query: Bson): Document {

        return findOneOrNull(query, { document -> document }) ?: throw DocumentNotFoundException(this.collectionName, query)

    }


    fun <T> findOne(query: Bson, documentMapper: (Document) -> T): T {

        return findOneOrNull(query, documentMapper) ?: throw DocumentNotFoundException(this.collectionName, query)

    }


    fun <T> findOne(query: Bson, orderBy: Bson, documentMapper: (Document) -> T): T {

        return findOneOrNull(query, orderBy, documentMapper) ?: throw DocumentNotFoundException(this.collectionName, query)

    }


    fun <T> findOne(query: Bson, orderBy: Sort?, propertyNameToCollectionFieldNameMapper: (String) -> String, documentMapper: (Document) -> T): T {

        return findOneOrNull(query, null, orderBy, propertyNameToCollectionFieldNameMapper, documentMapper) ?: throw DocumentNotFoundException(this.collectionName, query)

    }


    fun findOneOrNull(query: Bson): Document? {

        return findOneOrNull(query, null, null, { document -> document })

    }


    fun <T> findOneOrNull(query: Bson, documentMapper: (Document) -> T?): T? {

        return findOneOrNull(query, null, null, documentMapper)

    }


    fun <T> findOneOrNull(query: Bson, orderBy: Bson, documentMapper: (Document) -> T): T? {

        return findOneOrNull(query, null, orderBy, documentMapper)

    }


    fun <T> findOneOrNull(
            query: Bson,
            projection: Bson?,
            orderBy: Bson?,
            documentMapper: (Document) -> T?
    ): T? {

        val foundDbObject: Document? = collection
                .find(query)
                .projection(projection)
                .sort(orderBy)
                .firstOrNull()

        return foundDbObject?.let { documentMapper.invoke(it) }

    }


    fun <T> findOneOrNull(
            query: Bson,
            projection: Bson?,
            orderBy: Sort?,
            propertyNameToCollectionFieldNameMapper: (String) -> String,
            documentMapper: (Document) -> T?
    ): T? {

        val orderByBson = orderBy?.let { createSortFrom(it, propertyNameToCollectionFieldNameMapper) }

        val foundDbObject: Document? = collection
                .find(query)
                .projection(projection)
                .sort(orderByBson)
                .firstOrNull()

        return foundDbObject?.let { documentMapper.invoke(it) }

    }


    fun findOneAndUpdate(filter: Bson, update: Bson): Document {

        return findOneAndUpdateOrNull(filter, update)
                .orElseThrow { DocumentNotFoundException(this.collectionName, filter) }

    }


    fun findOneAndUpdate(
            filter: Bson,
            update: Bson,
            options: FindOneAndUpdateOptions
    ): Document {

        return findOneAndUpdateOrNull(filter, update, options)
                .orElseThrow { DocumentNotFoundException(this.collectionName, filter) }

    }


    fun <T> findOneAndUpdate(
            filter: Bson,
            update: Bson,
            documentMapper: Function<Document, T>
    ): T {

        return findOneAndUpdateOrNull(filter, update, documentMapper)
                .orElseThrow { DocumentNotFoundException(this.collectionName, filter) }

    }


    fun <T> findOneAndUpdate(
            filter: Bson,
            update: Bson,
            findOneAndUpdateOptions: FindOneAndUpdateOptions,
            documentMapper: (Document) -> T
    ): T {

        return findOneAndUpdateOrNull(filter, update, findOneAndUpdateOptions, documentMapper)
                ?: throw DocumentNotFoundException(this.collectionName, filter)

    }


    fun findOneAndUpdateOrNull(
            filter: Bson,
            update: Bson
    ): Optional<Document> {

        val returnDocument = collection.findOneAndUpdate(filter, update)
        return Optional.ofNullable(returnDocument)

    }


    fun findOneAndUpdateOrNull(
            filter: Bson,
            update: Bson,
            options: FindOneAndUpdateOptions
    ): Optional<Document> {

        val returnDocument = collection.findOneAndUpdate(filter, update, options)
        return Optional.ofNullable(returnDocument)

    }


    fun <T> findOneAndUpdateOrNull(
            filter: Bson,
            update: Bson,
            documentMapper: Function<Document, T>
    ): Optional<T> {

        val returnDocument = collection.findOneAndUpdate(filter, update)
        return Optional.ofNullable(returnDocument).map(documentMapper)

    }


    fun <T> findOneAndUpdateOrNull(
            filter: Bson,
            update: Bson,
            findOneAndUpdateOptions: FindOneAndUpdateOptions,
            documentMapper: (Document) -> T?
    ): T? {

        val returnDocument = collection.findOneAndUpdate(filter, update, findOneAndUpdateOptions)
        return returnDocument?.let { documentMapper.invoke(it) }

    }


    fun <T> updateMany(
            filter: Bson,
            update: Bson,
            updateOptions: UpdateOptions
    ): UpdateResult {

        return collection.updateMany(filter, update, updateOptions)

    }


    fun <T : Any> findPage(
        mongoPageableSearchRequest: MongoPageableSearchRequest,
        documentMapper: (Document) -> T,
        propertyNameToCollectionFieldNameMapper: (String) -> String
    ): Page<T> {

        val query = mongoPageableSearchRequest.query
        val pageable = mongoPageableSearchRequest.pageable

        if (pageable != null) {
            return findPage(query, pageable, documentMapper, propertyNameToCollectionFieldNameMapper)
        }

        val list = find(query, documentMapper)
        return PageImpl(list)

    }


    fun <T> find(query: Bson, documentMapper: (Document) -> T): List<T> {

        try {

            val iterable: MongoCursor<Document> = collection.find(query).iterator()
            return iterable.asSequence().toList().map { documentMapper.invoke(it) }

        } catch (e: Exception) {

            if (logger.isErrorEnabled) {

                val queryString = bsonToString(query)

                logger.error("Error executing find operation. $e. db.${this.collectionName}.find($queryString)")

            }

            throw e

        }

    }


    private fun bsonToString(bson: Bson): String {

        return when (bson) {
            is Document -> bson.toJson()
            else -> bson.toString()
        }

    }


    fun <T : Any> findPage(
            query: Bson,
            pageable: Pageable,
            documentMapper: (Document) -> T,
            propertyNameToCollectionFieldNameMapper: (String) -> String
    ): Page<T> {

        val offset: Int = pageable.offset.toInt()
        val pageSize = pageable.pageSize
        val sort = createSortFrom(pageable.sort, propertyNameToCollectionFieldNameMapper)
        val documentList = find(query, null, offset, pageSize, sort, documentMapper)
        val total = count(query)
        return PageImpl(documentList, pageable, total)

    }


    fun <T : Any> findPage(
            query: Bson,
            sort: Bson,
            pageable: Pageable,
            documentMapper: (Document) -> T
    ): Page<T> {

        val offset: Int = pageable.offset.toInt()
        val pageSize = pageable.pageSize
        val documentList = find(query, null, offset, pageSize, sort, documentMapper)
        val total = count(query)
        return PageImpl(documentList, pageable, total)

    }


    fun <T : Any> find(
        mongoSearchRequest: MongoSearchRequest,
        documentMapper: (Document) -> T
    ): List<T> {

        return find(
                mongoSearchRequest.query,
                mongoSearchRequest.projection,
                mongoSearchRequest.offset,
                mongoSearchRequest.limit,
                mongoSearchRequest.sort,
                documentMapper)

    }


    fun <T> search(
        mongoSearchRequest: MongoSearchRequest,
        documentMapper: (Document) -> T
    ): SearchResultPage<T> {

        val data = find(
                mongoSearchRequest.query,
                mongoSearchRequest.projection,
                mongoSearchRequest.offset,
                mongoSearchRequest.limit,
                mongoSearchRequest.sort,
                documentMapper)
        val count = count(mongoSearchRequest.query)
        return SearchResultPage(data, count, mongoSearchRequest.offset, mongoSearchRequest.limit)

    }


    fun <T> search(
        mongoSearchRequest: MongoAggregationSearchRequest,
        documentMapper: (Document) -> T
    ): SearchResultPage<T> {

        val results = findUsingAggregate(
                mongoSearchRequest.searchAggregations,
                documentMapper
        ).toList()

        val totalCountAggregations = mongoSearchRequest.totalCountAggregations
                .plus(Document("\$count", "totalCount"))

        val totalCountDocumentMapper = { doc: Document -> doc.getInteger("totalCount") }

        val count = findUsingAggregate(
                totalCountAggregations,
                totalCountDocumentMapper
        ).firstOrNull()
                ?.toLong()
                ?: 0

        return SearchResultPage(
                results,
                count,
                mongoSearchRequest.skip,
                mongoSearchRequest.limit
        )

    }


    private fun <T> find(
            query: Bson,
            projection: Bson?,
            offset: Int,
            limit: Int?,
            sort: Bson?,
            documentMapper: (Document) -> T
    ): List<T> {

        val cursor = collection
                .find(query)
                .sort(sort)
                .skip(offset)

        limit?.let { cursor.limit(it) }

        projection?.let { cursor.projection(it) }

        val iterable = Iterable { cursor.iterator() }
        return iterable.map { documentMapper.invoke(it) }

    }


    private fun <T> findUsingAggregate(
            aggregations: List<Bson>,
            documentMapper: (Document) -> T
    ): MongoIterable<T> {

        val aggregate = this.collection.aggregate(aggregations)
        return aggregate.map(documentMapper)

    }


    fun <T> find(
            query: Bson,
            sortDocument: Document,
            documentMapper: Function<Document, T>
    ): List<T> {

        val cursor = collection
                .find(query)
                .sort(sortDocument)

        val iterable = Iterable { cursor.iterator() }
        return iterable.map { documentMapper.apply(it) }

    }


    fun <T> distinct(fieldName: String, resultClass: Class<T>): DistinctIterable<T> {

        return collection.distinct(fieldName, resultClass)

    }


    fun <T> distinct(fieldName: String, filter: Bson, resultClass: Class<T>): DistinctIterable<T> {

        return collection.distinct(fieldName, filter, resultClass)

    }


    fun <T : Comparable<T>> distinctSet(fieldName: String, resultClass: Class<T>): SortedSet<T> {

        val iterable = distinct(fieldName, resultClass)
        return iterable.toSortedSet()

    }


    fun <T : Comparable<T>> distinctSet(fieldName: String, filter: Bson, resultClass: Class<T>): SortedSet<T> {

        val iterable = distinct(fieldName, filter, resultClass)
        return iterable.toSortedSet()

    }


    private fun createSortFrom(sort: Sort, propertyNameToCollectionFieldNameMapper: (String) -> String): Bson {

        val sortDocument = Document()

        sort.forEach { order ->
            val collectionFieldName = getCollectionFieldNameFrom(order, propertyNameToCollectionFieldNameMapper)
            val directionInt = getDirectionFrom(order)
            sortDocument[collectionFieldName] = directionInt
        }

        return sortDocument

    }


    private fun getDirectionFrom(order: Sort.Order): Int {

        val direction = order.direction
        return directionFrom(direction)

    }


    private fun getCollectionFieldNameFrom(order: Sort.Order, propertyNameToCollectionFieldNameMapper: (String) -> String): String {

        val classFieldName = order.property
        return propertyNameToCollectionFieldNameMapper.invoke(classFieldName)

    }


    private fun directionFrom(direction: Sort.Direction): Int {

        return when (direction) {
            Sort.Direction.ASC -> 1
            Sort.Direction.DESC -> -1
            else -> throw RuntimeException("Unknown direction [$direction]")
        }

    }


    fun count(): Long {

        return collection.countDocuments()

    }


    fun count(filter: Bson): Long {

        return collection.countDocuments(filter)

    }


    fun exists(filter: Bson): Boolean {

        return collection.find(filter).any()

    }


    fun notExists(filter: Document): Boolean {

        return exists(filter) == false

    }


    fun updateOneById(id: ObjectId, update: Bson): UpdateResult {

        val filter = Document("_id", id)
        return collection.updateOne(filter, update)

    }


    fun updateOneByIdAndVersion(id: ObjectId, version: Long, update: Bson): UpdateResult {

        val filter = Document("_id", id).append("v", version)
        return collection.updateOne(filter, update)

    }


    fun deleteById(id: ObjectId): DeleteResult {

        return deleteOne(Document("_id", id))

    }


    fun deleteByIdAndVersion(id: ObjectId, version: Long): DeleteResult {

        return deleteOne(Document("_id", id).append("v", version))

    }


    fun deleteOne(filter: Bson): DeleteResult {

        return collection.deleteOne(filter)

    }


    fun deleteMany(filter: Bson): DeleteResult {

        return collection.deleteMany(filter)

    }


    fun readBoolean(`object`: Any): Boolean {

        return `object` as Boolean

    }


    fun readInteger(`object`: Any): Int {

        return `object` as Int

    }


    fun createIndex(indexKeys: Bson, indexOptions: IndexOptions) {

        logger.info("Creating index in collection '$collectionName' on database '${mongoClientFacade.defaultDatabase.name}: keys=$indexKeys, indexOptions=$indexOptions")
        val indexName = collection.createIndex(indexKeys, indexOptions)
        logger.info("Created index '$indexName' on collection '$collectionName'")

    }


    fun bulkWrite(writeModels: List<WriteModel<Document>>): BulkWriteResult {

        return if (writeModels.isEmpty()) {
            BulkWriteResult.unacknowledged()
        } else {
            collection.bulkWrite(writeModels)
        }

    }


    fun replaceOne(filter: Bson, replacement: Document): UpdateResult {

        return collection.replaceOne(filter, replacement)

    }


    fun drop() {

        if (this.droppable == false) {
            throw RuntimeException("This collection [${this.collectionName}] is not droppable.")
        }

        this.collection.drop()

    }


    fun find(filter: Bson): FindIterable<Document> {

        return this.collection.find(filter)

    }


    fun findAll(): FindIterable<Document> {

        return this.collection.find()

    }


}

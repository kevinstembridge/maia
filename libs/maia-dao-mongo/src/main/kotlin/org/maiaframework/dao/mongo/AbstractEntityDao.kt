package org.maiaframework.dao.mongo

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.AggregateIterable
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.WriteModel
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.maiaframework.domain.AbstractEntity
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.mongo.toObjectId
import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.maiaframework.domain.search.mongo.MongoPageableSearchRequest
import org.maiaframework.domain.search.mongo.MongoSearchRequest
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.function.Function

@Repository
abstract class AbstractEntityDao<ENTITY : AbstractEntity> protected constructor(collectionName: CollectionName, mongoClientFacade: MongoClientFacade) {


    val mongoCollectionFacade: MongoCollectionFacade =
        MongoCollectionFacade(collectionName, mongoClientFacade)


    val collectionName: CollectionName
        get() = this.mongoCollectionFacade.collectionName


    protected fun createIndex(keys: Bson, indexOptions: IndexOptions) {
        this.mongoCollectionFacade.createIndex(keys, indexOptions)
    }

    protected abstract val typeDiscriminator: String?


    fun insert(entity: ENTITY) {

        val entityDocument = prepForInsert(entity)
        this.mongoCollectionFacade.insert(entityDocument)

    }


    fun insertMany(entities: List<ENTITY>) {

        val documents = entities.map { this.prepForInsert(it) }

        this.mongoCollectionFacade.insertMany(documents)

    }


    fun insertMany(entities: List<ENTITY>, insertManyOptions: InsertManyOptions) {

        val documents = entities.map { this.prepForInsert(it) }

        this.mongoCollectionFacade.insertMany(documents, insertManyOptions)

    }


    private fun prepForInsert(entity: ENTITY): Document {

        return toDocumentFrom(entity)

    }


    protected abstract fun toDocumentFrom(entity: ENTITY): Document


    protected abstract fun toUpsertDocumentFrom(entity: ENTITY): Document


    protected abstract fun toEntityFrom(document: Document): ENTITY


    fun findByIdOrNull(id: DomainId): ENTITY? {

        return this.mongoCollectionFacade.findByIdOrNull(id.toObjectId(), { this.toEntityFrom(it) })

    }


    fun findById(id: DomainId): ENTITY {

        return this.mongoCollectionFacade.findById(id.toObjectId(), { this.toEntityFrom(it) })

    }


    protected fun findOne(query: Bson): ENTITY {

        return this.mongoCollectionFacade.findOne(query, { this.toEntityFrom(it) })

    }


    protected fun findOne(query: Bson, orderBy: Bson): ENTITY {

        return this.mongoCollectionFacade.findOne(query, orderBy, { this.toEntityFrom(it) })

    }


    protected fun findOne(query: Bson, orderBy: org.springframework.data.domain.Sort?): ENTITY {

        return this.mongoCollectionFacade.findOne(query, orderBy, { this.convertClassFieldNameToCollectionFieldName(it) }, { this.toEntityFrom(it) })

    }


    protected fun findOneOrNull(query: Bson): ENTITY? {

        return this.mongoCollectionFacade.findOneOrNull(query, { this.toEntityFrom(it) })

    }


    protected fun findOneOrNull(query: Bson, projection: Bson, orderBy: Bson): ENTITY? {

        return this.mongoCollectionFacade.findOneOrNull(query, projection, orderBy, { this.toEntityFrom(it) })

    }


    protected fun findOneOrNull(query: Bson, orderBy: org.springframework.data.domain.Sort? = null): ENTITY? {

        return this.mongoCollectionFacade.findOneOrNull(
                query = query,
                projection = null,
                orderBy = orderBy,
                propertyNameToCollectionFieldNameMapper = { this.convertClassFieldNameToCollectionFieldName(it)},
                documentMapper = { this.toEntityFrom(it) })

    }


    protected fun findOneAndUpdate(filter: Bson, update: Bson, findOneAndUpdateOptions: FindOneAndUpdateOptions): ENTITY {

        return this.mongoCollectionFacade.findOneAndUpdate(filter, update, findOneAndUpdateOptions, { this.toEntityFrom(it) })

    }


    protected fun findOneAndUpdateOrNull(filter: Bson, update: Bson, findOneAndUpdateOptions: FindOneAndUpdateOptions): ENTITY? {

        return this.mongoCollectionFacade.findOneAndUpdateOrNull(filter, update, findOneAndUpdateOptions, { this.toEntityFrom(it) })

    }


    fun search(mongoPageableSearchRequest: MongoPageableSearchRequest): org.springframework.data.domain.Page<ENTITY> {

        return this.mongoCollectionFacade.findPage(mongoPageableSearchRequest, { this.toEntityFrom(it) }, { this.convertClassFieldNameToCollectionFieldName(it) })

    }


    fun findAllAndIterate(): Sequence<ENTITY> {

        return this.mongoCollectionFacade.findAll().asSequence().map { this.toEntityFrom(it) }

    }


    fun find(query: Bson): List<ENTITY> {

        return this.mongoCollectionFacade.find(query, { this.toEntityFrom(it) })

    }


    fun findAndIterate(query: Bson): Sequence<ENTITY> {

        return this.mongoCollectionFacade.find(query).asSequence().map( {this.toEntityFrom(it) })

    }


    fun <T> findAndIterate(query: Bson, documentMapper: (Document) -> T): Sequence<T> {

        return this.mongoCollectionFacade.find(query).asSequence().map(documentMapper)

    }


    fun <T> findAndIterate(query: Bson, projection: Bson, documentMapper: (Document) -> T): Sequence<T> {

        return this.mongoCollectionFacade.find(query).projection(projection).asSequence().map(documentMapper)

    }


    fun findPage(pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<ENTITY> {

        val filter = Document()
        typeDiscriminator?.let { typeDiscriminator -> filter["TYP"] = typeDiscriminator }
        return findPage(filter, pageable)

    }


    fun findPage(query: Bson, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<ENTITY> {

        return this.mongoCollectionFacade.findPage(query, pageable, { this.toEntityFrom(it) }, { this.convertClassFieldNameToCollectionFieldName(it) })

    }


    fun find(mongoSearchRequest: MongoSearchRequest): List<ENTITY> {

        return this.mongoCollectionFacade.find(mongoSearchRequest, { this.toEntityFrom(it)})

    }


    protected abstract fun convertClassFieldNameToCollectionFieldName(classFieldName: String): String


    fun count(): Long {

        return this.mongoCollectionFacade.count()

    }


    protected fun count(filter: Bson): Long {

        return this.mongoCollectionFacade.count(filter)

    }


    protected fun exists(filter: Bson): Boolean {

        return this.mongoCollectionFacade.exists(filter)

    }


    protected fun updateOneById(id: DomainId, update: Bson): UpdateResult {

        return this.mongoCollectionFacade.updateOneById(id.toObjectId(), update)

    }


    protected fun updateOneByIdAndVersion(id: DomainId, version: Long?, update: Bson): UpdateResult {

        if (version == null) {
            return this.mongoCollectionFacade.updateOneById(id.toObjectId(), update)
        }

        val updateResult = this.mongoCollectionFacade.updateOneByIdAndVersion(id.toObjectId(), version, update)

        if (updateResult.modifiedCount != 1L) {
            throw OptimisticLockingException(collectionName, id, version)
        }

        return updateResult

    }


    fun deleteById(id: DomainId) {

        this.mongoCollectionFacade.deleteById(id.toObjectId())

    }


    fun removeById(id: DomainId): ENTITY? {

        val found = findByIdOrNull(id)

        if (found != null) {
            this.mongoCollectionFacade.deleteById(id.toObjectId())
        }

        return found

    }


    fun deleteByIdAndVersion(id: DomainId, version: Long): DeleteResult {

        val deleteResult = this.mongoCollectionFacade.deleteByIdAndVersion(id.toObjectId(), version)

        if (deleteResult.deletedCount != 1L) {
            throw OptimisticLockingException(collectionName, id, version)
        }

        return deleteResult

    }


    protected fun deleteOne(filter: Bson): DeleteResult {

        return this.mongoCollectionFacade.deleteOne(filter)

    }


    protected fun deleteMany(filter: Bson): DeleteResult {

        return this.mongoCollectionFacade.deleteMany(filter)

    }


    protected fun <SOURCE, T> readList(
            mapper: (SOURCE) -> T,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): List<T> {

        return DocumentFacade.readList(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T : Enum<T>> readEnum(
            enumClass: Class<T>,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): T {

        return DocumentFacade.readEnum(
            enumClass,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T : Enum<T>> readEnumList(
            enumClass: Class<T>,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): List<T> {

        return DocumentFacade.readEnumList(
            enumClass,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readBoolean(`object`: Any): Boolean? {

        return DocumentFacade.readBoolean(`object`)

    }


    protected fun readInteger(`object`: Any): Int? {

        return DocumentFacade.readInteger(`object`)

    }


    protected fun <T> readBoolean(
            mapper: (Boolean) -> T,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): T {

        return DocumentFacade.readBoolean(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readBoolean(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): Boolean {

        return DocumentFacade.readBoolean(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readObjectId(
        mapper: (ObjectId) -> T,
        collectionFieldName: String,
        classFieldName: String,
        document: Document
    ): T {

        return DocumentFacade.readObjectId_kt(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readObjectId(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): ObjectId {

        return DocumentFacade.readObjectId(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readInt(
            mapper: (Int) -> T,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): T {

        return DocumentFacade.readInt(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readInt(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): Int {

        return DocumentFacade.readInt(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readLong(
            mapper: (Long) -> T,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): T {

        return DocumentFacade.readLong(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readLong(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): Long {

        return DocumentFacade.readLong(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readDouble(
            mapper: (Double) -> T,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): T {

        return DocumentFacade.readDouble(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readDouble(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): Double {

        return DocumentFacade.readDouble(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readInstant(
        mapper: Function<Instant, T>,
        collectionFieldName: String,
        classFieldName: String,
        document: Document
    ): T {

        return DocumentFacade.readInstant(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readInstant(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): Instant {

        return DocumentFacade.readInstant(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readLocalDate(
        mapper: (LocalDate) -> T,
        collectionFieldName: String,
        classFieldName: String,
        document: Document
    ): T {

        return DocumentFacade.readLocalDate(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readLocalDate(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): LocalDate {

        return DocumentFacade.readLocalDate(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readString(
            mapper: (String) -> T,
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): T {

        return DocumentFacade.readString(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readString(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): String {

        return DocumentFacade.readString(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readStringList(collectionFieldName: String, classFieldName: String, document: Document): List<String> {

        return DocumentFacade.readStringList(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readInstantList(collectionFieldName: String, classFieldName: String, document: Document): List<Instant> {

        return DocumentFacade.readInstantList(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readLocalDateList(collectionFieldName: String, classFieldName: String, document: Document): List<LocalDate> {

        return DocumentFacade.readLocalDateList(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readPeriodList(collectionFieldName: String, classFieldName: String, document: Document): List<Period> {

        return DocumentFacade.readPeriodList(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <T> readPeriod(
        mapper: Function<Period, T>,
        collectionFieldName: String,
        classFieldName: String,
        document: Document
    ): T {

        return DocumentFacade.readPeriod(
            mapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun readPeriod(
            collectionFieldName: String,
            classFieldName: String,
            document: Document
    ): Period {

        return DocumentFacade.readPeriod(
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    protected fun <KEY, TARGET_VALUE> readMap(
        keyMapper: Function<String, KEY>,
        valueMapper: Function<Any, TARGET_VALUE>,
        collectionFieldName: String,
        classFieldName: String,
        document: Document
    ): Map<KEY, TARGET_VALUE> {

        return DocumentFacade.readMap(
            keyMapper,
            valueMapper,
            collectionFieldName,
            this.collectionName,
            classFieldName,
            document
        )

    }


    fun aggregate(pipeline: List<Bson>): AggregateIterable<Document> {

        return this.mongoCollectionFacade.collection.aggregate(pipeline)

    }


    fun bulkWrite(writeModels: List<WriteModel<Document>>): BulkWriteResult {

        return this.mongoCollectionFacade.bulkWrite(writeModels)

    }


}


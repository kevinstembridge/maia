package org.maiaframework.dao.mongo


import org.maiaframework.domain.AbstractHistoryEntity
import org.maiaframework.domain.AbstractVersionedEntity
import org.maiaframework.domain.types.CollectionName
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.maiaframework.domain.ChangeType
import org.maiaframework.domain.DomainId
import org.bson.conversions.Bson
import org.springframework.stereotype.Repository

@Repository
abstract class AbstractEntityWithHistoryDao<ENTITY : AbstractVersionedEntity, HISTORY_ENTITY: AbstractHistoryEntity>
protected constructor(
    collectionName: CollectionName,
    mongoClientFacade: MongoClientFacade,
    private val historyEntityDao: AbstractEntityDao<HISTORY_ENTITY>
): AbstractEntityDao<ENTITY>(
        collectionName,
        mongoClientFacade) {


    fun history(entity: ENTITY, changeType: ChangeType): HISTORY_ENTITY {
        return history(entity, entity.version, changeType)
    }


    abstract fun history(entity: ENTITY, v: Long, changeType: ChangeType): HISTORY_ENTITY


    override fun insert(entity: ENTITY) {

        super.insert(entity)
        insertHistory(entity, ChangeType.CREATE)

    }


    override fun insertMany(entities: List<ENTITY>) {

        super.insertMany(entities)
        val historyEntities = entities.map { history(it, ChangeType.CREATE) }
        this.historyEntityDao.insertMany(historyEntities)

    }


    override fun insertMany(entities: List<ENTITY>, insertManyOptions: InsertManyOptions) {

        super.insertMany(entities, insertManyOptions)
        val historyEntities = entities.map { history(it, ChangeType.CREATE) }
        this.historyEntityDao.insertMany(historyEntities, insertManyOptions)

    }


    override fun findOneAndUpdate(filter: Bson, update: Bson, findOneAndUpdateOptions: FindOneAndUpdateOptions): ENTITY {

        val previousVersionOfEntity = super.findOneAndUpdate(filter, update, findOneAndUpdateOptions)
        val updatedEntity = findById(previousVersionOfEntity.id)
        insertHistory(updatedEntity, ChangeType.UPDATE)

        return previousVersionOfEntity

    }


    override fun findOneAndUpdateOrNull(filter: Bson, update: Bson, findOneAndUpdateOptions: FindOneAndUpdateOptions): ENTITY? {

        val previousVersionOfEntity = super.findOneAndUpdateOrNull(filter, update, findOneAndUpdateOptions)

        if (previousVersionOfEntity == null) {
            return null
        }

        val updatedEntity = findById(previousVersionOfEntity.id)
        insertHistory(updatedEntity, ChangeType.UPDATE)

        return previousVersionOfEntity

    }


    fun updateOneById(id: DomainId, update: Bson, createHistoryRecord: Boolean): UpdateResult {

        val updateResult = super.updateOneById(id, update)

        if (createHistoryRecord) {
            val updatedEntity = findById(id)
            insertHistory(updatedEntity, ChangeType.UPDATE)
        }

        return updateResult

    }


    override fun updateOneById(id: DomainId, update: Bson): UpdateResult {

        val updateResult = super.updateOneById(id, update)
        val updatedEntity = findById(id)
        insertHistory(updatedEntity, ChangeType.UPDATE)

        return updateResult

    }


    fun updateOneByIdAndVersion(id: DomainId, version: Long, update: Bson, createHistoryRecord: Boolean): UpdateResult {

        if (createHistoryRecord) {
            return updateOneByIdAndVersion(id, version, update)
        }

        return super.updateOneByIdAndVersion(id, version, update)

    }


    override fun updateOneByIdAndVersion(id: DomainId, version: Long?, update: Bson): UpdateResult {

        val updateResult = super.updateOneByIdAndVersion(id, version, update)
        val updatedEntity = findById(id)
        insertHistory(updatedEntity, ChangeType.UPDATE)

        return updateResult

    }


    override fun deleteById(id: DomainId) {

        val found = findByIdOrNull(id)

        if (found != null) {
            super.deleteByIdAndVersion(found.id, found.version)
            insertHistory(found, found.version + 1, ChangeType.DELETE)
        }

    }

    override fun removeById(id: DomainId): ENTITY? {

        val removed = super.removeById(id)

        if (removed != null) {
            insertHistory(removed, removed.version + 1, ChangeType.DELETE)
        }

        return removed

    }

    override fun deleteOne(filter: Bson): DeleteResult {

        TODO("Delete by filter is not yet supported for history entities.")

    }


    private fun insertHistory(entity: ENTITY, changeType: ChangeType) {

        this.historyEntityDao.insert(history(entity, entity.version, changeType))

    }


    private fun insertHistory(entity: ENTITY, version: Long, changeType: ChangeType) {

        this.historyEntityDao.insert(history(entity, version, changeType))

    }


}

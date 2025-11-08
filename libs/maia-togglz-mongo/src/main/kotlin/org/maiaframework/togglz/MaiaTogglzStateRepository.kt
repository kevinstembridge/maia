package org.maiaframework.togglz

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import org.togglz.core.Feature
import org.togglz.core.repository.FeatureState
import org.togglz.core.repository.StateRepository

class MaiaTogglzStateRepository(
    private val mongoClient: MongoClient,
    private val databaseName: String,
    private val collection: String
) : StateRepository {


    override fun getFeatureState(feature: Feature): FeatureState? {

        val featureDocument: Document? = togglzCollection().find(queryFor(feature)).first()

        if (featureDocument == null) {
            return null
        }

        val featureState = FeatureState(feature)

        val enabled = featureDocument.getBoolean(FIELD_ENABLED, false)
        featureState.isEnabled = enabled

        val strategy: String? = featureDocument.getString(FIELD_STRATEGY)
        strategy?.let { featureState.strategyId = it.trim() }

        val fieldParams: Any? = featureDocument[FIELD_PARAMS]

        if (fieldParams is Document) {
            fieldParams.forEach { key, value -> featureState.setParameter(key, value.toString().trim()) }
        }

        return featureState

    }


    override fun setFeatureState(featureState: FeatureState) {

        val featureStateDocument = Document()
        featureStateDocument[FIELD_FEATURE] = featureState.feature.name()
        featureStateDocument[FIELD_ENABLED] = featureState.isEnabled
        featureState.strategyId?.let { featureStateDocument[FIELD_STRATEGY] = featureState.strategyId }

        if (featureState.parameterNames.isNotEmpty()) {
            val params = Document()
            featureState.parameterNames.forEach { paramName ->
                params[paramName] = featureState.getParameter(paramName)
            }
            featureStateDocument[FIELD_PARAMS] = params
        }

        val query = queryFor(featureState.feature)
        val replaceOptions = ReplaceOptions().upsert(true)

        togglzCollection().replaceOne(query, featureStateDocument, replaceOptions)

    }


    private fun queryFor(feature: Feature): Document {
        return Document(FIELD_FEATURE, feature.name())
    }


    private fun togglzCollection(): MongoCollection<Document> {
        val db = mongoClient.getDatabase(databaseName)
        return db.getCollection(collection)
    }


    companion object {

        private const val FIELD_FEATURE = "feature"
        private const val FIELD_ENABLED = "enabled"
        private const val FIELD_STRATEGY = "strategy"
        private const val FIELD_PARAMS = "params"

    }


}

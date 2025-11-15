package org.maiaframework.job.mongo

import org.maiaframework.job.batch.BatchItemWriter
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.dao.mongo.MongoCollectionFacade
import com.mongodb.client.model.InsertOneModel
import org.bson.Document

class MongoBatchItemWriter(private val mongoCollectionFacade: MongoCollectionFacade): BatchItemWriter<Document> {


    override fun writeItems(documents: List<Document>, jm: JobMetrics) {

        jm.timeChildJob("mongoBulkWrite") { jm1 ->

            val insertModels = documents
                    .map { InsertOneModel(it) }

            jm1.getOrCreateCounter("itemCount").inc(insertModels.size.toLong())

            this.mongoCollectionFacade.bulkWrite(insertModels)

        }

    }


}

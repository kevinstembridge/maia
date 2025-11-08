package org.maiaframework.job.mongo

import org.maiaframework.job.batch.BatchItemReader
import org.maiaframework.job.batch.BatchItemStream
import org.maiaframework.dao.mongo.MongoCollectionFacade
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCursor
import org.bson.Document


class MongoCursorBatchItemReader<T>(
    private val mongoCollectionFacade: MongoCollectionFacade,
    private val filter: Document,
    private val sort: Document,
    private val projection: Document? = null,
    private val documentMapper: (Document) -> T?
): BatchItemReader<T>, BatchItemStream {


    private lateinit var findIterable: FindIterable<Document>
    private var mongoCursor: MongoCursor<Document>? = null


    override fun openItemStream() {

        this.findIterable = this.mongoCollectionFacade.find(this.filter).sort(this.sort).noCursorTimeout(false)
        this.projection?.let { this.findIterable.projection(it) }
        this.mongoCursor = null

    }


    override fun readItem(): T? {

        val cursor = getOrInitMongoCursor()
        val next = cursor.tryNext()

        return next?.let(documentMapper)

    }


    private fun getOrInitMongoCursor(): MongoCursor<Document> {

        if (this.mongoCursor == null) {
            this.mongoCursor = this.findIterable.iterator()
        }

        return this.mongoCursor!!

    }


    override fun closeItemStream() {

        this.mongoCursor?.close()

    }


}

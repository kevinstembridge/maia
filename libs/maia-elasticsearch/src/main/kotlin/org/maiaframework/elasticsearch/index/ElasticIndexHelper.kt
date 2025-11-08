package org.maiaframework.elasticsearch.index

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import org.springframework.stereotype.Component

@Component
class ElasticIndexHelper(private val client: ElasticsearchClient) {


    fun dropIndex(indexName: String) {

        try {
            this.client.indices().delete { d -> d.index(indexName) }
        } catch (e: ElasticsearchException) {
            // do nothing
            println(e)
        }

        // TODO should I be doing something with the response?

    }


}

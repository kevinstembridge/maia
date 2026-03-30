package org.maiaframework.showcase.testing

import org.testcontainers.elasticsearch.ElasticsearchContainer

class SingletonElasticsearchContainer(
    dockerImageName: String
): ElasticsearchContainer(
    dockerImageName
) {


    override fun stop() {
        // do nothing
    }


    companion object {

        val instance = SingletonElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.17.0")

    }


}

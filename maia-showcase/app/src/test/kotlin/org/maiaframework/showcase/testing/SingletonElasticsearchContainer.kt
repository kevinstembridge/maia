package org.maiaframework.showcase.testing

import org.testcontainers.elasticsearch.ElasticsearchContainer

class SingletonElasticsearchContainer(
    dockerImageName: String
): ElasticsearchContainer(
    dockerImageName
) {


    init {
        // Disable security so @ServiceConnection uses HTTP, avoiding a shaded-commons-lang3 conflict
        // between testcontainers-elasticsearch:1.21.4 and testcontainers:2.0.3 (ES 8.x requires HTTPS
        // by default but @ServiceConnection can't configure SSL due to the classpath mismatch)
        withEnv("xpack.security.enabled", "false")
    }


    override fun stop() {
        // do nothing
    }


    companion object {

        val instance = SingletonElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.2.4")

    }


}

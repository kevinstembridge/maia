package org.maiaframework.showcase.history

import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus


class HistoryBlotterEndpointTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var dao: HistorySampleDao


    @Test
    fun `search returns 2 history records after create and update`() {

        // GIVEN a sample entity inserted into the database (produces version 1 / CREATE history record)
        val entity = HistorySampleEntityTestBuilder().build()
        val entityId = entity.id
        this.dao.insert(entity)

        // WHEN we update the entity (produces version 2 / UPDATE history record)
        val entityV1 = this.dao.findByPrimaryKey(entityId)
        val updater = HistorySampleEntityUpdater.forPrimaryKey(entityV1.id, entityV1.version) {
            someInt(entityV1.someInt + 1)
            someString(entityV1.someString + "_updated")
        }
        this.dao.setFields(updater)

        // AND we call the history search endpoint with an empty search model
        val requestBody = """
            {
                "filterModel": {},
                "sortModel": [],
                "startRow": 0
            }
        """.trimIndent()

        // THEN we get HTTP 200 and 2 results
        assertThat_POST("/api/history-sample/$entityId/history/search", requestBody)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .isLenientlyEqualTo("""{"totalResultCount": 2}""")

    }


}

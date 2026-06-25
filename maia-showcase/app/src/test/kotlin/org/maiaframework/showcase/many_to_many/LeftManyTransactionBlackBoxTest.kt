package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class LeftManyTransactionBlackBoxTest : AbstractBlackBoxTest() {

    @Autowired
    private lateinit var leftManyDao: LeftManyDao

    @BeforeEach
    fun cleanUp() {
        jdbcOps.update("truncate table ${LeftManyEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
    }

    @Test
    fun `nothing is persisted when a join insert fails`() {
        assertThat(leftManyDao.count()).isZero()

        val nonExistentRightId = DomainId.newId()

        assertThat_POST(
            "/api/left-many/create",
            """
            {
                "someInt": 1,
                "someString": "test-rollback",
                "rightSimpleEntityIds": ["$nonExistentRightId"]
            }
            """.trimIndent()
        ).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)

        assertThat(leftManyDao.count()).isZero()
    }

}

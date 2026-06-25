package org.maiaframework.showcase.many_to_many

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.test.context.support.WithMockUser

class LeftManyTransactionBlackBoxTest : AbstractBlackBoxTest() {

    @Autowired
    private lateinit var leftManyDao: LeftManyDao

    @Autowired
    private lateinit var leftManyCrudService: LeftManyCrudService

    @BeforeEach
    fun cleanUp() {
        jdbcOps.update("truncate table ${LeftManyEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
    }

    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `nothing is persisted when a join insert fails`() {
        assertThat(leftManyDao.count()).isZero()

        val nonExistentRightId = DomainId.newId()

        val createDto = LeftManyCreateRequestDto(
            rightEntities_raw = emptyList(),
            rightSimpleEntityIds_raw = listOf(nonExistentRightId),
            rightSystemEffectiveEntities_raw = emptyList(),
            rightUserEffectiveEntities_raw = emptyList(),
            someInt_raw = 1,
            someString_raw = "test-rollback"
        )

        assertThatThrownBy { leftManyCrudService.create(createDto) }
            .isInstanceOf(DataIntegrityViolationException::class.java)

        assertThat(leftManyDao.count()).isZero()
    }

}

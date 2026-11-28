package org.maiaframework.showcase.hazelcast_blotter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaCreateRequestDto
import org.maiaframework.showcase.join.AlphaCrudService
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.BravoCreateRequestDto
import org.maiaframework.showcase.join.BravoCrudService
import org.maiaframework.showcase.join.BravoEntityMeta
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRebuildService
import org.maiaframework.showcase.join.BravoHzDtoHazelcastRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser


/**
 * Black-box test of the rebuild service of the Hazelcast-backed Blotter `BravoHz`: after the database is changed behind
 * the map's back, rebuilding makes the fetch-all result match the database again.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastRebuildServiceIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var alphaCrudService: AlphaCrudService


    @Autowired
    private lateinit var bravoCrudService: BravoCrudService


    @Autowired
    private lateinit var bravoHzRepo: BravoHzDtoHazelcastRepo


    @Autowired
    private lateinit var rebuildService: BravoHzDtoHazelcastRebuildService


    @BeforeEach
    fun resetDatabase() {

        // cascades to bravo
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")
        bravoHzRepo.populate()

    }


    @Test
    fun `rebuild makes the fetch-all result match rows changed directly in the database`() {

        val alpha = createAlpha()
        val changed = bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = 1, someString_raw = "before"))
        val removed = bravoCrudService.create(BravoCreateRequestDto(alpha_raw = alpha.id, someInt_raw = 2, someString_raw = "removed"))
        bravoHzRepo.populate()
        assertThat(bravoHzRepo.findAll().map { it.dtoStringFromBravo }).containsExactlyInAnyOrder("before", "removed")

        jdbcOps.update("update ${BravoEntityMeta.SCHEMA_AND_TABLE_NAME} set some_string = 'after' where id = '${changed.id.value}'")
        jdbcOps.update("delete from ${BravoEntityMeta.SCHEMA_AND_TABLE_NAME} where id = '${removed.id.value}'")

        rebuildService.rebuild()

        val rows = bravoHzRepo.findAll()
        assertThat(rows.map { it.id }).containsExactly(changed.id)
        assertThat(rows.single().dtoStringFromBravo).isEqualTo("after")

    }


    private fun createAlpha(): AlphaEntity {

        return alphaCrudService.create(AlphaCreateRequestDto(someInt_raw = 1, someString_raw = "alpha"))

    }


}

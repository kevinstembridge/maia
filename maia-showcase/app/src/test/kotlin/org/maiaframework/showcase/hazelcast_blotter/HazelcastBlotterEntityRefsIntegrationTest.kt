package org.maiaframework.showcase.hazelcast_blotter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.join.AlphaDao
import org.maiaframework.showcase.join.AlphaEntity
import org.maiaframework.showcase.join.AlphaEntityMeta
import org.maiaframework.showcase.join.AlphaEntityTestBuilder
import org.maiaframework.showcase.join.BravoAlphaHzDtoHazelcastRepo
import org.maiaframework.showcase.join.BravoDao
import org.maiaframework.showcase.join.BravoEntity
import org.maiaframework.showcase.join.BravoEntityTestBuilder
import org.maiaframework.showcase.join.CharlieDao
import org.maiaframework.showcase.join.CharlieEntity
import org.maiaframework.showcase.join.CharlieEntityTestBuilder
import org.maiaframework.showcase.join.DeltaDao
import org.maiaframework.showcase.join.DeltaEntity
import org.maiaframework.showcase.join.DeltaEntityTestBuilder
import org.maiaframework.showcase.join.DeltaHzDtoHazelcastRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper


/**
 * Black-box tests of the rich, denormalized row of Hazelcast-backed Blotters (ADR-0007, ADR-0008): a foreign key to an
 * Entity with an Entity ref is a ref, a nullable one is a null ref, a foreign key to an Entity without an Entity ref
 * stays a plain id, and a path field is flattened into the document.
 */
@WithMockUser(authorities = ["WRITE"])
class HazelcastBlotterEntityRefsIntegrationTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var jsonMapper: JsonMapper


    @Autowired
    private lateinit var alphaDao: AlphaDao


    @Autowired
    private lateinit var bravoDao: BravoDao


    @Autowired
    private lateinit var charlieDao: CharlieDao


    @Autowired
    private lateinit var deltaDao: DeltaDao


    @Autowired
    private lateinit var bravoAlphaHzRepo: BravoAlphaHzDtoHazelcastRepo


    @Autowired
    private lateinit var deltaHzRepo: DeltaHzDtoHazelcastRepo


    @BeforeEach
    fun resetDatabase() {

        // cascades to bravo, charlie and delta
        jdbcOps.update("truncate table ${AlphaEntityMeta.SCHEMA_AND_TABLE_NAME} cascade")

    }


    @Test
    fun `a required foreign key is a ref and a path field is flattened into the row`() {

        val alpha = insertAlpha("alpha-display")
        val bravo = insertBravo(alpha, "bravo-text")
        bravoAlphaHzRepo.populate()

        val rows = fetchAll("/api/bravo-alpha-hz-blotter/find-all")

        assertThat(rows).hasSize(1)
        val row = rows.single()
        assertThat(row["id"].asString()).isEqualTo(bravo.id.value)
        assertThat(row["alpha"]["id"].asString()).isEqualTo(alpha.id.value)
        assertThat(row["alpha"]["displayName"].asString()).isEqualTo("alpha-display")
        assertThat(row["dtoStringFromAlpha"].asString()).isEqualTo("alpha-display")
        assertThat(row["dtoStringFromBravo"].asString()).isEqualTo("bravo-text")

    }


    @Test
    fun `a nullable foreign key is a ref or null and a foreign key to an Entity without an Entity ref stays a plain id`() {

        val alpha = insertAlpha("delta-alpha-display")
        val charlie = insertCharlie(insertBravo(insertAlpha("other-alpha"), "other-bravo"))
        val withAlpha = insertDelta(DeltaEntityTestBuilder(alphaId = alpha.id, charlieId = charlie.id, someString = "delta-with").build())
        val withoutAlpha = insertDelta(DeltaEntityTestBuilder(alphaId = null, charlieId = charlie.id, someString = "delta-without").build())
        deltaHzRepo.populate()

        val rows = fetchAll("/api/delta-hz-blotter/find-all").associateBy { it["id"].asString() }

        assertThat(rows).hasSize(2)

        val withAlphaRow = rows.getValue(withAlpha.id.value)
        assertThat(withAlphaRow["alpha"]["id"].asString()).isEqualTo(alpha.id.value)
        assertThat(withAlphaRow["alpha"]["displayName"].asString()).isEqualTo("delta-alpha-display")
        assertThat(withAlphaRow["charlie"].isString).isTrue()
        assertThat(withAlphaRow["charlie"].asString()).isEqualTo(charlie.id.value)

        val withoutAlphaRow = rows.getValue(withoutAlpha.id.value)
        assertThat(withoutAlphaRow["alpha"] == null || withoutAlphaRow["alpha"].isNull).isTrue()
        assertThat(withoutAlphaRow["charlie"].asString()).isEqualTo(charlie.id.value)

    }


    private fun fetchAll(url: String): List<JsonNode> {

        val result = mockMvc.get().uri(url)
            .with(user("nigel").authorities(SimpleGrantedAuthority("WRITE")))
            .exchange()

        // MockMvc's security filter chain clears the thread's SecurityContext; restore the @WithMockUser one
        SecurityContextHolder.setContext(TestSecurityContextHolder.getContext())

        assertThat(result.response.status).isEqualTo(200)

        return jsonMapper.readTree(result.response.contentAsString).toList()

    }


    private fun insertAlpha(someString: String): AlphaEntity {

        return AlphaEntityTestBuilder(someString = someString).build().also { alphaDao.insert(it) }

    }


    private fun insertBravo(alpha: AlphaEntity, someString: String): BravoEntity {

        return BravoEntityTestBuilder(alphaId = alpha.id, someString = someString).build().also { bravoDao.insert(it) }

    }


    private fun insertCharlie(bravo: BravoEntity): CharlieEntity {

        return CharlieEntityTestBuilder(bravoId = bravo.id).build().also { charlieDao.insert(it) }

    }


    private fun insertDelta(delta: DeltaEntity): DeltaEntity {

        deltaDao.insert(delta)
        return delta

    }


}

package org.maiaframework.toggles

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.maiaframework.toggles.activation.ActivationStrategyParameter
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.ValueMatcher
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.json.JsonAssert
import org.springframework.test.web.servlet.assertj.MockMvcTester


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FeatureToggleBlackBoxTest : AbstractBlackBoxTest() {


    private val ignoreValueMatcher = ValueMatcher<Any> { _, _ -> true }


    private val createdTimestampCustomization = Customization.customization("**.createdTimestamp", ignoreValueMatcher)


    private val lastModifiedTimestampCustomization = Customization.customization("**.lastModifiedTimestamp", ignoreValueMatcher)


    private val idCustomization = Customization.customization("**.id", ignoreValueMatcher)


    private val versionCustomization = Customization.customization("**.version", ignoreValueMatcher)


    private val jsonComparator = CustomComparator(JSONCompareMode.STRICT, createdTimestampCustomization, lastModifiedTimestampCustomization, idCustomization, versionCustomization)


    private val jsonAssertComparator = JsonAssert.comparator(jsonComparator)


    private val historySearchBody = """{"filterModel": {}, "sortModel": [], "startRow": 0, "endRow": 10}"""


    @Test
    @WithMockUser(username = "muriel", authorities = ["MAIA_TOGGLES_READ", "MAIA_TOGGLES_WRITE"])
    fun `journey test`(@Autowired mockMvc: MockMvcTester) {

        `list all toggles`(mockMvc)

        `assert that the toggle is inactive`(mockMvc)

        `assert that the toggle is active`("SampleFeatureTwo", mockMvc)

        `enable SampleFeatureOne`(mockMvc)

        `assert that the toggle is active`("SampleFeatureOne", mockMvc)

        `set an activation strategy named`("alwaysActiveStrategy", 2, mockMvc)

        `assert that the toggle is active`("SampleFeatureOne", mockMvc)

        `set an activation strategy named`("alwaysInactiveStrategy", 3, mockMvc)

        `assert that the toggle is inactive`(mockMvc)

        `set an activation strategy named`("maiaTogglesUsernameActivationStrategy", 4, mockMvc, listOf(ActivationStrategyParameter("usernames", "kathleen")))

        `assert that the toggle is inactive`(mockMvc)

        `set an activation strategy named`("maiaTogglesUsernameActivationStrategy", 5, mockMvc, listOf(ActivationStrategyParameter("usernames", "muriel")))

        `assert that the toggle is active`("SampleFeatureOne", mockMvc)

    }


    @Test
    @WithMockUser(username = "nobody")
    fun `a user without any toggles authority is forbidden from every endpoint`(@Autowired mockMvc: MockMvcTester) {

        assertThat(mockMvc.get().uri("/api/ops/toggles/toggles")).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(mockMvc.get().uri("/api/ops/toggles/strategies")).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(mockMvc.get().uri("/api/ops/toggles/SampleFeatureOne/is-active")).hasStatus(HttpStatus.FORBIDDEN)

        assertThat(
            mockMvc.post()
                .uri("/api/ops/toggles/feature-toggle/00000000-0000-0000-0000-000000000000/history/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(historySearchBody)
        ).hasStatus(HttpStatus.FORBIDDEN)

        assertThat(
            mockMvc.post()
                .uri("/api/ops/toggles/set-feature-toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(mapOf("featureName" to "SampleFeatureOne", "enabled" to true, "version" to 1)))
        ).hasStatus(HttpStatus.FORBIDDEN)

        assertThat(
            mockMvc.put()
                .uri("/api/ops/toggles/feature-toggle/inline/activation-strategies")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(mapOf("activationStrategies" to emptyList<String>(), "id" to "00000000-0000-0000-0000-000000000000", "version" to 1)))
        ).hasStatus(HttpStatus.FORBIDDEN)

    }


    @Test
    @WithMockUser(username = "reader", authorities = ["MAIA_TOGGLES_READ"])
    fun `a read-only user can read but not write`(@Autowired mockMvc: MockMvcTester) {

        assertThat(mockMvc.get().uri("/api/ops/toggles/toggles")).hasStatusOk()
        assertThat(mockMvc.get().uri("/api/ops/toggles/strategies")).hasStatusOk()
        assertThat(mockMvc.get().uri("/api/ops/toggles/SampleFeatureTwo/is-active")).hasStatusOk()

        assertThat(
            mockMvc.post()
                .uri("/api/ops/toggles/feature-toggle/${`id of`("SampleFeatureTwo", mockMvc)}/history/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(historySearchBody)
        ).hasStatusOk()

        assertThat(
            mockMvc.post()
                .uri("/api/ops/toggles/set-feature-toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(mapOf("featureName" to "SampleFeatureOne", "enabled" to true, "version" to 1)))
        ).hasStatus(HttpStatus.FORBIDDEN)

        assertThat(
            mockMvc.put()
                .uri("/api/ops/toggles/feature-toggle/inline/activation-strategies")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(mapOf("activationStrategies" to emptyList<String>(), "id" to "00000000-0000-0000-0000-000000000000", "version" to 1)))
        ).hasStatus(HttpStatus.FORBIDDEN)

    }


    @Test
    @WithMockUser(username = "reader", authorities = ["MAIA_TOGGLES_READ"])
    fun `lists the registered activation strategies and their parameters`(@Autowired mockMvc: MockMvcTester) {

        assertThat(mockMvc.get().uri("/api/ops/toggles/strategies"))
            .hasStatusOk()
            .bodyJson()
            .isEqualTo(
                asJson(
                    listOf(
                        mapOf("id" to "alwaysActiveStrategy", "description" to null, "parameters" to emptyList<String>()),
                        mapOf("id" to "alwaysInactiveStrategy", "description" to null, "parameters" to emptyList<String>()),
                        mapOf(
                            "id" to "maiaTogglesUsernameActivationStrategy",
                            "description" to "Active only for the listed users.",
                            "parameters" to listOf(
                                mapOf("name" to "usernames", "description" to "Comma-separated list of usernames", "required" to true)
                            )
                        ),
                    )
                )
            )

    }


    @Test
    @WithMockUser(username = "writer", authorities = ["MAIA_TOGGLES_READ", "MAIA_TOGGLES_WRITE"])
    fun `rejects invalid activation strategies with a 400`(@Autowired mockMvc: MockMvcTester) {

        val featureId = `id of`("SampleFeatureTwo", mockMvc)

        fun putStrategy(descriptor: Map<String, Any>) = mockMvc.put()
            .uri("/api/ops/toggles/feature-toggle/inline/activation-strategies")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(mapOf("activationStrategies" to listOf(descriptor), "id" to featureId, "version" to 1)))

        assertThat(putStrategy(mapOf("id" to "noSuchStrategy", "parameters" to emptyList<String>())))
            .hasStatus(HttpStatus.BAD_REQUEST)

        assertThat(putStrategy(mapOf("id" to "maiaTogglesUsernameActivationStrategy", "parameters" to emptyList<String>())))
            .hasStatus(HttpStatus.BAD_REQUEST)

        assertThat(putStrategy(mapOf("id" to "alwaysActiveStrategy", "parameters" to listOf(mapOf("name" to "bogus", "value" to "x")))))
            .hasStatus(HttpStatus.BAD_REQUEST)

    }


    private fun `list all toggles`(mockMvc: MockMvcTester) {

        assertThat(mockMvc.get().uri("/api/ops/toggles/toggles"))
            .debug()
            .hasStatusOk()
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()
            .isEqualTo(
                asJson(
                    listOf(
                        mapOf(
                            "activationStrategies" to emptyList<String>(),
                            "attributes" to null,
                            "comment" to "Initial creation by system",
                            "contactPerson" to "Muriel",
                            "createdTimestamp" to "ignored",
                            "description" to null,
                            "enabled" to false,
                            "featureName" to "SampleFeatureOne",
                            "id" to "ignored",
                            "infoLink" to null,
                            "lastModifiedBy" to "SYSTEM",
                            "lastModifiedTimestamp" to "ignored",
                            "reviewDate" to null,
                            "ticketKey" to null,
                            "version" to 1,
                        ),
                        mapOf(
                            "activationStrategies" to emptyList<String>(),
                            "attributes" to null,
                            "comment" to "Initial creation by system",
                            "contactPerson" to "Muriel",
                            "createdTimestamp" to "ignored",
                            "description" to null,
                            "enabled" to true,
                            "featureName" to "SampleFeatureTwo",
                            "id" to "ignored",
                            "infoLink" to null,
                            "lastModifiedBy" to "SYSTEM",
                            "lastModifiedTimestamp" to "ignored",
                            "reviewDate" to null,
                            "ticketKey" to null,
                            "version" to 1,
                        ),
                    ),
                ),
                this.jsonAssertComparator
            )

    }


    private fun `enable SampleFeatureOne`(mockMvc: MockMvcTester) {

        assertThat(
            mockMvc.post()
                .uri("/api/ops/toggles/set-feature-toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    asJson(
                        mapOf(
                            "featureName" to "SampleFeatureOne",
                            "comment" to "Updated comment",
                            "enabled" to true,
                            "version" to 1
                        )
                    )
                )
        )
            .debug()
            .hasStatusOk()

    }


    private fun `assert that the toggle is active`(
        featureName: String,
        mockMvc: MockMvcTester
    ) {

        `assert that the toggle has active state`(mockMvc, featureName, true)

    }


    private fun `assert that the toggle is inactive`(mockMvc: MockMvcTester) {

        `assert that the toggle has active state`(mockMvc, "SampleFeatureOne", false)

    }


    private fun `assert that the toggle has active state`(
        mockMvc: MockMvcTester,
        featureName: String,
        activeFlag: Boolean
    ) {

        assertThat(mockMvc.get().uri("/api/ops/toggles/$featureName/is-active"))
            .debug()
            .hasStatusOk()
            .bodyJson()
            .isEqualTo(asJson(mapOf("active" to activeFlag)), this.jsonAssertComparator)

    }


    private fun `set an activation strategy named`(
        activationStrategyName: String,
        version: Long,
        mockMvc: MockMvcTester,
        strategyParameters: List<ActivationStrategyParameter> = emptyList()
    ) {

        assertThat(mockMvc.put()
            .with(csrf())
            .uri("/api/ops/toggles/feature-toggle/inline/activation-strategies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(mapOf(
                "activationStrategies" to listOf(
                    mapOf(
                        "id" to activationStrategyName,
                        "parameters" to strategyParameters
                    )
                ),
                "id" to `id of`("SampleFeatureOne", mockMvc),
                "version" to version
            )))

        ).debug()
            .hasStatusOk()

    }


    private fun `id of`(featureName: String, mockMvc: MockMvcTester): String {

        val body = mockMvc.get().uri("/api/ops/toggles/toggles").exchange().response.contentAsString
        val toggles = jsonMapper.readTree(body)
        return toggles.first { it["featureName"].asString() == featureName }["id"].asString()

    }


}

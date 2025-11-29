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
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.json.JsonAssert
import org.springframework.test.web.servlet.assertj.MockMvcTester


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FeatureToggleBlackBoxTest : AbstractBlackBoxTest() {


    private val ignoreValueMatcher = ValueMatcher<Any> { _, _ -> true }


    private val createdTimestampUtcCustomization = Customization.customization("**.createdTimestampUtc", ignoreValueMatcher)


    private val lastModifiedTimestampUtcCustomization = Customization.customization("**.lastModifiedTimestampUtc", ignoreValueMatcher)


    private val jsonComparator = CustomComparator(JSONCompareMode.STRICT, createdTimestampUtcCustomization, lastModifiedTimestampUtcCustomization)


    private val jsonAssertComparator = JsonAssert.comparator(jsonComparator)


    @Test
    @WithMockUser(username = "muriel")
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


    private fun `list all toggles`(mockMvc: MockMvcTester) {

        assertThat(mockMvc.get().uri("/api/maia_toggles/toggles"))
            .debug()
            .hasStatusOk()
            .hasContentType(MediaType.APPLICATION_JSON)
            .bodyJson()
            .isEqualTo(
                asJson(
                    listOf(
                        mapOf(
                            "featureName" to "SampleFeatureOne",
                            "comment" to "Initial creation by system",
                            "contactPerson" to "Muriel",
                            "createdTimestampUtc" to "ignored",
                            "enabled" to false,
                            "lastModifiedBy" to "SYSTEM",
                            "lastModifiedTimestampUtc" to "ignored",
                        ),
                        mapOf(
                            "featureName" to "SampleFeatureTwo",
                            "comment" to "Initial creation by system",
                            "contactPerson" to "Muriel",
                            "createdTimestampUtc" to "ignored",
                            "enabled" to true,
                            "lastModifiedBy" to "SYSTEM",
                            "lastModifiedTimestampUtc" to "ignored",
                        ),
                    ),
                ),
                this.jsonAssertComparator
            )

    }


    private fun `enable SampleFeatureOne`(mockMvc: MockMvcTester) {

        assertThat(
            mockMvc.post()
                .uri("/api/maia_toggles/set_feature_toggle")
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

        assertThat(mockMvc.get().uri("/api/maia_toggles/$featureName/is_active"))
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
            .uri("/api/feature_toggle/inline/activation_strategies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(mapOf(
                "activationStrategies" to listOf(
                    mapOf(
                        "id" to activationStrategyName,
                        "parameters" to strategyParameters
                    )
                ),
                "featureName" to "SampleFeatureOne",
                "version" to version
            )))

        ).debug()
            .hasStatusOk()

    }


}

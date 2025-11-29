package org.maiaframework.toggles

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
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
    @WithMockUser
    @Order(1)
    fun `should list all feature toggles`(@Autowired mockMvc: MockMvcTester) {

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


    @Test
    @WithMockUser
    fun `should set the enabled flag of a specific feature toggle`(@Autowired mockMvc: MockMvcTester) {

        assertThat(mockMvc.post()
            .uri("/api/maia_toggles/set_feature_toggle")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(mapOf(
                "featureName" to "SampleFeatureOne",
                "comment" to "Updated comment",
                "enabled" to true,
                "version" to 1
            ))))
            .debug()
            .hasStatusOk()

        assertThat(mockMvc.get().uri("/api/maia_toggles/SampleFeatureOne/is_active"))
            .debug()
            .hasStatusOk()
            .bodyJson()
            .isEqualTo(asJson(mapOf("active" to true)), this.jsonAssertComparator)

    }


    @Test
    @WithMockUser
    fun `should return false if an ActivationStrategy does not pass`(@Autowired mockMvc: MockMvcTester) {

        assertThat(mockMvc.post()
            .uri("/api/maia_toggles/set_feature_toggle")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(mapOf(
                "featureName" to "SampleFeatureOne",
                "comment" to "Updated comment",
                "enabled" to true,
                "version" to 1
            ))))
            .debug()
            .hasStatusOk()

        assertThat(mockMvc.put()
            .with(csrf())
            .uri("/api/feature_toggle/inline/activation_strategies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJson(mapOf(
                "activationStrategies" to listOf(
                    mapOf(
                        "id" to "alwaysTrueStrategy",
                        "parameters" to emptyList<ActivationStrategyParameter>()
                    ),
                    mapOf(
                        "id" to "alwaysFalseStrategy",
                        "parameters" to emptyList<ActivationStrategyParameter>()
                    )
                ),
                "featureName" to "SampleFeatureOne",
                "version" to 2
            ))))
            .debug()
            .hasStatusOk()

        assertThat(mockMvc.get().uri("/api/maia_toggles/SampleFeatureOne/is_active"))
            .debug()
            .hasStatusOk()
            .bodyJson()
            .isEqualTo(asJson(mapOf("active" to false)), this.jsonAssertComparator)

    }


}

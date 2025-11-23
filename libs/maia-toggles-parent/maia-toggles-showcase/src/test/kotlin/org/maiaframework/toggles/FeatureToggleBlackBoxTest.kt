package org.maiaframework.toggles

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.ValueMatcher
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
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
    fun `should get a specific feature toggle`(@Autowired mockMvc: MockMvcTester) {

        assertThat(mockMvc.post().uri("/api/maia_toggles/set_feature_toggle")
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


}

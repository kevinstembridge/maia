package org.maiaframework.toggles

import org.maiaframework.testing.spring.JsonResultMatcher
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.ValueMatcher
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


class FeatureToggleBlackBoxTest : AbstractBlackBoxTest() {


    private val ignoreValueMatcher = ValueMatcher<Any> { _, _ -> true }


    private val createdTimestampUtcCustomization = Customization.customization("**.createdTimestampUtc", ignoreValueMatcher)


    private val lastModifiedTimestampUtcCustomization = Customization.customization("**.lastModifiedTimestampUtc", ignoreValueMatcher)


    private val jsonResultMatcher = JsonResultMatcher(
        CustomComparator(JSONCompareMode.STRICT, createdTimestampUtcCustomization, lastModifiedTimestampUtcCustomization)
    )


    @Test
    fun `should list all feature toggles`(@Autowired mockMvc: MockMvc) {

        mockMvc.get("/api/maia/toggles")
            .andDo { print() }
            .andExpect {
                status { isOk() }
                content {
                    contentType("application/json")
                }
                match(
                    jsonResultMatcher.asJson(
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
                        )
                    )
                )
            }

    }


}

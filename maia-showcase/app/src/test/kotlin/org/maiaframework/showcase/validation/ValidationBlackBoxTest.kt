package org.maiaframework.showcase.validation

import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.http.HttpStatus

class ValidationBlackBoxTest : AbstractBlackBoxTest() {


    @Test
    fun `test all validators`() {

        assertThat_POST("/api/all_field_types/create", "{}")
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .isLenientlyEqualTo("""
                {
                    "detail": "Invalid request content.",
                    "errors": {
                        "someBoolean_raw": ["must not be null"],
                        "someBooleanType_raw": ["must not be null"],
                        "someBooleanTypeProvided_raw": ["must not be null"],
                        "someInstant_raw": ["must not be null"],
                        "someInstantModifiable_raw": ["must not be null"],
                        "someInt_raw": ["must not be null"],
                        "someIntModifiable_raw": ["must not be null"],
                        "someIntType_raw": ["must not be null"],
                        "someIntTypeProvided_raw": ["must not be null"],
                        "someListOfStrings_raw": ["must not be null"],
                        "someLocalDateModifiable_raw": ["must not be null"],
                        "someLongType_raw": ["must not be null"],
                        "someLongTypeProvided_raw": ["must not be null"],
                        "somePeriodModifiable_raw": ["must not be blank"],
                        "someProvidedStringType_raw": ["must not be blank"],
                        "someString_raw": ["must not be blank"],
                        "someStringModifiable_raw": ["must not be blank"],
                        "someStringType_raw": ["must not be blank"]
                    }
                }
            """.trimIndent())

    }


}

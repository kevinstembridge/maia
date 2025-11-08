package org.maiaframework.dynamic_filter

import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DynamicStringFilterTest {


    private val includePatternPropertyName = anyString()


    private val excludePatternPropertyName = anyString()


    private val currentProperties = mutableMapOf<String, String?>()


    private val filter = DynamicStringFilter(
        includePatternPropertyName,
        excludePatternPropertyName,
        "Some Display Name",
        "Some description"
    ) { propertyName -> this.currentProperties[propertyName] }


    @Test
    fun `should accept if include and exclude patterns are null`() {

        val input = anyString()
        currentProperties[includePatternPropertyName] = null
        currentProperties[excludePatternPropertyName] = null

        assertThat(filter.accepts(input)).isTrue()
        assertThat(filter.rejects(input)).isFalse()

    }


    @Test
    fun `should accept if input is null or empty`() {

        currentProperties[includePatternPropertyName] = "abc"
        currentProperties[excludePatternPropertyName] = null

        assertThat(filter.accepts("   ")).isTrue()

    }


    @ParameterizedTest
    @CsvSource(
        "abc.*,abcdef",
        "^(123|ALPHA)$,ALPHA",
        "555,555"
    )
    fun `should accept if it matches the include pattern while exclude pattern is null`(
        includePattern: String,
        input: String
    ) {

        currentProperties[includePatternPropertyName] = includePattern
        currentProperties[excludePatternPropertyName] = null

        assertThat(filter.accepts(input)).isTrue()
        assertThat(filter.rejects(input)).isFalse()

    }


    @ParameterizedTest
    @CsvSource(
        "abc.*,xyz.*,abcdef",
        "^(123|ALPHA)$,ALPHA_ex,ALPHA",
        "555,666,555"
    )
    fun `should accept if it matches the include pattern and not the exclude pattern`(
        includePattern: String,
        excludePattern: String,
        input: String
    ) {

        currentProperties[includePatternPropertyName] = includePattern
        currentProperties[excludePatternPropertyName] = excludePattern

        assertThat(filter.accepts(input)).isTrue()
        assertThat(filter.rejects(input)).isFalse()

    }


    @ParameterizedTest
    @CsvSource(
        "abc.*,abd",
        "^(123|ALPHA)$,ALP",
        "555,25552"
    )
    fun `should accept if it doesn't match the include pattern while the exclude pattern is null`(
        includePattern: String,
        input: String
    ) {

        currentProperties[includePatternPropertyName] = includePattern
        currentProperties[excludePatternPropertyName] = null

        assertThat(filter.accepts(input)).isFalse()
        assertThat(filter.rejects(input)).isTrue()

    }


    @ParameterizedTest
    @CsvSource(
        "abc.*,xyz",
        "^(123|ALPHA)$,BOGUS",
        ".*123,25552"
    )
    fun `should accept if the include pattern is null and it doesn't match the exclude pattern`(
        excludePattern: String,
        input: String
    ) {

        currentProperties[includePatternPropertyName] = null
        currentProperties[excludePatternPropertyName] = excludePattern

        assertThat(filter.accepts(input)).isTrue()
        assertThat(filter.rejects(input)).isFalse()

    }


    @ParameterizedTest
    @CsvSource(
        "abc.*,abcd",
        "^(123|ALPHA)$,ALPHA",
        ".*123,2555123"
    )
    fun `should reject if the include pattern is null and it matches the exclude pattern`(
        excludePattern: String,
        input: String
    ) {

        currentProperties[includePatternPropertyName] = null
        currentProperties[excludePatternPropertyName] = excludePattern

        assertThat(filter.accepts(input)).isFalse()
        assertThat(filter.rejects(input)).isTrue()

    }


}

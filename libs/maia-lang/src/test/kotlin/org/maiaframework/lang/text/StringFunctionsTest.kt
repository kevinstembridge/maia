package org.maiaframework.lang.text

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class StringFunctionsTest {


    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("toTitleCaseSource")
    fun `toTitleCase converts input to title case`(input: String, expected: String) {

        val actual = StringFunctions.toTitleCase(input)
        assertThat(actual).isEqualTo(expected)

    }


    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("toSnakeCaseSource")
    fun `toSnakeCase converts input to snake case`(input: String, expected: String) {

        val actual = StringFunctions.toSnakeCase(input)
        assertThat(actual).isEqualTo(expected)

    }


    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("toKebabCaseSource")
    fun `toKebabCase converts input to kebab case`(input: String, expected: String) {

        val actual = StringFunctions.toKebabCase(input)
        assertThat(actual).isEqualTo(expected)

    }


    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("stripToNullSource")
    fun `stripToNull returns null for blank input and trimmed value otherwise`(input: String?, expected: String?) {

        val actual = StringFunctions.stripToNull(input)
        assertThat(actual).isEqualTo(expected)

    }


    @Test
    fun `stripToNull with mapper applies mapper to trimmed non-blank input`() {

        assertThat(StringFunctions.stripToNull("  42  ") { it.toInt() }).isEqualTo(42)

    }


    @Test
    fun `stripToNull with mapper returns null for blank input`() {

        assertThat(StringFunctions.stripToNull("   ") { it.toInt() }).isNull()

    }


    @Test
    fun `stripToNull with mapper returns null for null input`() {

        assertThat(StringFunctions.stripToNull(null) { it.toInt() }).isNull()

    }


    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("firstToUpperSource")
    fun `firstToUpper uppercases the first character`(input: String, expected: String) {

        val actual = StringFunctions.firstToUpper(input)
        assertThat(actual).isEqualTo(expected)

    }


    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("firstToLowerSource")
    fun `firstToLower lowercases the first character`(input: String, expected: String) {

        val actual = StringFunctions.firstToLower(input)
        assertThat(actual).isEqualTo(expected)

    }


    @ParameterizedTest(name = "input={0}, length={1} -> \"{2}\"")
    @MethodSource("padWithLeadingSpacesSource")
    fun `padWithLeadingSpaces pads input to the given length with leading spaces`(input: String?, paddedLength: Int, expected: String) {

        val actual = StringFunctions.padWithLeadingSpaces(input, paddedLength)
        assertThat(actual).isEqualTo(expected)

    }


    @ParameterizedTest(name = "input={0}, length={1} -> \"{2}\"")
    @MethodSource("padWithLeadingZeroesSource")
    fun `padWithLeadingZeroes pads integer to the given length with leading zeroes`(input: Int, paddedLength: Int, expected: String) {

        val actual = StringFunctions.padWithLeadingZeroes(input, paddedLength)
        assertThat(actual).isEqualTo(expected)

    }


    @Test
    fun `padWithLeadingZeroes throws when input exceeds padded length and overflow is not allowed`() {

        assertThatThrownBy { StringFunctions.padWithLeadingZeroes(1234, 3, dontAllowOverflow = true) }
            .isInstanceOf(IllegalArgumentException::class.java)

    }


    @ParameterizedTest(name = "input={0}, length={1} -> \"{2}\"")
    @MethodSource("padWithTrailingSpacesSource")
    fun `padWithTrailingSpaces pads input to the given length with trailing spaces`(input: String?, paddedLength: Int, expected: String) {

        val actual = StringFunctions.padWithTrailingSpaces(input, paddedLength)
        assertThat(actual).isEqualTo(expected)

    }


    @Test
    fun `padWithTrailingCharacters throws when input exceeds padded length and overflow is not allowed`() {

        assertThatThrownBy { StringFunctions.padWithTrailingCharacters("toolong", 3, '*', dontAllowOverflow = true) }
            .isInstanceOf(IllegalArgumentException::class.java)

    }


    companion object {


        @JvmStatic
        fun toTitleCaseSource(): Stream<Arguments> {

            return Stream.of(
                arguments("camelCase", "Camel Case"),
                arguments("PascalCase", "Pascal Case"),
                arguments("snake_case", "Snake Case"),
                arguments("kebab-case", "Kebab Case"),
                arguments("hello", "Hello"),
                arguments("mixedCase_with-separators", "Mixed Case With Separators")
            )

        }


        @JvmStatic
        fun toSnakeCaseSource(): Stream<Arguments> {

            return Stream.of(
                arguments("camelCase", "camel_case"),
                arguments("PascalCase", "pascal_case"),
                arguments("kebab-case", "kebab_case"),
                arguments("already_snake", "already_snake"),
                arguments("multiWordCamelCase", "multi_word_camel_case")
            )

        }


        @JvmStatic
        fun toKebabCaseSource(): Stream<Arguments> {

            return Stream.of(
                arguments("camelCase", "camel-case"),
                arguments("PascalCase", "pascal-case"),
                arguments("snake_case", "snake-case"),
                arguments("already-kebab", "already-kebab"),
                arguments("multiWordCamelCase", "multi-word-camel-case")
            )

        }


        @JvmStatic
        fun stripToNullSource(): Stream<Arguments> {

            return Stream.of(
                arguments(null, null),
                arguments("", null),
                arguments("   ", null),
                arguments("hello", "hello"),
                arguments("  hello  ", "hello")
            )

        }


        @JvmStatic
        fun firstToUpperSource(): Stream<Arguments> {

            return Stream.of(
                arguments("hello", "Hello"),
                arguments("Hello", "Hello"),
                arguments("hELLO", "HELLO"),
                arguments("a", "A")
            )

        }


        @JvmStatic
        fun firstToLowerSource(): Stream<Arguments> {

            return Stream.of(
                arguments("Hello", "hello"),
                arguments("hello", "hello"),
                arguments("HELLO", "hELLO"),
                arguments("A", "a")
            )

        }


        @JvmStatic
        fun padWithLeadingSpacesSource(): Stream<Arguments> {

            return Stream.of(
                arguments("hi", 5, "   hi"),
                arguments("hi", 2, "hi"),
                arguments("hi", 1, "hi"),
                arguments(null, 3, "   ")
            )

        }


        @JvmStatic
        fun padWithLeadingZeroesSource(): Stream<Arguments> {

            return Stream.of(
                arguments(5, 3, "005"),
                arguments(42, 3, "042"),
                arguments(123, 3, "123"),
                arguments(1234, 3, "1234")
            )

        }


        @JvmStatic
        fun padWithTrailingSpacesSource(): Stream<Arguments> {

            return Stream.of(
                arguments("hi", 5, "hi   "),
                arguments("hi", 2, "hi"),
                arguments("hi", 1, "hi"),
                arguments(null, 3, "   ")
            )

        }


    }


}

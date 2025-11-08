package org.maiaframework.lang.text

object StringFunctions {


    fun toSnakeCase(input: String): String {

        return toLowerCaseWithSeparator(input, "_")

    }


    fun toKebabCase(input: String): String {

        return toLowerCaseWithSeparator(input, "-")

    }


    private fun toLowerCaseWithSeparator(input: String, separator: String): String {

        val chars = input.toCharArray()
        val sb = StringBuilder()
        var firstChar = true

        for (ch in chars) {

            if (ch == '-') {
                sb.append(separator)
            } else if (Character.isUpperCase(ch)) {

                if (firstChar == false) {
                    sb.append(separator)
                }

                sb.append(Character.toLowerCase(ch))

            } else {
                sb.append(ch)
            }

            firstChar = false

        }

        return sb.toString()
    }


    fun stripToNull(input: String?): String? {

        if (input.isNullOrBlank()) {
            return null
        }

        return input.trim()

    }


    fun <T> stripToNull(input: String?, mapper: (String) -> T): T? {

        if (input.isNullOrBlank()) {
            return null
        }

        return mapper.invoke(input.trim())

    }


    fun firstToUpper(input: String): String {

        return transformFirstChar(input) { Character.toUpperCase(it) }

    }


    fun firstToLower(input: String): String {

        return transformFirstChar(input) { Character.toLowerCase(it) }

    }


    private fun transformFirstChar(value: String, func: (Char) -> Char): String {

        val chars = value.toCharArray()
        chars[0] = func.invoke(chars[0])
        return String(chars)

    }


    fun padWithLeadingSpaces(input: String?, paddedLength: Int): String {

        return padWithLeadingCharacters(input, paddedLength, ' ')

    }


    fun padWithLeadingZeroes(
            input: Int,
            paddedLength: Int,
            dontAllowOverflow: Boolean = false
    ): String {

        return padWithLeadingCharacters(
                input.toString(),
                paddedLength,
                '0',
                dontAllowOverflow
        )

    }


    fun padWithLeadingCharacters(
            input: String?,
            paddedLength: Int,
            padChar: Char,
            dontAllowOverflow: Boolean = false
    ): String {

        return padWithCharacters(
                input,
                paddedLength,
                padChar,
                leading = true,
                dontAllowOverflow
        )

    }


    fun padWithTrailingSpaces(input: Any?, paddedLength: Int): String {

        return padWithTrailingCharacters(input?.toString(), paddedLength, ' ')

    }


    fun padWithTrailingCharacters(
            input: String?,
            paddedLength: Int,
            padChar: Char,
            dontAllowOverflow: Boolean = false
    ): String {

        return padWithCharacters(input, paddedLength, padChar, leading = false, dontAllowOverflow)

    }


    private fun padWithCharacters(
            input: String?,
            paddedLength: Int,
            padChar: Char,
            leading: Boolean,
            dontAllowOverflow: Boolean
    ): String {

        if (input == null) {
            return padWithTrailingCharacters("", paddedLength, padChar)
        }

        if (input.length == paddedLength) {
            return input
        }

        val numberOfPadChars = getNumberOfPadChars(paddedLength, input, dontAllowOverflow)

        val padding = List(numberOfPadChars) { padChar }.joinToString("")

        return if (leading) {
            "$padding$input"
        } else {
            "$input$padding"
        }

    }


    private fun getNumberOfPadChars(paddedLength: Int, input: String, dontAllowOverflow: Boolean): Int {

        val numberOfPadChars = paddedLength - input.length

        if (numberOfPadChars < 0) {

            if (dontAllowOverflow) {
                throw IllegalArgumentException("The input string is longer than the padded length. input = [$input], paddedLength=$paddedLength")
            } else {
                return 0
            }

        } else {

            return numberOfPadChars

        }

    }


}

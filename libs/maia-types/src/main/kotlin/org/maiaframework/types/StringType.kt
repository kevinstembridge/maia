package org.maiaframework.types


import com.fasterxml.jackson.annotation.JsonValue
import org.maiaframework.lang.text.CaseHandling
import org.maiaframework.lang.text.StringFunctions


abstract class StringType<T : StringType<T>> protected constructor(
    value: String,
    caseHandling: CaseHandling = CaseHandling.ORIGINAL
): Comparable<T> {


    @JsonValue
    val value: String

    init {

        require(value.isNotBlank()) { "value must not be blank" }
        this.value = caseHandling.apply(value.trim())

    }


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as StringType<*>?

        return this.value == that!!.value

    }


    override fun hashCode(): Int {

        return this.value.hashCode()

    }


    override fun toString(): String {

        return this.value

    }


    override fun compareTo(other: T): Int {

        return this.value.compareTo(other.value)

    }


    fun firstToUpper(): String {

        return transformFirstChar { Character.toUpperCase(it) }

    }


    fun firstToLower(): String {

        return transformFirstChar { Character.toLowerCase(it) }

    }


    fun toSnakeCase(): String {

        return StringFunctions.toSnakeCase(this.value)

    }


    fun toKebabCase(): String {

        return StringFunctions.toKebabCase(this.value)

    }


    private fun transformFirstChar(func: (Char) -> Char): String {

        val chars = this.value.toCharArray()
        chars[0] = func.invoke(chars[0])
        return String(chars)

    }


}

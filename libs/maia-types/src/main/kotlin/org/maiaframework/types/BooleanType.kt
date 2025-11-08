package org.maiaframework.types


import com.fasterxml.jackson.annotation.JsonValue

abstract class BooleanType<T: BooleanType<T>> protected constructor(@get:JsonValue val value: Boolean) : Comparable<T> {


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as BooleanType<*>?

        return this.value == that!!.value

    }


    override fun hashCode(): Int {

        return java.lang.Boolean.hashCode(this.value)

    }


    override fun toString(): String {

        return this.value.toString()

    }


    override fun compareTo(other: T): Int {

        return java.lang.Boolean.compare(this.value, other.value)

    }


}

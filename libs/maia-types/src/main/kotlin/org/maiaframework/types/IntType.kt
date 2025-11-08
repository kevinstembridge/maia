package org.maiaframework.types


import com.fasterxml.jackson.annotation.JsonValue

abstract class IntType<T : IntType<T>> protected constructor(@get:JsonValue val value: Int) : Comparable<T> {


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as IntType<*>?

        return this.value == that!!.value

    }


    override fun hashCode(): Int {

        return this.value

    }


    override fun toString(): String {

        return this.value.toString()

    }


    override fun compareTo(other: T): Int {

        return Integer.compare(this.value, other.value)

    }


}

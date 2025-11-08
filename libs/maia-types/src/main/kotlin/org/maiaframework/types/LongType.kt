package org.maiaframework.types


import com.fasterxml.jackson.annotation.JsonValue

import java.lang.reflect.InvocationTargetException

abstract class LongType<T : LongType<T>> protected constructor(@get:JsonValue val value: Long) : Comparable<T> {


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as LongType<*>?

        return this.value == that!!.value

    }


    override fun hashCode(): Int {

        return java.lang.Long.hashCode(this.value)

    }


    override fun toString(): String {

        return this.value.toString()

    }


    override fun compareTo(other: T): Int {

        return java.lang.Long.compare(this.value, other.value)

    }


    fun isLessThan(other: T): Boolean {

        return java.lang.Long.compare(value, other.value) < 0

    }


    fun isGreaterThan(other: T): Boolean {

        return java.lang.Long.compare(value, other.value) > 0

    }


    operator fun plus(other: T): T {

        return plus(other.value)

    }


    operator fun plus(increment: Long): T {

        val sum = value + increment

        try {
            val constructor = javaClass.getConstructor(Long::class.javaPrimitiveType)

            return constructor.newInstance(sum) as T
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Should never happen")
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Should never happen")
        } catch (e: InstantiationException) {
            throw RuntimeException("Should never happen")
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Should never happen")
        }

    }


    fun subtract(other: T): T {

        return subtract(other.value)

    }


    fun subtract(decrement: Long): T {

        val newValue = value - decrement

        try {
            val constructor = javaClass.getConstructor(Long::class.javaPrimitiveType)

            return constructor.newInstance(newValue) as T
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Should never happen")
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Should never happen")
        } catch (e: InstantiationException) {
            throw RuntimeException("Should never happen")
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Should never happen")
        }

    }


}

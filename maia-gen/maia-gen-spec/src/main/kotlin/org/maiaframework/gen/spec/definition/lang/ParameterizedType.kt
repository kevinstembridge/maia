package org.maiaframework.gen.spec.definition.lang

import java.util.*


class ParameterizedType private constructor(
    fqcn: Fqcn,
    override val simpleTypeUnderlyingFieldType: FieldType?,
    val parameters: List<NonPrimitiveType>
) : NonPrimitiveType(
    fqcn,
    initUnqualifiedToString(fqcn, parameters)
) {

    override val isEnum = false

    override val isString = this.fqcn == Fqcn.STRING

    val isList: Boolean = this.fqcn == Fqcn.LIST

    val isSet: Boolean = this.fqcn == Fqcn.SET

    override val isSetOfEnums = isSet && firstParameter?.isEnum ?: false

    val isMap: Boolean = this.fqcn == Fqcn.MAP

    val isInstant: Boolean = this.fqcn == Fqcn.INSTANT

    val isLocalDate: Boolean = this.fqcn == Fqcn.LOCAL_DATE

    val isPeriod: Boolean = this.fqcn == Fqcn.PERIOD

    val firstParameter: NonPrimitiveType
        get() = this.parameters
            .firstOrNull() ?: throw RuntimeException("Expected at least one parameter type but found none: $this")

    val secondParameter: NonPrimitiveType
        get() = this.parameters
            .drop(1)
            .firstOrNull() ?: throw RuntimeException("Expected at least two parameters type but found less than two: $this")


    constructor(
        fqcn: Fqcn,
        vararg parameters: NonPrimitiveType
    ) : this(
        fqcn,
        null,
        *parameters
    )


    constructor(
        fqcn: Fqcn,
        simpleTypeUnderlyingFieldType: FieldType?,
        vararg parameters: NonPrimitiveType
    ) : this(
        fqcn,
        simpleTypeUnderlyingFieldType,
        parameters.toList()
    )


    override fun asParameterTo(outerParameterizedType: Fqcn): ParameterizedType {

        return ParameterizedType(outerParameterizedType, this.simpleTypeUnderlyingFieldType, this)

    }


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as ParameterizedType?
        return this.fqcn == that!!.fqcn && this.parameters == that.parameters
    }


    override fun hashCode(): Int {

        return Objects.hash(this.fqcn, this.parameters)

    }


    override fun toString(): String {

        return if (this.parameters.isEmpty()) {
            this.fqcn.toString()
        } else {
            this.fqcn.toString() + this.parameters.map { it.toString() }.joinToString(prefix = "<", separator = ", ", postfix = ">")
        }

    }


    override fun withPrefix(prefix: String): ParameterizedType {

        return ParameterizedType(
            this.fqcn.withPrefix(prefix),
            this.simpleTypeUnderlyingFieldType,
            this.parameters
        )

    }


    companion object {


        private fun initUnqualifiedToString(
            fqcn: Fqcn,
            parameters: List<NonPrimitiveType>
        ): String {

            return if (parameters.isEmpty()) {
                fqcn.unqualifiedToString
            } else {
                fqcn.unqualifiedToString + parameters.map { it.unqualifiedToString }.joinToString(prefix = "<", separator = ", ", postfix = ">")
            }

        }


    }


}

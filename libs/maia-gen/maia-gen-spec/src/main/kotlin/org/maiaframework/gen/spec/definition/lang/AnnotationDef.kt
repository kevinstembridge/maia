package org.maiaframework.gen.spec.definition.lang

import java.util.Objects

class AnnotationDef(
    val fqcn: Fqcn,
    private val value: (() -> String)? = null,
    private val attributes: Map<String, String> = emptyMap(),
    private val usageSite: AnnotationUsageSite? = null
) : Comparable<AnnotationDef> {


    val unqualifiedToString = this.fqcn.uqcn.toString()


    constructor(
        fqcn: Fqcn,
        value: String,
        usageSite: AnnotationUsageSite? = null
    ) : this(
        fqcn,
        { "\"" + value + "\"" },
        emptyMap<String, String>(),
        usageSite
    )


    fun toStringInKotlin(usageSite: AnnotationUsageSite? = null): String {

        val usageSiteToUse = usageSite ?: this.usageSite

        val usageSiteText = if (usageSiteToUse != null) {
            "${usageSiteToUse.name}:"
        } else {
            ""
        }

        return "@$usageSiteText${this.fqcn.uqcn}${formattedAttributes()}"

    }


    override fun compareTo(other: AnnotationDef): Int {

        return this.fqcn.compareTo(other.fqcn)

    }


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as AnnotationDef?
        return this.fqcn == that!!.fqcn &&
                this.value == that.value &&
                this.attributes == that.attributes

    }


    override fun hashCode(): Int {

        return Objects.hash(fqcn, value, attributes)

    }


    private fun formattedAttributes(): String {

        if (parenthesesAreRequired() == false) {
            return ""
        }

        val sb = StringBuilder()
        sb.append("(")

        this.value?.let { v -> sb.append(v.invoke()) }

        sb.append(this.attributes.entries.map { entry -> entry.key + " = " + entry.value }.joinToString(", "))

        sb.append(")")

        return sb.toString()

    }


    private fun parenthesesAreRequired(): Boolean {

        return this.value != null || this.attributes.isNotEmpty()

    }


}

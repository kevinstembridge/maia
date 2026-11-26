package org.maiaframework.gen.spec.definition

sealed class EsDocMappingType(
    val value: String
) {


    override fun toString(): String {

        return this.value

    }


}


class BooleanEsDocMappingType internal constructor() : EsDocMappingType("boolean_")


class DateEsDocMappingType internal constructor() : EsDocMappingType("date")


class DoubleEsDocMappingType internal constructor() : EsDocMappingType("double_")


class KeywordEsDocMappingType internal constructor() : EsDocMappingType("keyword")


class LongEsDocMappingType internal constructor() : EsDocMappingType("long_")


class ObjectEsDocMappingType internal constructor() : EsDocMappingType("object")


/**
 * An Entity ref held in the index as an object: `id` (keyword), `displayName` (text with a `raw` keyword sub-field,
 * so it can be filtered and sorted) and, when the Entity declares one, `shortcode` (keyword). See ADR-0007.
 */
class EntityRefEsDocMappingType internal constructor(
    val hasShortcode: Boolean
) : EsDocMappingType("object")


/**
 * `scaled_float`: a float stored as a signed 64-bit long equal to `value * scalingFactor`. Parameterised, so each
 * instance carries its own scaling factor (10^scale for a BigDecimal field).
 */
class ScaledFloatEsDocMappingType internal constructor(
    val scalingFactor: Long
) : EsDocMappingType("scaledFloat") {


    override fun equals(other: Any?): Boolean {
        return other is ScaledFloatEsDocMappingType && scalingFactor == other.scalingFactor
    }


    override fun hashCode(): Int {
        return scalingFactor.hashCode()
    }


}


class SearchAsYouTypeEsDocMappingType internal constructor() : EsDocMappingType("searchAsYouType")


class TextEsDocMappingType internal constructor() : EsDocMappingType("text")


class TextAndKeywordEsDocMappingType internal constructor() : EsDocMappingType("huh")


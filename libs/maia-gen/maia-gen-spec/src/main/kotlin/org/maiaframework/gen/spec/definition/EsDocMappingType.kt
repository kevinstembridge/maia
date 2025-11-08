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


class DoubleEsDocMappingType internal constructor() : EsDocMappingType("double")


class KeywordEsDocMappingType internal constructor() : EsDocMappingType("keyword")


class LongEsDocMappingType internal constructor() : EsDocMappingType("long_")


class ObjectEsDocMappingType internal constructor() : EsDocMappingType("object")


class SearchAsYouTypeEsDocMappingType internal constructor() : EsDocMappingType("search_as_you_type")


class TextEsDocMappingType internal constructor() : EsDocMappingType("text")


class TextAndKeywordEsDocMappingType internal constructor() : EsDocMappingType("huh")


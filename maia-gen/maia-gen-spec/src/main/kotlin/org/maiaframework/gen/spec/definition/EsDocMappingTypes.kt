package org.maiaframework.gen.spec.definition


object EsDocMappingTypes {


    val boolean = BooleanEsDocMappingType()


    val date = DateEsDocMappingType()


    val double = DoubleEsDocMappingType()


    val keyword = KeywordEsDocMappingType()


    val long = LongEsDocMappingType()


    val `object` = ObjectEsDocMappingType()


    /** scaling factor is 10^[scale], so values are exact to [scale] decimal places */
    fun scaledFloat(scale: Int): ScaledFloatEsDocMappingType {

        var factor = 1L
        repeat(scale) { factor *= 10L }
        return ScaledFloatEsDocMappingType(factor)

    }


    val searchAsYouType = SearchAsYouTypeEsDocMappingType()


    val text = TextEsDocMappingType()


    val textAndKeyword = TextAndKeywordEsDocMappingType()


}

package org.maiaframework.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import java.io.IOException


class StringTrimmingDeserializer : StringDeserializer() {


    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): String? {

        val value = super.deserialize(parser, context)
        val trimmedValue = value?.trim()

        return if (trimmedValue.isNullOrBlank()) null else trimmedValue

    }


}

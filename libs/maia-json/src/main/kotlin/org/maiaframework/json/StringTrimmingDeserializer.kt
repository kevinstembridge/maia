package org.maiaframework.json

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.jdk.StringDeserializer


class StringTrimmingDeserializer : StringDeserializer() {


    override fun deserialize(parser: JsonParser, context: DeserializationContext): String? {

        val value = super.deserialize(parser, context)
        val trimmedValue = value?.trim()

        return if (trimmedValue.isNullOrBlank()) null else trimmedValue

    }


}

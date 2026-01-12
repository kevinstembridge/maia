package org.maiaframework.json

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode
import java.time.LocalDate

object JsonNodeExtensions {


    fun JsonNode.getObjectNode(fieldName: String): ObjectNode {

        return this.getObjectNodeOrNull(fieldName)
                ?: throw IllegalArgumentException("Expected to find an object field named $fieldName in ${this}")

    }


    fun JsonNode.getObjectNodeOrNull(fieldName: String): ObjectNode? {

        return this.getNodeOrNull(fieldName)?.let { v ->
            if (v.isObject == false) {
                throw IllegalArgumentException("Expected " + fieldName + " to be an ObjectNode but found " + v.nodeType)
            } else {
                v as ObjectNode
            }
        }

    }


    fun JsonNode.getArrayNode(fieldName: String): ArrayNode {

        return this.getArrayNodeOrNull(fieldName)
                ?: throw IllegalArgumentException("Expected to find an array field named $fieldName in ${this}")

    }


    fun JsonNode.getArrayNodeOrNull(fieldName: String): ArrayNode? {

        return this.getNodeOrNull(fieldName)?.let { v ->
            if (v.isArray) {
                return v as ArrayNode
            } else {
                throw IllegalArgumentException("Expected " + fieldName + " to be an ArrayNode but found " + v.nodeType)
            }
        }

    }


    fun JsonNode.getInt(fieldName: String): Int {

        return this.getIntOrNull(fieldName) ?: throw IllegalArgumentException("Expected to find an int field named $fieldName")

    }


    fun JsonNode.getIntOrNull(fieldName: String): Int? {

        return this.getNodeOrNull(fieldName)?.let { jsonNode ->
            if (jsonNode.isInt)
                jsonNode.intValue()
            else
                throw IllegalArgumentException("Expected $fieldName to be an integer node but found $jsonNode")
        }

    }


    fun JsonNode.getDouble(fieldName: String): Double {

        return this.getDoubleOrNull(fieldName) ?: throw IllegalArgumentException("Expected to find a numeric field named $fieldName")

    }


    fun JsonNode.getDoubleOrNull(fieldName: String): Double? {

        return getNumberOrNull(fieldName)?.toDouble()

    }


    fun JsonNode.getNumber(fieldName: String): Number {

        return this.getNumberOrNull(fieldName) ?: throw IllegalArgumentException("Expected to find a numeric field named $fieldName")

    }


    fun JsonNode.getNumberOrNull(fieldName: String): Number? {

        return this.getNodeOrNull(fieldName)?.let { jsonNode ->
            if (jsonNode.isNumber)
                jsonNode.numberValue()
            else
                throw IllegalArgumentException("Expected $fieldName to be numeric but found $jsonNode")
        }

    }


    fun JsonNode.getString(fieldName: String): String {

        return this.getStringOrNull(fieldName) ?: throw IllegalArgumentException("Expected to find a string field named $fieldName")

    }


    fun JsonNode.getStringOrNull(fieldName: String): String? {

        return this.getNodeOrNull(fieldName)?.let { jsonNode ->
            if (jsonNode.isTextual) {
                jsonNode.textValue()
            } else {
                throw IllegalArgumentException("Expected $fieldName to be a textual node but found $jsonNode")
            }
        }

    }


    fun JsonNode.getLocalDate(fieldName: String): LocalDate {

        val rawDate = this.getStringOrNull(fieldName) ?: throw IllegalArgumentException("Expected to find a localDate field named $fieldName")
        return LocalDate.parse(rawDate)

    }


    fun JsonNode.getNodeOrNull(fieldName: String): JsonNode? {

        return get(fieldName)

    }


}

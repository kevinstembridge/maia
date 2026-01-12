package org.maiaframework.domain.search.mongo

import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode
import org.maiaframework.types.StringType
import org.maiaframework.json.JsonNodeExtensions.getArrayNodeOrNull
import org.maiaframework.json.JsonNodeExtensions.getInt
import org.maiaframework.json.JsonNodeExtensions.getObjectNodeOrNull
import org.maiaframework.domain.search.InvalidSearchJsonException
import org.maiaframework.domain.search.SearchFieldConverter
import org.maiaframework.domain.search.SearchFieldNameConverter
import org.bson.Document
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.io.IOException
import java.util.*

class SearchRequestParser(
    private val searchRequestFieldNameConverter: SearchFieldNameConverter,
    private val searchFieldConverter: SearchFieldConverter
) {


    fun parseSearchJson(json: String): MongoPageableSearchRequest {

        if (json.isBlank()) {
            return MongoPageableSearchRequest(Document(), null)
        }

        val rootNode = parseRaw(json)

        val queryDocument = parseForQueryDocument(rootNode)
        val pageable = parseForPageable(rootNode)

        return MongoPageableSearchRequest(queryDocument, pageable)

    }


    private fun parseForQueryDocument(rootNode: ObjectNode): Document {

        val queryDocument = Document()

        rootNode.get("query")?.let { queryNode -> processObjectNode(queryNode as ObjectNode, queryDocument, null) }

        return queryDocument

    }


    private fun processObjectNode(
        objectNode: ObjectNode,
        document: Document,
        collectionFieldPath: CollectionFieldPath?
    ) {

        objectNode.fields().forEachRemaining { nodeEntry ->

            val fieldName = nodeEntry.key
            val childNode = nodeEntry.value

            if (childNode.isValueNode) {

                processValueNode(fieldName, collectionFieldPath, childNode, document)

            } else if (childNode.isObject) {

                val childDocument = Document()

                if (fieldName.startsWith("$")) {

                    document[fieldName] = childDocument
                    processObjectNode(childNode as ObjectNode, childDocument, collectionFieldPath)

                } else {

                    val collFieldPath = this.searchRequestFieldNameConverter.convertFieldName(fieldName)
                    document[collFieldPath] = childDocument
                    processObjectNode(childNode as ObjectNode, childDocument, CollectionFieldPath(collFieldPath))

                }

            } else if (childNode.isArray) {

                processArrayNode(fieldName, childNode as ArrayNode, document, collectionFieldPath)

            } else {
                throw RuntimeException("Wasn't expecting this!")
            }

        }

    }


    private fun processArrayNode(
        fieldName: String,
        arrayNode: ArrayNode,
        parentDocument: Document,
        collectionFieldPath: CollectionFieldPath?
    ) {

        if (fieldName.startsWith("$")) {

            validateOperator(fieldName)

            val children = ArrayList<Any>()
            parentDocument[fieldName] = children

            arrayNode.forEach { childNode ->

                if (childNode.isValueNode) {

                    val value = rawValueFrom(childNode)
                    children.add(value)

                } else if (childNode.isObject) {

                    val childDocument = Document()
                    children.add(childDocument)
                    processObjectNode(childNode as ObjectNode, childDocument, collectionFieldPath)

                } else if (childNode.isArray) {

                    // TODO

                } else {

                    throw IllegalStateException("Didn't see this coming!")

                }

            }

        } else {

            // TODO

        }

    }


    private fun processValueNode(
        fieldName: String,
        collectionFieldPath: CollectionFieldPath?,
        jsonNode: JsonNode,
        document: Document
    ) {

        if (fieldName.startsWith("$")) {

            validateOperator(fieldName)
            val collFieldPath =
                collectionFieldPath ?: throw IllegalStateException("Expected to find a collectionFieldPath associated with this node: $jsonNode")
            val rawValue = rawValueFrom(jsonNode)
            val convertedValue = this.searchFieldConverter.convertValue(collFieldPath.value, rawValue)
            document[fieldName] = convertedValue

        } else {

            if (collectionFieldPath != null) {
                throw IllegalStateException("Not expecting a collectionFieldPath to be provided here. $collectionFieldPath")
            }

            val collFieldPath = this.searchRequestFieldNameConverter.convertFieldName(fieldName)
            val rawValue = rawValueFrom(jsonNode)
            val convertedValue = this.searchFieldConverter.convertValue(collFieldPath, rawValue)
            document[collFieldPath] = convertedValue

        }

    }


    private fun validateOperator(operatorName: String) {

        if (VALID_OPERATOR_NAMES.contains(operatorName) == false) {
            throw IllegalArgumentException("Invalid operator: [$operatorName]")
        }

    }


    private fun parseRaw(json: String): ObjectNode {

        try {
            val jsonNode = OBJECT_MAPPER.readTree(json)

            return if (jsonNode.isObject) {
                jsonNode as ObjectNode
            } else {
                throw IllegalArgumentException("Expected an object node but found: $jsonNode")
            }
        } catch (e: IOException) {
            throw InvalidSearchJsonException(e)
        }

    }


    private fun rawValueFrom(jsonNode: JsonNode): Any {

        if (jsonNode.isValueNode == false) {
            throw IllegalArgumentException("Expected a value node but found: $jsonNode")
        }

        if (jsonNode.isTextual) {
            return jsonNode.textValue()
        }

        if (jsonNode.isDouble) {
            return jsonNode.doubleValue()
        }

        if (jsonNode.isFloat) {
            return jsonNode.floatValue()
        }

        if (jsonNode.isInt) {
            return jsonNode.intValue()
        }

        if (jsonNode.isLong) {
            return jsonNode.longValue()
        }

        if (jsonNode.isBoolean) {
            return jsonNode.booleanValue()
        }

        throw IllegalArgumentException("Unknown node type: " + jsonNode.nodeType)

    }


    private class CollectionFieldPath(value: String) : StringType<CollectionFieldPath>(value)


    private fun parseForPageable(rootNode: ObjectNode): Pageable? {

        return rootNode.getObjectNodeOrNull("pageable")?.let { pageableNode ->

            val pageNumber = pageableNode.getInt("page")
            val pageSize = pageableNode.getInt("size")
            val sort = buildSort(pageableNode)

            PageRequest.of(pageNumber, pageSize, sort)

        }

    }

    private fun buildSort(pageableNode: ObjectNode): Sort {

        val sortNodeOpt = pageableNode.getArrayNodeOrNull("sort")

        return sortNodeOpt?.let { sortNode ->

            val orders = ArrayList<Sort.Order>()

            sortNode.forEach { propertyNode ->
                if (propertyNode.isObject) {

                    propertyNode.fields().forEachRemaining { s ->
                        val propertyName = s.key
                        val rawDirection = s.value.textValue()
                        val direction = Sort.Direction.fromString(rawDirection)
                        orders.add(Sort.Order(direction, propertyName))

                    }

                }

            }

            Sort.by(orders)

        } ?: Sort.unsorted()

    }

    companion object {

        private val OBJECT_MAPPER = ObjectMapper()

        private val VALID_OPERATOR_NAMES = setOf(
            "\$eq",
            "\$gt",
            "\$gte",
            "\$in",
            "\$lt",
            "\$lte",
            "\$ne",
            "\$nin",
            "\$and",
            "\$not",
            "\$nor",
            "\$or",
            "\$exists",
            "\$expr",
            "\$mod",
            "\$options",
            "\$regex",
            "\$text",
            "\$where"
        )

    }


}

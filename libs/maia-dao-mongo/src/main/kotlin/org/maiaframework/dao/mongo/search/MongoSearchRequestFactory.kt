package org.maiaframework.dao.mongo.search

import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.maiaframework.dao.mongo.MongoLookupDescriptor
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.domain.search.FilterModelItem
import org.maiaframework.domain.search.SearchFieldConverter
import org.maiaframework.domain.search.SearchFieldNameConverter
import org.maiaframework.domain.search.SearchModel
import org.maiaframework.domain.search.mongo.MongoSearchRequest
import org.maiaframework.domain.types.TypeDiscriminator
import org.maiaframework.json.JsonNodeExtensions.getLocalDate
import org.maiaframework.json.JsonNodeExtensions.getNumber
import org.maiaframework.json.JsonNodeExtensions.getString
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.util.Locale
import java.util.SortedSet
import java.util.regex.Pattern

abstract class MongoSearchRequestFactory(
    private val searchFieldConverter: SearchFieldConverter,
    private val searchFieldNameConverter: SearchFieldNameConverter,
    private val jsonMapper: JsonMapper
) {


    abstract val typeDiscriminators: SortedSet<TypeDiscriminator>

    abstract val caseInsensitiveQueryFieldNames: Set<String>


    protected fun convert(
            searchModel: SearchModel,
            projectedFieldNames: Set<String>
    ): MongoSearchRequest {

        val query = buildFilterModelDocument(searchModel, emptySet())
        val projection = buildProjection(projectedFieldNames)
        val offset = searchModel.startRow
        val limit = determineLimit(searchModel, offset)
        val sort = buildSortBson(searchModel)

        return MongoSearchRequest(query, projection, offset, limit, sort)

    }


    protected fun convert(
            searchModel: AgGridSearchModel,
            projectedFieldNames: Set<String>
    ): MongoSearchRequest {

        TODO()

    }


    protected fun convertToAggregatePipeline(
            searchRawJson: String,
            rootDtoFieldNames: Set<String>,
            projectedFieldNames: Set<String>,
            lookupDescriptors: List<MongoLookupDescriptor>
    ): MongoAggregationSearchRequest {

        val searchModel = this.jsonMapper.readValue(searchRawJson, SearchModel::class.java)

        return convertToAggregatePipeline(
                searchModel,
                rootDtoFieldNames,
                projectedFieldNames,
                lookupDescriptors)

    }


    protected fun convertToAggregatePipeline(
            searchModel: SearchModel,
            rootDtoFieldNames: Set<String>,
            projectedFieldNames: Set<String>,
            lookupDescriptors: List<MongoLookupDescriptor>
    ): MongoAggregationSearchRequest {


        val appliedLookupDescriptors = mutableSetOf<MongoLookupDescriptor>()

        // We always start with a match on just the rootDto fields in order to reduce the number of
        // matching documents before we start doing lookups.
        val rootDtoFieldsMatchDoc = buildRootDtoFieldsMatchDoc(searchModel, rootDtoFieldNames)

        val fieldNamesInFilterModel = getFieldNamesFromFilterModel(searchModel)
        val fieldNamesInSortModel = getFieldNamesFromSortModel(searchModel)

        // If any lookup fields are included in the filterModel, we must apply the lookup before
        // we do the sort, offset and limit
        val lookupBsonsInFilterModel: List<Bson> = lookupDescriptors.mapNotNull { lookupDescriptor ->

            val includedInFilter: Boolean = lookupDescriptor.dtoFieldsFromLookupEntity.any { foreignField -> fieldNamesInFilterModel.contains(foreignField.dtoFieldName) }

            if (includedInFilter) {
                appliedLookupDescriptors.add(lookupDescriptor)
                buildLookupBson(lookupDescriptor, searchModel, includeMatchDocument = true)
            } else {
                null
            }

        }.flatten()


        // If any lookup fields are included in the sortModel but have not been applied yet (by virtue of being
        // in the filterModel), apply the lookup.
        val lookupBsonsForSortFields = lookupDescriptors.mapNotNull { lookupDescriptor ->

            val includedInSortButNotAlreadyApplied: Boolean =
                    appliedLookupDescriptors.contains(lookupDescriptor) == false
                    && lookupDescriptor.dtoFieldsFromLookupEntity.any { foreignField -> fieldNamesInSortModel.contains(foreignField.dtoFieldName) }

            if (includedInSortButNotAlreadyApplied) {
                appliedLookupDescriptors.add(lookupDescriptor)
                buildLookupBson(lookupDescriptor, searchModel, includeMatchDocument = false)
            } else {
                null
            }

        }.flatten()

        val sortDoc = buildSortBson(searchModel)?.let { Document("\$sort", it) }
        val offset = searchModel.startRow
        val skipDoc = Document("\$skip", offset)
        val limit = determineLimit(searchModel, offset)
        val limitDoc = limit?.let { Document("\$limit", limit) }

        // If any lookups were not involved in the filter or sort, apply them now.
        val lookupBsonsForFieldsNotAlreadyInSortOrFilter = lookupDescriptors.mapNotNull { lookupDescriptor ->

            val notAlreadyApplied: Boolean = appliedLookupDescriptors.contains(lookupDescriptor) == false

            if (notAlreadyApplied) {
                buildLookupBson(lookupDescriptor, searchModel, includeMatchDocument = false)
            } else {
                null
            }

        }.flatten()

        val projection = buildProjection(projectedFieldNames)
        val projectDoc = Document("\$project", projection)

        val searchAggregations = mutableListOf<Bson?>()
        searchAggregations.add(rootDtoFieldsMatchDoc)
        searchAggregations.addAll(lookupBsonsInFilterModel)
        searchAggregations.addAll(lookupBsonsForSortFields)
        sortDoc?.let { searchAggregations.add(it) }
        searchAggregations.add(skipDoc)
        limitDoc?.let { searchAggregations.add(it) }
        searchAggregations.addAll(lookupBsonsForFieldsNotAlreadyInSortOrFilter)
        searchAggregations.add(projectDoc)

        val countAggregations = mutableListOf<Bson?>()
        countAggregations.add(rootDtoFieldsMatchDoc)
        countAggregations.addAll(lookupBsonsInFilterModel)

        return MongoAggregationSearchRequest(
            searchAggregations.filterNotNull(),
            countAggregations.filterNotNull(),
            offset,
            limit
        )

    }


    private fun buildRootDtoFieldsMatchDoc(searchModel: SearchModel, rootDtoFieldNames: Set<String>): Document? {

        val rootDtoFieldsFilterDoc = buildFilterModelDocument(searchModel, rootDtoFieldNames)

        return if (rootDtoFieldsFilterDoc.isEmpty()) {
            null
        } else {
            Document("\$match", rootDtoFieldsFilterDoc)
        }

    }


    private fun getFieldNamesFromFilterModel(searchModel: SearchModel): Set<String> {

        return searchModel.filterModel.map { it.fieldPath }.toSet()

    }


    private fun getFieldNamesFromSortModel(searchModel: SearchModel): Set<String> {

        val sortModelArrayNode = searchModel.sortModel

        if (sortModelArrayNode.isEmpty()) {
            return emptySet()
        }

        val fieldNames = mutableSetOf<String>()

        sortModelArrayNode.let { sortModelNode -> sortModelNode.forEach { v ->

            val classFieldName = v.fieldPath
            fieldNames.add(classFieldName)

        } }

        return fieldNames

    }


    private fun determineLimit(rootNode: SearchModel, offset: Int): Int? {

        val endRow = rootNode.endRow

        return if (endRow == null) {
            null
        } else {
            endRow - offset
        }

    }


    private fun buildLookupBson(
            lookupDescriptor: MongoLookupDescriptor,
            searchModel: SearchModel,
            includeMatchDocument: Boolean
    ): List<Bson> {

        val matchDocOrNull = if (includeMatchDocument) {

            val includedFields = lookupDescriptor.dtoFieldsFromLookupEntity.map { it.dtoFieldName }.toSet()
            val filterModelDocument = buildFilterModelDocument(searchModel, includedFields)

            if (filterModelDocument.isEmpty()) {
                null
            } else {
                Document("\$match", filterModelDocument)
            }

        } else {
            null
        }

        val addFieldsDoc = Document()

        lookupDescriptor.dtoFieldsFromLookupEntity.forEach { field ->
            addFieldsDoc[field.dtoFieldName] =
                Document("\$arrayElemAt", listOf("\$someLookup.${field.foreignEntityFieldName}", 0))
        }

        return listOfNotNull(
            Document(
                "\$lookup",
                Document()
                    .append("from", lookupDescriptor.fromCollection.value)
                    .append("localField", lookupDescriptor.localField)
                    .append("foreignField", lookupDescriptor.foreignField)
                    .append("as", "someLookup")
            ),
            Document(
                "\$addFields",
                addFieldsDoc
            ),
            Document(
                "\$project",
                Document("someLookup", 0)
            ),
            matchDocOrNull
        )

    }

    /*

[
  {
    $lookup: {
      from: 'verySimple',
      localField: 'verySimpleId',
      foreignField: '_id',
      as: 'fromItems'
    }
  },
  {
    $addFields: {
      someForeignString: {
        $arrayElemAt: [
          "$fromItems.someString",
          0
        ]
      }
    }
  },
  {
    $project: {
      fromItems: 0
    }
  }
]
     */


    private fun buildFilterModelDocument(searchModel: SearchModel, includedFields: Set<String>): Document {

        val topLevelDocuments = mutableListOf<Document>()

        addAnyTypeDiscriminatorsTo(topLevelDocuments, this.typeDiscriminators)

        applyFilterModel(searchModel, includedFields, topLevelDocuments)

        return when {
            topLevelDocuments.isEmpty() -> Document()
            topLevelDocuments.size == 1 -> topLevelDocuments.first()
            else -> Document("\$and", topLevelDocuments)
        }

    }


    private fun applyFilterModel(
            searchModel: SearchModel,
            includedFields: Set<String>,
            documents: MutableList<Document>
    ) {

        searchModel.filterModel.forEach { filterModelItem ->

            val classFieldName = filterModelItem.fieldPath

            if (includedFields.isEmpty() || includedFields.contains(classFieldName)) {

//                val fieldNode = mutableEntry.value as ObjectNode

//                if (fieldNode.has("operator")) {
//                    val document = buildMultiConditionFilterDocument(fieldNode, classFieldName)
//                    documents.add(document)
//                } else {
                    val document = filterModelConditionToBson(classFieldName, filterModelItem)
                    documents.add(document)
//                }

            }

        }

    }


    private fun addAnyTypeDiscriminatorsTo(topLevelDocuments: MutableList<Document>, theseTypeDiscriminators: SortedSet<TypeDiscriminator>) {

        val typeDiscriminatorDocument: Document? = addTypeDiscriminators(theseTypeDiscriminators)

        if (typeDiscriminatorDocument != null) {
            topLevelDocuments.add(typeDiscriminatorDocument)
        }

    }


    private fun addTypeDiscriminators(typeDiscriminators: SortedSet<TypeDiscriminator>): Document? {

        if (typeDiscriminators.isEmpty()) {
            return null
        }

        if (typeDiscriminators.size == 1) {

            return Document("TYP", typeDiscriminators.first().value)

        } else {

            val subDocument = Document()
            val value: List<String> = typeDiscriminators.map { it.value }
            subDocument.append("\$in", value)
            return Document("TYP", subDocument)

        }

    }


    private fun buildMultiConditionFilterDocument(
        fieldNode: ObjectNode,
        fieldName: String
    ): Document {

        val operator = fieldNode.getString("operator").lowercase(Locale.getDefault())

        when (operator) {
            "or" -> {

                val conditionDocuments = buildConditionDocumentsFor(fieldName, fieldNode)
                return Document("\$or", conditionDocuments)

            }
            "and" -> {

                val conditionDocuments = buildConditionDocumentsFor(fieldName, fieldNode)
                return Document("\$and", conditionDocuments)

            }
            else -> throw IllegalArgumentException("Unknown conditional operator [$operator]")

        }

    }


    private fun buildConditionDocumentsFor(fieldName: String, fieldNode: ObjectNode): MutableList<Document> {

        val conditionDocuments = mutableListOf<Document>()

        fieldNode.properties().forEach { conditionEntry: MutableMap.MutableEntry<String, JsonNode> ->

            val conditionNameOrOperator = conditionEntry.key

            if (conditionNameOrOperator != "operator") {

                val conditionFilterModelNode = conditionEntry.value as ObjectNode
                val conditionDocument = filterModelConditionToBson(fieldName, conditionFilterModelNode)

                conditionDocuments.add(conditionDocument)

            }

        }

        return conditionDocuments

    }


    private fun filterModelConditionToBson(
            classFieldName: String,
            fieldFilterModel: ObjectNode
    ): Document {

        val queryDocument = Document()
        val convertedFieldName = this.searchFieldNameConverter.convertFieldName(classFieldName)

        val fieldType = fieldFilterModel.getString("fieldType")
        val filterType = fieldFilterModel.getString("filterType")

        when (fieldType) {

            "id" -> {
                val id = fieldFilterModel.getString("filter")
                queryDocument.append(convertedFieldName, ObjectId(id))
            }
            "text" -> {
                val isCaseInsensitive = this.caseInsensitiveQueryFieldNames.contains(classFieldName)
                val regexOptions = 0 + (if (isCaseInsensitive) Pattern.CASE_INSENSITIVE else 0)
                val filter = fieldFilterModel.getString("filter")

                when (filterType) {

                    "equals" -> {

                        val queryValue = if (isCaseInsensitive) {
                            Pattern.compile("^$filter$", Pattern.CASE_INSENSITIVE)
                        } else {
                            filter
                        }

                        queryDocument.append(convertedFieldName, queryValue)

                    }

                    "notEqual" -> queryDocument.append(convertedFieldName, Document("\$ne", filter))

                    "contains" -> queryDocument.append(convertedFieldName, Pattern.compile(".*$filter.*", regexOptions))

                    "notContains" -> queryDocument.append(convertedFieldName, Pattern.compile("^((?!$filter).)*\$"))

                    "startsWith" -> queryDocument.append(convertedFieldName, Pattern.compile("^$filter", regexOptions))

                    "endsWith" -> queryDocument.append(convertedFieldName, Pattern.compile("$filter\$", regexOptions))

                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")
                }
            }
            "date" -> {
                when (filterType) {
                    "equals" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateRangeDocument = Document()
                        dateRangeDocument.append("\$gte", dateFrom)
                        dateRangeDocument.append("\$lt", dateFrom.plusDays(1))
                        queryDocument.append(convertedFieldName, dateRangeDocument)

                    }
                    "notEqual" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")

                        queryDocument.append("\$or", listOf(
                            Document(convertedFieldName, Document("\$lt", dateFrom)),
                            Document(convertedFieldName, Document("\$gte", dateFrom.plusDays(1)))
                        ))

                    }
                    "greaterThan" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateFromDocument = Document()
                        dateFromDocument.append("\$gte", dateFrom)
                        queryDocument.append(convertedFieldName, dateFromDocument)

                    }
                    "lessThan" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateFromDocument = Document()
                        dateFromDocument.append("\$lt", dateFrom)
                        queryDocument.append(convertedFieldName, dateFromDocument)

                    }
                    "inRange" -> {

                        val dateFrom = fieldFilterModel.getLocalDate("dateFrom")
                        val dateTo = fieldFilterModel.getLocalDate("dateTo")
                        val dateRangeDocument = Document()
                        dateRangeDocument.append("\$gte", dateFrom)
                        dateRangeDocument.append("\$lt", dateTo.plusDays(1))
                        queryDocument.append(convertedFieldName, dateRangeDocument)

                    }
                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")
                }
            }
            "number" -> {
                when (filterType) {
                    "equals" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        queryDocument.append(convertedFieldName, filter)

                    }
                    "notEqual" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        queryDocument.append(convertedFieldName, Document("\$ne", filter))

                    }
                    "greaterThan" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        queryDocument.append(convertedFieldName, Document("\$gt", filter))

                    }
                    "greaterThanOrEqual" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        queryDocument.append(convertedFieldName, Document("\$gte", filter))

                    }
                    "lessThan" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        queryDocument.append(convertedFieldName, Document("\$lt", filter))

                    }
                    "lessThanOrEqual" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        queryDocument.append(convertedFieldName, Document("\$lte", filter))

                    }
                    "inRange" -> {

                        val filter = fieldFilterModel.getNumber("filter")
                        val filterTo = fieldFilterModel.getNumber("filterTo")
                        val rangeDocument = Document()
                        rangeDocument.append("\$gte", filter)
                        rangeDocument.append("\$lte", filterTo)
                        queryDocument.append(convertedFieldName, rangeDocument)

                    }
                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")
                }
            }
            else -> throw IllegalArgumentException("Unknown filter field type [$fieldType]")

        }

        return queryDocument

    }


    private fun filterModelConditionToBson(
            classFieldName: String,
            filterModelItem: FilterModelItem
    ): Document {

        val queryDocument = Document()
        val convertedFieldName = this.searchFieldNameConverter.convertFieldName(classFieldName)

        val fieldType = filterModelItem.fieldType
        val filterType = filterModelItem.filterType

        when (fieldType) {

            "id" -> {
                val id = filterModelItem.filter
                queryDocument.append(convertedFieldName, ObjectId(id))
            }
            "text" -> {
                val isCaseInsensitive = this.caseInsensitiveQueryFieldNames.contains(classFieldName)
                val regexOptions = 0 + (if (isCaseInsensitive) Pattern.CASE_INSENSITIVE else 0)
                val filter = filterModelItem.filter

                when (filterType) {

                    "equals" -> {

                        val queryValue = if (isCaseInsensitive) {
                            Pattern.compile("^$filter$", Pattern.CASE_INSENSITIVE)
                        } else {
                            filter
                        }

                        queryDocument.append(convertedFieldName, queryValue)

                    }

                    "notEqual" -> queryDocument.append(convertedFieldName, Document("\$ne", filter))

                    "contains" -> queryDocument.append(convertedFieldName, Pattern.compile(".*$filter.*", regexOptions))

                    "notContains" -> queryDocument.append(convertedFieldName, Pattern.compile("^((?!$filter).)*\$"))

                    "startsWith" -> queryDocument.append(convertedFieldName, Pattern.compile("^$filter", regexOptions))

                    "endsWith" -> queryDocument.append(convertedFieldName, Pattern.compile("$filter\$", regexOptions))

                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")
                }
            }
            "date" -> {
                when (filterType) {
                    "equals" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateRangeDocument = Document()
                        dateRangeDocument.append("\$gte", dateFrom)
                        dateRangeDocument.append("\$lt", dateFrom.plusDays(1))
                        queryDocument.append(convertedFieldName, dateRangeDocument)

                    }
                    "notEqual" -> {

                        val dateFrom = filterModelItem.dateFrom!!

                        queryDocument.append("\$or", listOf(
                            Document(convertedFieldName, Document("\$lt", dateFrom)),
                            Document(convertedFieldName, Document("\$gte", dateFrom.plusDays(1)))
                        ))

                    }
                    "greaterThan" -> {

                        val dateFrom = filterModelItem.dateFrom
                        val dateFromDocument = Document()
                        dateFromDocument.append("\$gte", dateFrom)
                        queryDocument.append(convertedFieldName, dateFromDocument)

                    }
                    "lessThan" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateFromDocument = Document()
                        dateFromDocument.append("\$lt", dateFrom)
                        queryDocument.append(convertedFieldName, dateFromDocument)

                    }
                    "inRange" -> {

                        val dateFrom = filterModelItem.dateFrom!!
                        val dateTo = filterModelItem.dateTo!!
                        val dateRangeDocument = Document()
                        dateRangeDocument.append("\$gte", dateFrom)
                        dateRangeDocument.append("\$lt", dateTo.plusDays(1))
                        queryDocument.append(convertedFieldName, dateRangeDocument)

                    }
                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")
                }
            }
            "number" -> {
                when (filterType) {
                    "equals" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        queryDocument.append(convertedFieldName, filter)

                    }
                    "notEqual" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        queryDocument.append(convertedFieldName, Document("\$ne", filter))

                    }
                    "greaterThan" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        queryDocument.append(convertedFieldName, Document("\$gt", filter))

                    }
                    "greaterThanOrEqual" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        queryDocument.append(convertedFieldName, Document("\$gte", filter))

                    }
                    "lessThan" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        queryDocument.append(convertedFieldName, Document("\$lt", filter))

                    }
                    "lessThanOrEqual" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        queryDocument.append(convertedFieldName, Document("\$lte", filter))

                    }
                    "inRange" -> {

                        val filter = filterModelItem.filter!!.toInt()
                        val filterTo = filterModelItem.filterTo!!.toInt()
                        val rangeDocument = Document()
                        rangeDocument.append("\$gte", filter)
                        rangeDocument.append("\$lte", filterTo)
                        queryDocument.append(convertedFieldName, rangeDocument)

                    }
                    else -> throw IllegalArgumentException("Unknown filter type [$filterType]")
                }
            }
            else -> throw IllegalArgumentException("Unknown filter field type [$fieldType]")

        }

        return queryDocument

    }


    private fun buildProjection(projectedFieldNames: Set<String>): Bson {

        val document = Document()
        projectedFieldNames.forEach { document.append(this.searchFieldNameConverter.convertFieldName(it), 1) }
        return document

    }


    private fun buildSortBson(searchModel: SearchModel): Bson? {

        val sortModelArrayNode = searchModel.sortModel

        if (sortModelArrayNode == null || sortModelArrayNode.isEmpty()) {
            return null
        }

        val sortDocument = Document()

        sortModelArrayNode.let { sortModelNode -> sortModelNode.forEach { v ->

            val classFieldName = v.fieldPath
            val sortDirectionRaw = v.sortDirection
            val columnName = this.searchFieldNameConverter.convertFieldName(classFieldName)

            val sortDirection = when(sortDirectionRaw) {
                "asc" -> 1
                "desc" -> -1
                else -> throw IllegalArgumentException("Unknown sort direction: $sortDirectionRaw")
            }

            sortDocument.append(columnName, sortDirection)

        } }

        return sortDocument

    }


}

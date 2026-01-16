package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithProvidedFieldConverter
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ClassVisibility
import org.maiaframework.gen.spec.definition.lang.DocumentMapperFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.jdbc.TableName
import org.maiaframework.lang.text.StringFunctions


class SearchableDtoDef(
    val dtoRootEntityDef: EntityDef,
    val dtoBaseName: DtoBaseName,
    val moduleName: ModuleName?,
    val packageName: PackageName,
    val tableName: TableName,
    fieldDefsNotInherited: List<SearchableDtoFieldDef>,
    val lookupDefs: List<SearchableDtoLookupDef>,
    val withPreAuthorize: WithPreAuthorize?,
    val withGeneratedEndpoint: WithGeneratedEndpoint,
    val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    val withGeneratedDto: WithGeneratedDto,
    val generateFindById: GenerateFindById,
    val searchModelType: SearchModelType,
    val withProvidedFieldConverter: WithProvidedFieldConverter,
    val manyToManyJoinEntityDefs: List<JoinEntityDef>
) {


    val allFields = fieldDefsNotInherited.sorted()


    val allRowMapperFieldDefs =
        allFields.map { RowMapperFieldDef(it.entityAndField.entityFieldDef, it.responseDtoFieldDef.nullability, it.classFieldName.value) }


    val schemaAndTableName = dtoRootEntityDef.schemaAndTableName

    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val searchDtoDef = SearchDtoDef(
        packageName,
        dtoBaseName,
        DtoSuffix("Dto"),
        allFields.map { it.classFieldDef },
        defaultSortModel = defaultSortModel(fieldDefsNotInherited),
        searchApiUrl = "/api/${dtoBaseName.toSnakeCase()}/search",
        countApiUrl = "/api/${dtoBaseName.toSnakeCase()}/count",
        findByIdServerSideApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}/{id}",
        findByIdClientSideApiUrl = "/api/$modulePath${dtoBaseName.toSnakeCase()}/",
        findAllApiUrl = "/api/${dtoBaseName.toSnakeCase()}/find_all",
        withGeneratedFindAllFunction = withGeneratedFindAllFunction,
        withGeneratedEndpoint = withGeneratedEndpoint,
        generateFindById = generateFindById,
        dataSourceType = DataSourceType.DATABASE,
        searchModelType = searchModelType
    )


    val dtoDef = searchDtoDef.dtoDef


    val uqcn = searchDtoDef.uqcn


    val fqcn = searchDtoDef.fqcn


    val rootDtoFields = fieldDefsNotInherited


    val hasAnyMapFields: Boolean = rootDtoFields.any { it.classFieldDef.isMap }


    private fun defaultSortModel(fields: List<SearchableDtoFieldDef>): List<FieldSortModel> {

        val defaultSortFields = fields.mapNotNull { it.fieldSortModel }

        val uniqueSortIndexCount = defaultSortFields.map { it.sortIndexAndDirection.sortIndex }.toSet().count()

        require(uniqueSortIndexCount == defaultSortFields.count()) {
            "There are duplicate default sort indexes defined on SearchableDto $dtoBaseName. ${defaultSortFields.map { it.toString() }}"
        }

        return defaultSortFields.sortedBy { it.sortIndexAndDirection.sortIndex }

    }


    val caseInsensitiveFields: List<SearchableDtoFieldDef> = this.allFields.filterNot { it.responseDtoFieldDef.caseSensitive.value }


    val dtoRepoClassDef = searchDtoDef.dtoRepoClassDef


    val documentMapperClassDef = searchDtoDef.documentMapperClassDef


    val searchableDtoSearchConverterClassDef = aClassDef(fqcn.withSuffix("SearchConverter"))
        .withSuperclass(initAgGridSearchConverterClassDef())
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val dtoSearchConverterClassDef = aClassDef(packageName.uqcn("${dtoBaseName}TableDtoSearchConverter"))
        .withSuperclass(initMongoSearchRequestFactoryClassDef())
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val fieldNameConverterClassDef = aClassDef(fqcn.withSuffix("FieldNameConverter"))
        .withInterface(ParameterizedType(Fqcns.SEARCH_FIELD_NAME_CONVERTER))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val fieldConverterClassDef = aClassDef(fqcn.withSuffix("FieldConverter"))
        .withInterface(ParameterizedType(Fqcns.SEARCH_FIELD_CONVERTER))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val dtoDaoClassDef = aClassDef(fqcn.withSuffix("Dao"))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val metaClassDef = aClassDef(fqcn.withSuffix("Meta"))
        .ofType(ClassType.OBJECT)
        .build()


    val hasLookupFields = lookupDefs.isNotEmpty()


    val angularComponentNames = AngularComponentNames(this.packageName, dtoBaseName.value)


    val angularServiceFieldName = StringFunctions.firstToLower(angularComponentNames.serviceName)


    val angularServiceName = angularComponentNames.serviceName


    val angularServiceImportStatement = angularComponentNames.serviceImportStatement


    val dtoRowMapperClassDef = this.searchDtoDef.dtoDef.rowMapperClassDef


    val rowMapperDef = RowMapperDef(
        uqcn,
        allRowMapperFieldDefs,
        dtoRowMapperClassDef,
        isForEditDto = false
    )


    private fun initAgGridSearchConverterClassDef(): ClassDef {

        val objectMapperFieldDef = aClassField("jsonMapper", FieldTypes.byFqcn(Fqcns.JACKSON_JSON_MAPPER)).build()
        val searchFieldNameConverterFieldDef = aClassField("fieldNameConverter", FieldTypes.byFqcn(Fqcns.SEARCH_FIELD_NAME_CONVERTER)).build()
        val searchFieldConverterFieldDef = aClassField("fieldConverter", FieldTypes.byFqcn(Fqcns.SEARCH_FIELD_CONVERTER)).build()

        val fieldDefsNotIndexFieldDef = listOf(
            objectMapperFieldDef,
            searchFieldNameConverterFieldDef,
            searchFieldConverterFieldDef
        )

        return ClassDef(
            ParameterizedType(Fqcns.ABSTRACT_AG_GRID_CONVERTER),
            Fqcns.ABSTRACT_AG_GRID_CONVERTER,
            true,
            ClassType.CLASS,
            ClassVisibility.PUBLIC,
            fieldDefsNotIndexFieldDef,
            emptyList(),
            emptyList(),
            emptyList(),
            null
        )

    }


    fun hasProvidedFieldConverter(): Boolean {

        return this.withProvidedFieldConverter.value

    }


    fun findFieldByPath(fieldName: String): SearchableDtoFieldDef {

        return this.allFields.find { it.classFieldName.value == fieldName }
            ?: throw RuntimeException("No field named $fieldName found on SearchableDtoDef $dtoBaseName with fields ${allFields.map { it.classFieldName }}.")

    }


    // TODO If we still need to support Mongo, we need to reintroduce this
    fun findFieldByPath_old(fieldName: String): EntityFieldDef {

        return this.dtoRootEntityDef.findFieldByNameOrNull(fieldName)
            ?: findLookupFieldByName(fieldName)

    }


    fun findDocumentFieldByPath(fieldName: String): DocumentMapperFieldDef {

        val entityFieldDef = dtoRootEntityDef.findFieldByNameOrNull(fieldName)

        if (entityFieldDef != null) {
            return DocumentMapperFieldDef(
                entityFieldDef.classFieldDef,
                entityFieldDef.tableColumnName,
                entityFieldDef.fieldReaderParameterizedType,
                entityFieldDef.fieldWriterParameterizedType
            )
        }

        val lookupFieldEntityFieldDef = findLookupFieldByName(fieldName)

        return DocumentMapperFieldDef(
            lookupFieldEntityFieldDef.classFieldDef.withFieldName(fieldName),
            TableColumnName(fieldName),
            lookupFieldEntityFieldDef.fieldReaderParameterizedType,
            lookupFieldEntityFieldDef.fieldWriterParameterizedType
        )

    }


    private fun findLookupFieldByName(fieldName: String): EntityFieldDef {

        this.lookupDefs.forEach { lookupDef ->
            lookupDef.lookupFieldDefs.forEach { field ->

                if (field.dtoFieldName.value == fieldName) {
                    return field.foreignFieldDef
                }

            }
        }

        throw IllegalArgumentException("No field named $fieldName found on SearchableDto ${this.dtoBaseName}")

    }


    private fun initMongoSearchRequestFactoryClassDef(): ClassDef {

        val objectMapperFieldDef = aClassField("jsonMapper", FieldTypes.byFqcn(Fqcns.JACKSON_JSON_MAPPER)).build()
        val searchFieldNameConverterFieldDef = aClassField("fieldNameConverter", FieldTypes.byFqcn(Fqcns.SEARCH_FIELD_NAME_CONVERTER)).build()
        val searchFieldConverterFieldDef = aClassField("fieldConverter", FieldTypes.byFqcn(Fqcns.SEARCH_FIELD_CONVERTER)).build()

        val fieldDefsNotIndexFieldDef = listOf(
            objectMapperFieldDef,
            searchFieldNameConverterFieldDef,
            searchFieldConverterFieldDef
        )

        return ClassDef(
            ParameterizedType(Fqcns.MONGO_SEARCH_REQUEST_FACTORY),
            Fqcns.MONGO_SEARCH_REQUEST_FACTORY,
            true,
            ClassType.CLASS,
            ClassVisibility.PUBLIC,
            fieldDefsNotIndexFieldDef,
            emptyList(),
            emptyList(),
            emptyList(),
            null
        )

    }


    fun findSearchableDtoFieldByName(dtoFieldName: String): SearchableDtoFieldDef {

        return this.allFields.firstOrNull { it.classFieldName.value == dtoFieldName }
            ?: throw IllegalArgumentException("No field named $dtoFieldName found on SearchableDtoDef with base name $dtoBaseName.")

    }


}

package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.SearchableDtoFieldDefBuilder
import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithProvidedFieldConverter
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ClassVisibility
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.jdbc.TableName
import org.maiaframework.lang.text.StringFunctions


class SearchableDtoDef(
    val dtoRootEntityDef: EntityDef,
    val dtoBaseName: DtoBaseName,
    val moduleName: ModuleName?,
    val packageName: PackageName,
    val tableName: TableName, // TODO is this always going to be the same as the dtoRootEntityDef.tableName?
    fieldDefsNotInherited: List<SearchableDtoFieldDef>,
    val withPreAuthorize: WithPreAuthorize?,
    val withGeneratedEndpoint: WithGeneratedEndpoint,
    val withGeneratedTypescriptService: WithGeneratedTypescriptService,
    val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    val withGeneratedDto: WithGeneratedDto,
    val generateFindById: GenerateFindById,
    val searchModelType: SearchModelType,
    val withProvidedFieldConverter: WithProvidedFieldConverter,
    val manyToManyJoinEntityDefs: List<JoinEntityDef>
) {


    private val rootDtoFieldDefs = enrichFields(fieldDefsNotInherited)


    private fun enrichFields(fieldDefsNotInherited: List<SearchableDtoFieldDef>): List<SearchableDtoFieldDef> {

        val fields = mutableListOf<SearchableDtoFieldDef>()

        if (dtoRootEntityDef.primaryKeyFields.size == 1) {

            val pkField = dtoRootEntityDef.primaryKeyFields.first()

            if (fieldDefsNotInherited.none { it.classFieldName == pkField.classFieldName }) {

                val entityAndField = EntityAndField(dtoRootEntityDef, pkField, null)
                val fieldPath = FieldPath.of(pkField.classFieldName.value)

                val builder = SearchableDtoFieldDefBuilder(
                    pkField.classFieldName,
                    entityAndField,
                    fieldPath,
                    caseSensitive = CaseSensitive.FALSE,
                    defaultFieldTypeFieldReaderProvider = { null },
                    defaultFieldTypeFieldWriterProvider = { null }
                )

                fields.add(builder.build())

            }

        } else if (dtoRootEntityDef.hasCompositePrimaryKey) {

            val idField = aClassField("id", FieldTypes.string).build()
            val compositeIdFieldDef = CompositeIdFieldDef(idField)

            fields.add(compositeIdFieldDef)

        }

        return fields + fieldDefsNotInherited

    }


    val idField = dtoRootEntityDef.idField


    val allFieldsSorted = rootDtoFieldDefs.sortedBy { it.classFieldDef.classFieldName }


    val nonManyToManyFields = allFieldsSorted.filterIsInstance<SimpleSearchableDtoFieldDef>()


    private val allRowMapperFieldDefs = allFieldsSorted
            .mapNotNull {
                when (it) {
                    is CompositeIdFieldDef -> null
                    is ManyToManySearchableDtoFieldDef -> ManyToManyRowMapperFieldDef(it, this.dtoRootEntityDef)
                    is SimpleSearchableDtoFieldDef -> EntityFieldRowMapperFieldDef(it.classFieldName, it.entityFieldDef, it.responseDtoFieldDef.classFieldName.value)
                }
            }


    val schemaAndTableName = dtoRootEntityDef.schemaAndTableName


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val searchDtoDef = SearchDtoDef(
        packageName,
        dtoBaseName,
        DtoSuffix("Dto"),
        allFieldsSorted.map { it.classFieldDef },
        defaultSortModel = defaultSortModel(rootDtoFieldDefs),
        searchApiUrl = "/api/${dtoBaseName.toKebabCase()}/search",
        countApiUrl = "/api/${dtoBaseName.toKebabCase()}/count",
        findByIdServerSideApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}/{id}",
        findByIdClientSideApiUrl = "/api/$modulePath${dtoBaseName.toKebabCase()}/",
        findAllApiUrl = "/api/${dtoBaseName.toKebabCase()}/find_all",
        withGeneratedFindAllFunction = withGeneratedFindAllFunction,
        withGeneratedEndpoint = withGeneratedEndpoint,
        withGeneratedTypescriptService = withGeneratedTypescriptService,
        generateFindById = generateFindById,
        dataSourceType = DataSourceType.DATABASE,
        searchModelType = searchModelType
    )


    val dtoDef = searchDtoDef.dtoDef


    val uqcn = searchDtoDef.uqcn


    val fqcn = searchDtoDef.fqcn


    val hasAnyMapFields: Boolean = rootDtoFieldDefs.any { it.classFieldDef.isMap }


    val hasAnyManyToManyFields: Boolean = allFieldsSorted.any { it is ManyToManySearchableDtoFieldDef }


    private fun defaultSortModel(fields: List<SearchableDtoFieldDef>): List<FieldSortModel> {

        val defaultSortFields = fields.mapNotNull { it.fieldSortModel }

        val uniqueSortIndexCount = defaultSortFields.map { it.sortIndexAndDirection.sortIndex }.toSet().count()

        require(uniqueSortIndexCount == defaultSortFields.count()) {
            "There are duplicate default sort indexes defined on SearchableDto $dtoBaseName. ${defaultSortFields.map { it.toString() }}"
        }

        return defaultSortFields.sortedBy { it.sortIndexAndDirection.sortIndex }

    }


    val caseInsensitiveFields: List<SimpleSearchableDtoFieldDef> = this.nonManyToManyFields.filterNot { it.responseDtoFieldDef.caseSensitive.value }


    val dtoRepoClassDef = searchDtoDef.dtoRepoClassDef


    val documentMapperClassDef = searchDtoDef.documentMapperClassDef


    val searchableDtoSearchConverterClassDef = aClassDef(fqcn.withSuffix("SearchConverter"))
        .withSuperclass(initAgGridSearchConverterClassDef())
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


    val angularComponentNames = AngularComponentNames(this.packageName, dtoBaseName.value)


    val angularServiceFieldName = StringFunctions.firstToLower(angularComponentNames.serviceName)


    val angularServiceName = angularComponentNames.serviceName


    val angularServiceImportStatement = angularComponentNames.serviceImportStatement


    val dtoRowMapperClassDef = this.searchDtoDef.dtoDef.rowMapperClassDef


    private val compositeIdFieldNames = if (dtoRootEntityDef.primaryKeyFields.size > 1) {
        dtoRootEntityDef.primaryKeyFieldsSorted.map { it.classFieldName.value }
    } else {
        emptyList()
    }


    val rowMapperDef = RowMapperDef(
        searchDtoDef.fqcn,
        allRowMapperFieldDefs,
        dtoRowMapperClassDef,
        isForEditDto = false,
        compositeIdFields = compositeIdFieldNames
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


    fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef {

        return getFieldByPath(fieldPath, dtoRootEntityDef)

    }


    private fun getFieldByPath(
        fieldPath: FieldPath,
        entityDef: EntityDef
    ): ClassFieldDef {

        val fieldName = fieldPath.head()
        val entityFieldDef = entityDef.findFieldByName(fieldName)

        return if (fieldPath.isJustOneField()) {
            val fieldName = fieldPath.head()
            val searchableDtoFieldDef = findSearchableDtoFieldByName(fieldName)
            searchableDtoFieldDef.classFieldDef
        } else {

            val fieldType = entityFieldDef.fieldType

            val foreignKeyEntityDef = if (fieldType is ForeignKeyFieldType) {
                fieldType.foreignKeyFieldDef.foreignEntityDef
            } else {
                throw RuntimeException("Entity field $fieldName on Entity ${entityDef.entityBaseName} does not reference a foreign key entity. fieldPath=$fieldPath")
            }

            getFieldByPath(
                fieldPath.tail(),
                foreignKeyEntityDef
            )

        }

    }


    fun findSearchableDtoFieldByName(dtoFieldName: String): SearchableDtoFieldDef {

        return this.rootDtoFieldDefs.firstOrNull { it.classFieldName.value == dtoFieldName }
            ?: throw IllegalArgumentException("No field named $dtoFieldName found on SearchableDtoDef with base name $dtoBaseName. Existing fields: ${this.allFieldsSorted.map { it.classFieldName.value }}")

    }


}

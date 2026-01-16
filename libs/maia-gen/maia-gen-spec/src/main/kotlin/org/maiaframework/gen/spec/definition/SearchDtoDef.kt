package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ClassVisibility
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType

class SearchDtoDef(
    packageName: PackageName,
    val dtoBaseName: DtoBaseName,
    dtoSuffix: DtoSuffix,
    dtoClassFields: List<ClassFieldDef>,
    val defaultSortModel: List<FieldSortModel>,
    val dataSourceType: DataSourceType,
    val searchModelType: SearchModelType,
    val searchApiUrl: String,
    val countApiUrl: String,
    val findByIdClientSideApiUrl: String,
    val findByIdServerSideApiUrl: String,
    val findAllApiUrl: String,
    val withGeneratedFindAllFunction: WithGeneratedFindAllFunction,
    val generateFindById: GenerateFindById,
    val withGeneratedEndpoint: WithGeneratedEndpoint
) {

    val dtoDef = DtoDefBuilder(packageName, dtoBaseName, dtoSuffix, dtoClassFields)
        .withCharacteristic(DtoCharacteristic.RESPONSE_DTO)
        .build()

    val fqcn = dtoDef.fqcn

    val uqcn = fqcn.uqcn

    val angularComponentNames = AngularComponentNames(packageName, dtoBaseName.value)

    private val searchServiceClassName = "${uqcn}SearchService"


    val searchServiceFqcn = packageName.uqcn(searchServiceClassName)


    val searchEndpointClassDef = aClassDef(fqcn.withSuffix("SearchEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val searchServiceClassDef = aClassDef(searchServiceFqcn)
        .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
        .build()


    val esDocMapperClassDef = aClassDef(fqcn.withSuffix("EsDocMapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val dtoRepoClassDef = aClassDef(fqcn.withSuffix("Repo"))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val fieldNameMapperClassDef = aClassDef(fqcn.withSuffix("FieldNameMapper"))
        .ofType(ClassType.OBJECT)
        .build()


    val csvHelperClassDef = aClassDef(fqcn.withSuffix("CsvHelper"))
        .withInterface(ParameterizedType(Fqcns.CSV_WRITER_HELPER, ParameterizedType(fqcn)))
        .build()

    val documentMapperClassDef = aClassDef(fqcn.withSuffix("DocumentMapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val tableDtoSearchConverterClassDef = aClassDef(packageName.uqcn("${dtoBaseName}TableDtoSearchConverter"))
        .withSuperclass(initMongoSearchRequestFactoryClassDef())
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    private fun initMongoSearchRequestFactoryClassDef(): ClassDef {

        val objectMapperFieldDef = aClassField("jsonMapper", FieldTypes.byFqcn(Fqcns.JACKSON_JSON_MAPPER)).build()
        val searchFieldNameConverterFieldDef =
            aClassField("fieldNameConverter", FieldTypes.byFqcn(Fqcns.SEARCH_FIELD_NAME_CONVERTER)).build()
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


}

package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.flags.DataSourceType
import org.maiaframework.gen.spec.definition.flags.GenerateFindById
import org.maiaframework.gen.spec.definition.flags.SearchModelType
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.flags.WithGeneratedTypescriptService
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType

class SearchDtoDef(
    private val packageName: PackageName,
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
    val withGeneratedEndpoint: WithGeneratedEndpoint,
    val withGeneratedTypescriptService: WithGeneratedTypescriptService,
    /** Trailing version suffix such as `_v0002`, or empty. Appended last to the DTO name and to every name derived from it. */
    private val versionSuffix: String = "",
) {


    private val unversionedUqcn = dtoBaseName.withSuffix(dtoSuffix).value


    /** A class derived from this DTO: the version-less DTO name, then [suffix], then the version suffix. */
    private fun derivedFqcn(suffix: String) = packageName.uqcn("$unversionedUqcn$suffix$versionSuffix")


    val dtoDef = DtoDefBuilder(packageName, dtoBaseName, DtoSuffix("${dtoSuffix.value}$versionSuffix"), dtoClassFields)
        .withCharacteristic(DtoCharacteristic.RESPONSE_DTO)
        .build()


    val fqcn = dtoDef.fqcn


    val uqcn = fqcn.uqcn


    val angularComponentNames = AngularComponentNames(packageName, dtoBaseName.value)


    val searchServiceFqcn = derivedFqcn("SearchService")


    val searchEndpointClassDef = aClassDef(derivedFqcn("SearchEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val searchServiceClassDef = aClassDef(searchServiceFqcn)
        .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
        .build()


    val esDocMapperClassDef = aClassDef(derivedFqcn("EsDocMapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val dtoRepoClassDef = aClassDef(derivedFqcn("Repo"))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val fieldNameMapperClassDef = aClassDef(derivedFqcn("FieldNameMapper"))
        .ofType(ClassType.OBJECT)
        .build()


    val csvHelperClassDef = aClassDef(derivedFqcn("CsvHelper"))
        .withInterface(ParameterizedType(Fqcns.CSV_WRITER_HELPER, ParameterizedType(fqcn)))
        .build()

    val documentMapperClassDef = aClassDef(derivedFqcn("DocumentMapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


}

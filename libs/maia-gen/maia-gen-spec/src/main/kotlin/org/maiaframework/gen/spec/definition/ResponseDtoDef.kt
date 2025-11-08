package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.flags.WithProvidedFieldConverter
import org.maiaframework.gen.spec.definition.lang.*
import org.maiaframework.jdbc.TableName


class ResponseDtoDef(
    val dtoBaseName: DtoBaseName,
    packageName: PackageName,
    val tableName: TableName,
    fieldDefsNotInherited: List<ResponseDtoFieldDef>,
    val withPreAuthorize: WithPreAuthorize?,
    private val withProvidedFieldConverter: WithProvidedFieldConverter
) {


    private val dtoSuffix = DtoSuffix("ResponseDto")


    val allFields = fieldDefsNotInherited.sorted()


    val allClassFields: List<ClassFieldDef> = this.allFields.map { it.classFieldDef }


    val dtoDef = DtoDefBuilder(packageName, dtoBaseName, dtoSuffix, allClassFields)
        .withCharacteristic(DtoCharacteristic.RESPONSE_DTO)
        .build()


    val repoClassDef = aClassDef(packageName.uqcn(dtoBaseName.toString() + "ResponseDtoRepo"))
        .ofType(ClassType.INTERFACE)
        .build()


    val endpointClassDef = aClassDef(packageName.uqcn(dtoBaseName.toString() + "ResponseDtoEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .withConstructorAnnotation(AnnotationDefs.SPRING_AUTOWIRED)
        .build()


    val csvHelperClassDef = aClassDef(packageName.uqcn(dtoBaseName.toString() + "DtoCsvHelper"))
        .withInterface(
            ParameterizedType(
                Fqcns.CSV_WRITER_HELPER,
                ParameterizedType(packageName.uqcn(this.dtoDef.uqcn))
            )
        ).build()


    val searchParserClassDef = aClassDef(packageName.uqcn(dtoBaseName.toString() + "ResponseDtoSearchParser"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withConstructorAnnotation(AnnotationDefs.SPRING_AUTOWIRED)
        .build()


    val searchRequestFieldNameConverterClassDef = aClassDef(packageName.uqcn(dtoBaseName.withSuffix("SearchRequestFieldNameConverter").value))
        .withInterface(ParameterizedType(Fqcns.SEARCH_FIELD_NAME_CONVERTER))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withConstructorAnnotation(AnnotationDefs.SPRING_AUTOWIRED)
        .build()


    val searchRequestFieldConverterClassDef = aClassDef(packageName.uqcn(dtoBaseName.withSuffix("SearchRequestFieldConverter").value))
        .withInterface(ParameterizedType(Fqcns.SEARCH_FIELD_CONVERTER))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withConstructorAnnotation(AnnotationDefs.SPRING_AUTOWIRED)
        .build()


    fun hasProvidedFieldConverter(): Boolean {

        return this.withProvidedFieldConverter.value

    }


    fun findFieldByName(fieldName: String): ResponseDtoFieldDef {

        return allFields.firstOrNull { v -> v.classFieldDef.classFieldName.value == fieldName }
            ?: throw IllegalArgumentException("No field named " + fieldName + " is defined on dto " + this.dtoBaseName)

    }


}

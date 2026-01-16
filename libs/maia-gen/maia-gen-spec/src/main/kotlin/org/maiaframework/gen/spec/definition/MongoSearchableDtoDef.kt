package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ClassVisibility
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


class MongoSearchableDtoDef(
    val dtoFqcn: Fqcn,
    val searchApiUrl: String,
    val withPreAuthorize: WithPreAuthorize?,
    val dataSourceType: DataSourceType,
    val searchServiceFqcn: Fqcn
) {

    val dtoRepoClassDef: ClassDef = aClassDef(dtoFqcn.withSuffix("Repo"))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()

    val searchableEndpointClassDef: ClassDef = aClassDef(dtoFqcn.withSuffix("SearchEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .withConstructorAnnotation(AnnotationDefs.SPRING_AUTOWIRED)
        .build()

    val documentMapperClassDef: ClassDef = aClassDef(dtoFqcn.withSuffix("DocumentMapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()

    val searchableDtoSearchConverterClassDef: ClassDef = aClassDef(dtoFqcn.withSuffix("SearchConverter"))
        .withSuperclass(initAgGridSearchConverterClassDef())
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withConstructorAnnotation(AnnotationDefs.SPRING_AUTOWIRED)
        .build()


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


}

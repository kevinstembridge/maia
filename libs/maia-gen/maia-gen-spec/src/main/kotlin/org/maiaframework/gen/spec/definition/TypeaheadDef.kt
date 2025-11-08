package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEsDocRepo
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class TypeaheadDef(
    packageName: PackageName,
    val typeaheadName: TypeaheadName,
    entityDef: EntityDef?,
    val idFieldName: ClassFieldName,
    val sortByFieldName: String,
    val searchTermFieldName: String,
    indexVersion: Int,
    val withHandCodedEsDocRepo: WithHandCodedEsDocRepo,
    val fieldDefs: List<TypeaheadFieldDef>
) {


    val angularServiceFileName = "${typeaheadName.toKebabCase()}-typeahead-api.service"


    val angularServiceClassName = "${typeaheadName}TypeaheadApiService"


    val endpointUrl: String = "/api/typeahead/${typeaheadName.toSnakeCase()}"


    val elasticIndexBaseName = ElasticIndexBaseName("${typeaheadName.toSnakeCase()}_typeahead")


    val entityUqcn = entityDef?.entityClassDef?.uqcn


    val entityDaoFqcn = entityDef?.daoFqcn


    val entityRepoFqcn = entityDef?.entityRepoFqcn


    val entityCrudApiDef = entityDef?.entityCrudApiDef


    val esIndexClassDef = aClassDef(packageName.uqcn("${typeaheadName}TypeaheadEsIndex"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val serviceClassDef = aClassDef(packageName.uqcn("${typeaheadName}TypeaheadService"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val crudListenerClassDef = entityCrudApiDef?.let {
        aClassDef(packageName.uqcn("${typeaheadName}TypeaheadCrudListenerImpl"))
            .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
            .withInterfaces(ParameterizedType(it.entityDef.crudListenerClassDef.fqcn))
            .build()
    }


    val indexServiceClassDef = aClassDef(packageName.uqcn("${typeaheadName}TypeaheadIndexService"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val endpointClassDef = aClassDef(packageName.uqcn("${typeaheadName}TypeaheadEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val refreshIndexJobClassDef = aClassDef(packageName.uqcn("Refresh${typeaheadName}TypeaheadIndexJob"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedType(Fqcns.MAIA_JOB))
        .build()


    private val esDocFields = this.fieldDefs.map { EsDocFieldDef(it.classFieldDef, it.esDocMappingType, it.entityFieldDef) }


    val esDocDef = EsDocDef(
        packageName,
        DtoBaseName("${typeaheadName}Typeahead"),
        elasticIndexBaseName,
        indexVersion,
        Description("A typeahead index for the $searchTermFieldName field of $typeaheadName records."),
        esDocFields,
        renderFieldEnum = false,
        generateRefreshIndexJob = true,
        disableRendering = false,
        entityDef
    )


    val typescriptServiceRenderedFilePath = "app/gen-components/${packageName.asTypescriptDirs()}/${this.angularServiceFileName}.ts"


    val typescriptServiceImportStatement = "import {$angularServiceClassName} from '@app/gen-components/${packageName.asTypescriptDirs()}/${this.angularServiceFileName}';"


    val typescriptServiceImport = TypescriptImport(angularServiceClassName, "@app/gen-components/${packageName.asTypescriptDirs()}/${this.angularServiceFileName}")


}

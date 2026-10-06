package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEsDocRepo
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class TypeaheadDef(
    packageName: PackageName,
    val typeaheadBaseName: TypeaheadBaseName,
    entityDef: EntityDef?,
    val sortByFieldName: String,
    val searchTermFieldName: String,
    indexVersion: Int,
    val withHandCodedEsDocRepo: WithHandCodedEsDocRepo,
    val fieldDefs: List<TypeaheadFieldDef>
) {


    val idField: TypeaheadFieldDef by lazy {

        this.fieldDefs.first { it.isIdField }

    }


    private val typeaheadNameAndVersion = "${typeaheadBaseName}V$indexVersion"


    val angularServiceFileName = "${typeaheadBaseName.toKebabCase()}-typeahead-api.service"


    val angularServiceClassName = "${typeaheadBaseName}TypeaheadApiService"


    val endpointUrl: String = "/api/typeahead/${typeaheadBaseName.toKebabCase()}"


    val elasticIndexBaseName = ElasticIndexBaseName("${typeaheadBaseName.toKebabCase()}-typeahead")


    val entityUqcn = entityDef?.entityClassDef?.uqcn


    val entityDaoFqcn = entityDef?.daoFqcn


    val entityRepoFqcn = entityDef?.entityRepoFqcn


    val entityCrudApiDef = entityDef?.entityCrudApiDef


    val esIndexClassDef = aClassDef(packageName.uqcn("${typeaheadBaseName}TypeaheadEsIndex"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val serviceClassDef = aClassDef(packageName.uqcn("${typeaheadBaseName}TypeaheadService"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val crudListenerClassDef = entityCrudApiDef?.let {
        aClassDef(packageName.uqcn("${typeaheadBaseName}TypeaheadCrudListenerImpl"))
            .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
            .withInterfaces(ParameterizedType(it.entityDef.crudListenerClassDef.fqcn))
            .build()
    }


    val indexServiceClassDef = aClassDef(packageName.uqcn("${typeaheadBaseName}TypeaheadIndexService"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val endpointClassDef = aClassDef(packageName.uqcn("${typeaheadBaseName}TypeaheadEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val refreshIndexJobClassDef = aClassDef(packageName.uqcn("Refresh${typeaheadBaseName}TypeaheadIndexJob"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedType(Fqcns.MAIA_JOB))
        .build()


    val esDocIdFieldName: String = fieldDefs.firstOrNull {
        it.entityFieldDef?.classFieldName == ClassFieldName.id
    }?.classFieldDef?.classFieldName?.value ?: "id"


    private val esDocFields = this.fieldDefs.map {
        EsDocFieldDef(
            it.classFieldDef,
            it.esDocMappingType,
            it.entityFieldDef,
            it.isIdField
        )
    }


    val esDocDef = EsDocDef(
        packageName,
        DtoBaseName("${typeaheadBaseName}Typeahead"),
        elasticIndexBaseName,
        indexVersion,
        Description("A typeahead index for the $searchTermFieldName field of $typeaheadBaseName records."),
        esDocFields,
        renderFieldEnum = false,
        generateRefreshIndexJob = true,
        disableRendering = false,
        entityDef
    )


    val typescriptServiceRenderedFilePath = "app/gen-components/${packageName.asTypescriptDirs()}/${this.angularServiceFileName}.ts"


    val typescriptServiceImportStatement = "import {$angularServiceClassName} from '@app/gen-components/${packageName.asTypescriptDirs()}/${this.angularServiceFileName}';"


    val typescriptServiceImport = TypescriptImport(angularServiceClassName, "@app/gen-components/${packageName.asTypescriptDirs()}/${this.angularServiceFileName}")


    init {

        require(this.fieldDefs.count { it.isIdField } == 1) {
            "The ${typeaheadBaseName}Typeahead must have exactly one ID field. Found ${this.fieldDefs.filter { it.isIdField }}."
        }

    }


}

package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.WithHandCodedEsDocRepo
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.lang.text.StringFunctions.toKebabCase

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


    private val typeaheadName = "${typeaheadBaseName}Typeahead"


    private val typeaheadNameKebab = toKebabCase(typeaheadName)


    private val versionSuffix = VersionSuffix.of(indexVersion)


    val angularServiceFileName = "${typeaheadNameKebab}-api-service${VersionSuffix.kebab(indexVersion)}"


    val angularServiceClassName = "${typeaheadName}ApiService$versionSuffix"


    val endpointUrl = "/api/v${indexVersion}/${typeaheadNameKebab}"


    val elasticIndexBaseName = ElasticIndexBaseName(typeaheadNameKebab)


    val entityUqcn = entityDef?.entityClassDef?.uqcn


    val entityDaoFqcn = entityDef?.daoFqcn


    val entityRepoFqcn = entityDef?.entityRepoFqcn


    val entityCrudApiDef = entityDef?.entityCrudApiDef?.also {
        require(it.entityDef.crudDef.withCrudListener.value) {
            "Typeahead $typeaheadName$versionSuffix keeps its index in step through the CrudListener of entity " +
                "${it.entityDef.entityBaseName}, but that entity declares withCrudListener(false) in its crud {} block"
        }
    }


    val esIndexClassDef = aClassDef(packageName.uqcn("${typeaheadName}EsIndex$versionSuffix"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val serviceClassDef = aClassDef(packageName.uqcn("${typeaheadName}SearchService$versionSuffix"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val crudListenerClassDef = entityCrudApiDef?.let {
        aClassDef(packageName.uqcn("${typeaheadName}CrudListenerImpl$versionSuffix"))
            .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
            .withInterfaces(ParameterizedType(it.entityDef.crudListenerClassDef.fqcn))
            .build()
    }


    val indexServiceClassDef = aClassDef(packageName.uqcn("${typeaheadName}IndexService$versionSuffix"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val endpointClassDef = aClassDef(packageName.uqcn("${typeaheadName}SearchEndpoint$versionSuffix"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val refreshIndexJobClassDef = aClassDef(packageName.uqcn("Refresh${typeaheadName}IndexJob$versionSuffix"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedType(Fqcns.MAIA_JOB))
        .build()


    val esDocIdFieldName: String = fieldDefs.firstOrNull {
        it.isIdField
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
        DtoBaseName(typeaheadName),
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


    val angularComponentFileName = "$typeaheadNameKebab${VersionSuffix.kebab(indexVersion)}"


    val angularComponentClassName = "${typeaheadName}Component$versionSuffix"


    val angularComponentSelector = "app-$typeaheadNameKebab${VersionSuffix.kebab(indexVersion)}"


    val typescriptComponentRenderedFilePath = "app/gen-components/${packageName.asTypescriptDirs()}/${this.angularComponentFileName}.ts"


    /** A standalone component, so it goes into the `imports` array of the components that use it. */
    val typescriptComponentImport = TypescriptImport(angularComponentClassName, "@app/gen-components/${packageName.asTypescriptDirs()}/${this.angularComponentFileName}", isModule = true)


    init {

        require(this.fieldDefs.count { it.isIdField } == 1) {
            "The $typeaheadBaseName typeahead must have exactly one ID field. Found ${this.fieldDefs.filter { it.isIdField }}."
        }

    }


}

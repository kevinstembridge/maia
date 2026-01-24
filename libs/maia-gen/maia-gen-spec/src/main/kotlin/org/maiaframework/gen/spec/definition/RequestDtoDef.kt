package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class RequestDtoDef(
    val dtoBaseName: DtoBaseName,
    dtoSuffix: DtoSuffix = DtoSuffix("RequestDto"),
    val packageName: PackageName,
    moduleName: ModuleName?,
    requestMappingPath: String? = null,
    dtoFieldDefs: List<RequestDtoFieldDef>,
    val preAuthorizeExpression: PreAuthorizeExpression?,
    val withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.TRUE
) {


    val dtoFieldDefs = dtoFieldDefs
        .sortedWith(compareBy<RequestDtoFieldDef> { it.classFieldDef.nullable }
            .thenBy { it.classFieldDef.classFieldName })


    val classFieldDefs = this.dtoFieldDefs.map { it.classFieldDef }


    private val modulePath = if (moduleName == null) "" else "${moduleName.value}/"


    val requestMappingPath = requestMappingPath ?: "/api/$modulePath${dtoBaseName.toSnakeCase()}"


    val classDef = aClassDef(packageName.uqcn(dtoBaseName.withSuffix(dtoSuffix).value))
        .withClassAnnotation(AnnotationDefs.JSON_IGNORE_PROPERTIES)
        .withConstructorAnnotation(AnnotationDefs.JSON_CREATOR)
        .withFieldDefsNotInherited(dtoFieldDefs.map { it.classFieldDef })
        .build()


    val handlerClassDef = aClassDef(packageName.uqcn("${dtoBaseName}RequestDtoHandler"))
        .ofType(ClassType.INTERFACE)
        .build()


    val endpointClassDef = aClassDef(packageName.uqcn("${dtoBaseName}RequestDtoEndpoint"))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val uqcn = this.classDef.uqcn


    val fqcn = this.classDef.fqcn


    private val typescriptFilePathWithoutSuffix = "app/gen-components/${packageName.asTypescriptDirs()}/${this.uqcn}"


    val typescriptDtoRenderedFilePath = "$typescriptFilePathWithoutSuffix.ts"


    val typescriptFileImportStatement = "import {${this.uqcn}} from '@$typescriptFilePathWithoutSuffix';"


    val typescriptImport = TypescriptImport(uqcn.value, "@$typescriptFilePathWithoutSuffix")


    fun findFieldByName(fieldName: ClassFieldName): RequestDtoFieldDef {

        return this.dtoFieldDefs.firstOrNull { it.classFieldDef.classFieldName == fieldName }
            ?: throw ClassFieldNotExistsException(fieldName, "RequestDto '$dtoBaseName'")

    }


}

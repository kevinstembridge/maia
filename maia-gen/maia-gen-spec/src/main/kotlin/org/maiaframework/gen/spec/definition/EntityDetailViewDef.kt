package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType


class EntityDetailViewDef(
    val entityDef: EntityDef
) {


    private val entityDetailBaseName = entityDef.entityBaseName.withSuffix("EntityDetail")


    private val modulePath = if (entityDef.moduleName == null) "" else "/${entityDef.moduleName.value}"


    private val viewBaseName = entityDetailBaseName.withSuffix("View").value


    private val viewContentComponentBaseName = DtoBaseName(viewBaseName)


    val viewContentAngularComponentNames = AngularComponentNames(this.entityDef.packageName, this.viewContentComponentBaseName.value)


    private val dtoBaseName = DtoBaseName(viewContentComponentBaseName.withSuffix("Dto").value)


    val typescriptServiceName = viewContentAngularComponentNames.serviceName


    val viewContentComponentHtmlRenderedFilePath = viewContentAngularComponentNames.htmlRenderedFilePath


    val fetchApiUrlForTypescript = $$"/api$$modulePath/$${dtoBaseName.toKebabCase()}/${id}"


    val fetchApiUrlForKotlin = "/api$modulePath/${dtoBaseName.toKebabCase()}/{id}"


    val dtoDef = DtoDefBuilder(
        entityDef.packageName,
        DtoBaseName(entityDef.entityBaseName.value),
        DtoSuffix("EntityDetailViewDto"),
        entityDef.allClassFields.map { toDtoClassField(it) }
    )
        .withCharacteristic(DtoCharacteristic.RESPONSE_DTO)
        .build()


    private fun toDtoClassField(classFieldDef: ClassFieldDef): ClassFieldDef {

        val fieldType = classFieldDef.fieldType

        if (fieldType is ForeignKeyFieldType) {

            if (fieldType.foreignKeyFieldDef.foreignEntityDef.hasPkAndNameDtoDef) {

                val newFieldType = FieldTypes.pkAndName(fieldType.foreignKeyFieldDef.foreignEntityDef.entityPkAndNameDef)
                val builder = aClassField(classFieldDef.classFieldName, newFieldType)
                classFieldDef.displayName?.let { builder.displayName(it.value) }
                return builder.build()

            }

        }

        val pipes = pipesFor(fieldType)

        return classFieldDef.copy(pipes = pipes)

    }


    private fun pipesFor(fieldType: FieldType): List<String> {

        return when (fieldType) {
            is InstantFieldType -> listOf(Pipes.INSTANT_PIPE)
            else -> emptyList()
        }

    }


    val endpointClassDef = aClassDef(entityDef.packageName.uqcn(dtoDef.uqcn.withSuffix("Endpoint")))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    val serviceClassDef = aClassDef(entityDef.packageName.uqcn(dtoDef.uqcn.withSuffix("Service")))
            .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
            .build()


    val entityRepoClassDef = entityDef.entityRepoClassDef


    val repoClassDef = aClassDef(entityDef.packageName.uqcn(dtoDef.uqcn.withSuffix("Repo")))
            .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
            .build()


}

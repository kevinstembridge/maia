package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions


data class ManyToManyTimestampedFieldDef(
    val entityDef: EntityDef,
    val manyToManyEntityDef: ManyToManyEntityDef,
    val typeaheadDef: TypeaheadDef,
    val joinRequestDtoDef: RequestDtoDef
) {

    private val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)

    val fieldName: String = otherSide.fieldName
    val displayName: String = otherSide.displayName

    val joinsFieldName: String = "${fieldName}Joins"
    val showFormSignalName: String = "show${displayName}JoinForm"
    val addEntityControlName: String = "add${displayName}JoinEntityControl"
    val effectiveFromControlName: String = "add${displayName}JoinEffectiveFromControl"
    val effectiveToControlName: String = "add${displayName}JoinEffectiveToControl"
    val filteredFieldName: String = "filtered${displayName}Entities"
    val filteredIsLoadingFieldName: String = "${filteredFieldName}IsLoading"
    val confirmMethodName: String = "confirmAdd${displayName}Join"
    val cancelMethodName: String = "cancelAdd${displayName}Join"
    val removeMethodName: String = "remove${displayName}Join"
    val joinEntryTypeName: String = "${displayName}JoinEntry"
    val requestDtoFieldName: String = "${fieldName}Entities"
    val joinRequestDtoClassName: String = joinRequestDtoDef.uqcn.value
    val joinEntityIdFieldName: String = "${fieldName}EntityId"

    val esDocClassName: String = typeaheadDef.esDocDef.dtoDef.uqcn.value
    val serviceClassName: String = typeaheadDef.angularServiceClassName
    val serviceFieldName: String = StringFunctions.firstToLower(serviceClassName)
    val searchTermFieldName: String = typeaheadDef.searchTermFieldName
    val esDocIdFieldName: String = typeaheadDef.esDocIdFieldName

    val serviceImport: TypescriptImport = typeaheadDef.typescriptServiceImport
    val esDocImport: TypescriptImport = typeaheadDef.esDocDef.dtoDef.typescriptDtoImport
    val joinRequestDtoTypescriptImport: TypescriptImport = joinRequestDtoDef.typescriptImport

    val labelText: String = "$displayName Entities"
    val searchPlaceholder: String = "Search $labelText..."
    val autocompleteRefName: String = "${fieldName}JoinEntityAuto"
    val displayWithMethodName: String = "display${displayName}Entity"

}

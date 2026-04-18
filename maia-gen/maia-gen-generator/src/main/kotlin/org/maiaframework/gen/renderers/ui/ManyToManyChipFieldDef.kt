package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions


data class ManyToManyChipFieldDef(
    val entityDef: EntityDef,
    val manyToManyEntityDef: ManyToManyEntityDef,
    val typeaheadDef: TypeaheadDef
) {

    private val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)

    val fieldName: String = otherSide.fieldName
    val displayName: String = otherSide.displayName
    val labelText: String = "$displayName Entities"
    val searchPlaceholder: String = "Search $labelText..."

    val selectedFieldName: String = "selected${displayName}Entities"
    val filteredFieldName: String = "filtered${displayName}Entities"
    val filteredIsLoadingFieldName: String = "${filteredFieldName}IsLoading"
    val searchControlFieldName: String = "${fieldName}EntitySearchControl"
    val inputRefName: String = "${fieldName}EntityInput"
    val autocompleteRefName: String = "${fieldName}EntityAuto"
    val addMethodName: String = "add${displayName}Entity"
    val removeMethodName: String = "remove${displayName}Entity"
    val requestDtoFieldName: String = "${fieldName}EntityIds"
    val fetchForEditDtoFieldName: String = "${fieldName}Entities"

    val esDocClassName: String = typeaheadDef.esDocDef.dtoDef.uqcn.value
    val serviceClassName: String = typeaheadDef.angularServiceClassName
    val serviceFieldName: String = StringFunctions.firstToLower(serviceClassName)
    val searchTermFieldName: String = typeaheadDef.searchTermFieldName
    val esDocIdFieldName: String = typeaheadDef.esDocIdFieldName

    val serviceImport: TypescriptImport = typeaheadDef.typescriptServiceImport
    val esDocImport: TypescriptImport = typeaheadDef.esDocDef.dtoDef.typescriptDtoImport

}

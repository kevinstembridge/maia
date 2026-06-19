package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.lang.text.StringFunctions.firstToUpper


data class ManyToManyTimestampedFieldDef(
    val entityDef: EntityDef,
    val manyToManyEntityDef: ManyToManyEntityDef,
    val typeaheadDef: TypeaheadDef,
    val joinRequestDtoDef: RequestDtoDef
) {

    private val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)

    // Disambiguates UI-internal identifiers when entityDef has multiple many-to-many
    // associations whose other side resolves to the same field/display name (e.g. two
    // associations both pointing at a "right" entity). Empty for the first such association,
    // preserving existing naming for backwards compatibility.
    private val nameSuffix = manyToManyEntityDef.nameSuffixFor(entityDef)

    val fieldName: String = otherSide.fieldName
    val displayName: String = otherSide.displayName

    private val uniqueFieldName: String = "$fieldName$nameSuffix"
    private val uniqueFieldNameUpper: String = firstToUpper("$fieldName$nameSuffix")
    private val uniqueDisplayName: String = "$displayName$nameSuffix"

    val joinsFieldName: String = "${uniqueFieldName}Joins"
    val showFormSignalName: String = "show${uniqueFieldNameUpper}JoinForm"
    val addEntityControlName: String = "add${uniqueFieldNameUpper}JoinEntityControl"
    val effectiveFromControlName: String = "add${uniqueFieldNameUpper}JoinEffectiveFromControl"
    val effectiveToControlName: String = "add${uniqueFieldNameUpper}JoinEffectiveToControl"
    val filteredFieldName: String = "filtered${uniqueFieldNameUpper}Entities"
    val filteredIsLoadingFieldName: String = "${filteredFieldName}IsLoading"
    val confirmMethodName: String = "confirmAdd${uniqueFieldNameUpper}Join"
    val cancelMethodName: String = "cancelAdd${uniqueFieldNameUpper}Join"
    val removeMethodName: String = "remove${uniqueFieldNameUpper}Join"
    val joinEntryTypeName: String = "${uniqueFieldName}JoinEntry"
    val requestDtoFieldName: String = "${fieldName}Entities"
    val fetchForEditDtoFieldName: String = manyToManyEntityDef.fetchForEditFieldNameFor(entityDef)
    val joinRequestDtoClassName: String = joinRequestDtoDef.uqcn.value
    val joinEntityIdFieldName: String = "${fieldName}EntityId"

    val esDocClassName: String = typeaheadDef.esDocDef.dtoDef.uqcn.value
    val serviceClassName: String = typeaheadDef.angularServiceClassName
    val serviceFieldName: String = if (nameSuffix.isEmpty()) StringFunctions.firstToLower(serviceClassName) else "${uniqueFieldName}TypeaheadApiService"
    val searchTermFieldName: String = typeaheadDef.searchTermFieldName
    val esDocIdFieldName: String = typeaheadDef.esDocIdFieldName

    val serviceImport: TypescriptImport = typeaheadDef.typescriptServiceImport
    val esDocImport: TypescriptImport = typeaheadDef.esDocDef.dtoDef.typescriptDtoImport
    val joinRequestDtoTypescriptImport: TypescriptImport = joinRequestDtoDef.typescriptImport

    val labelText: String = "$uniqueDisplayName Entities"
    val searchPlaceholder: String = "Search $labelText..."
    val autocompleteRefName: String = "${uniqueFieldName}JoinEntityAuto"
    val displayWithMethodName: String = "display${uniqueFieldNameUpper}Entity"

}

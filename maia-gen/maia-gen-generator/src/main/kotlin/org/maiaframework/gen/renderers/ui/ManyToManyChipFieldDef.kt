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

    // Disambiguates UI-internal identifiers when entityDef has multiple many-to-many
    // associations whose other side resolves to the same field/display name (e.g. two
    // associations both pointing at a "right" entity). Empty for the first such association,
    // preserving existing naming for backwards compatibility.
    private val nameSuffix = manyToManyEntityDef.nameSuffixFor(entityDef)

    val fieldName: String = otherSide.fieldName
    val displayName: String = otherSide.displayName

    private val uniqueFieldName: String = "$fieldName$nameSuffix"
    private val uniqueDisplayName: String = "$displayName$nameSuffix"

    val labelText: String = "$uniqueDisplayName Entities"
    val searchPlaceholder: String = "Search $labelText..."

    val selectedFieldName: String = "selected${uniqueDisplayName}Entities"
    val filteredFieldName: String = "filtered${uniqueDisplayName}Entities"
    val filteredIsLoadingFieldName: String = "${filteredFieldName}IsLoading"
    val searchControlFieldName: String = "${uniqueFieldName}EntitySearchControl"
    val inputRefName: String = "${uniqueFieldName}EntityInput"
    val autocompleteRefName: String = "${uniqueFieldName}EntityAuto"
    val addMethodName: String = "add${uniqueDisplayName}Entity"
    val removeMethodName: String = "remove${uniqueDisplayName}Entity"
    val requestDtoFieldName: String = "${fieldName}EntityIds"
    val fetchForEditDtoFieldName: String = manyToManyEntityDef.fetchForEditFieldNameFor(entityDef)

    val esDocClassName: String = typeaheadDef.esDocDef.dtoDef.uqcn.value
    val serviceClassName: String = typeaheadDef.angularServiceClassName
    val serviceFieldName: String = if (nameSuffix.isEmpty()) StringFunctions.firstToLower(serviceClassName) else "${uniqueFieldName}TypeaheadApiService"
    val searchTermFieldName: String = typeaheadDef.searchTermFieldName
    val esDocIdFieldName: String = typeaheadDef.esDocIdFieldName

    val serviceImport: TypescriptImport = typeaheadDef.typescriptServiceImport
    val esDocImport: TypescriptImport = typeaheadDef.esDocDef.dtoDef.typescriptDtoImport

}

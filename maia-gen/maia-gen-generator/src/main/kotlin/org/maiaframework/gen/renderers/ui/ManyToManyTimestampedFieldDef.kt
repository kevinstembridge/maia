package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
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

    val isManagedBySystem: Boolean =
        manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM

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
    val filteredHasSearchedFieldName: String = "${filteredFieldName}HasSearched"
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

    // Signal forms: the joins are a signal of rows with a form over them, and a small form is used to add a join.
    val joinsFormFieldName: String = "${uniqueFieldName}JoinsForm"
    val addModelFieldName: String = "add${uniqueFieldNameUpper}JoinModel"
    val addFormFieldName: String = "add${uniqueFieldNameUpper}JoinForm"

    /**
     * Fields of the join entity beyond the standard id, entity id and effective range.
     * They are entered in the form that adds a join.
     */
    val extraJoinFields: List<ExtraJoinField> by lazy {

        val knownFieldNames = setOf("id", joinEntityIdFieldName, "effectiveFrom", "effectiveTo")

        joinRequestDtoDef.classFieldDefs
            .filterNot { it.classFieldName.value in knownFieldNames }
            .map { classFieldDef ->
                val isNumber = classFieldDef.fieldType is IntFieldType || classFieldDef.fieldType is IntTypeFieldType
                    || classFieldDef.fieldType is IntValueClassFieldType || classFieldDef.fieldType is LongFieldType
                    || classFieldDef.fieldType is LongTypeFieldType || classFieldDef.fieldType is DoubleFieldType
                ExtraJoinField(
                    name = classFieldDef.classFieldName.value,
                    label = StringFunctions.toTitleCase(classFieldDef.classFieldName.value),
                    isNumber = isNumber
                )
            }

    }

}


/** A field of a join entity that is entered when adding a join. Numbers are `number | null` so that an empty input is null. */
data class ExtraJoinField(val name: String, val label: String, val isNumber: Boolean) {

    val tsType: String = if (isNumber) "number | null" else "string"

    val emptyValue: String = if (isNumber) "null" else "''"

    val defaultValue: String = if (isNumber) "0" else "''"

    val htmlInputType: String = if (isNumber) "number" else "text"

}

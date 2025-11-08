package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.lang.text.StringFunctions

class ForeignKeyFieldDef(
    val foreignKeyFieldName: ClassFieldName,
    val foreignEntityDef: EntityDef,
    val typeaheadDef: TypeaheadDef?,
    val searchableDtoDef: SearchableDtoDef?,
    searchTermFieldName: String?
) {


    init {
        require(
            (typeaheadDef != null && searchableDtoDef != null) == false
        ) {
            "Only one of typeaheadDef and searchableDtoDef can be provided"
        }
    }


    val foreignEntityBaseName = foreignEntityDef.entityBaseName


    val formGroupFieldName = typeaheadDef?.typeaheadName?.firstToLower() ?: searchableDtoDef?.dtoBaseName?.firstToLower()


    val compareWithFunctionName = formGroupFieldName?.let { "compare${StringFunctions.firstToUpper(it)}" }


    val searchTermFieldName = typeaheadDef?.searchTermFieldName ?: searchTermFieldName


    val idFieldName = typeaheadDef?.idFieldName ?: "id"


}

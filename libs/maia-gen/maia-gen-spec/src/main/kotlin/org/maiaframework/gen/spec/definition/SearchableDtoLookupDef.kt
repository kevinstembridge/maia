package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.CaseSensitive
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName


class SearchableDtoLookupDef(
    val foreignKeyEntityDef: EntityDef,
    val localFieldClassFieldName: ClassFieldName,
    val foreignLookupFieldDef: EntityFieldDef,
    val lookupFieldDefs: List<SearchableDtoLookupFieldDef>
) {


    val allResponseDtoFields: List<ResponseDtoFieldDef> = lookupFieldDefs.map { toResponseDtoFieldDef(it) }


    private fun toResponseDtoFieldDef(lookupFieldDef: SearchableDtoLookupFieldDef): ResponseDtoFieldDef {

        val classFieldName = lookupFieldDef.dtoFieldName
        val foreignFieldDef = lookupFieldDef.foreignFieldDef
        val classFieldDef = foreignFieldDef.classFieldDef
        val fieldType = classFieldDef.fieldType

        return ResponseDtoFieldDef(
                classFieldName,
            TableColumnName(classFieldName.value),
                fieldType,
                classFieldDef.nullability,
                classFieldDef.isMasked,
                CaseSensitive.FALSE,
                lookupFieldDef.fieldReaderParameterizedType,
                lookupFieldDef.fieldWriterParameterizedType)

    }


}

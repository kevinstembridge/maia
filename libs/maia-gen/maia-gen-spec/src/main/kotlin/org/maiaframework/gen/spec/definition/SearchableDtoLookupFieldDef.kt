package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType

class SearchableDtoLookupFieldDef(
    val foreignFieldDef: EntityFieldDef,
    dtoFieldName: String?,
    val fieldReaderParameterizedType: ParameterizedType?,
    val fieldWriterParameterizedType: ParameterizedType?
) {

    val dtoFieldName = dtoFieldName?.let { ClassFieldName(it) } ?: foreignFieldDef.classFieldName

}

package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.spec.definition.jdbc.DbColumnFieldDef
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName

data class DocumentMapperFieldDef(
    val classFieldDef: ClassFieldDef,
    val tableColumnName: TableColumnName,
    val fieldReaderParameterizedType: ParameterizedType?,
    val fieldWriterParameterizedType: ParameterizedType?
) {


    val dbColumnFieldDef = DbColumnFieldDef(tableColumnName, fieldReaderParameterizedType, fieldWriterParameterizedType)


    val fieldReaderClassField: ClassFieldDef? = this.dbColumnFieldDef.fieldReaderClassField(classFieldDef.classFieldName)


    val fieldWriterClassField: ClassFieldDef? = this.dbColumnFieldDef.fieldWriterClassField(classFieldDef.classFieldName)


}

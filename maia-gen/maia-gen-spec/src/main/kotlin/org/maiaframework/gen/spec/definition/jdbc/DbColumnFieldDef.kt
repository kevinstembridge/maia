package org.maiaframework.gen.spec.definition.jdbc

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


data class DbColumnFieldDef(
    val tableColumnName: TableColumnName,
    val fieldReaderParameterizedType: ParameterizedType?,
    val fieldWriterParameterizedType: ParameterizedType?
) {


    private fun combinedClassAndTableColumnName(classFieldName: ClassFieldName): ClassFieldName {

        return classFieldName.withSuffix("_" + tableColumnName.toValidJavaIdentifier())

    }


    fun fieldReaderClassField(classFieldName: ClassFieldName): ClassFieldDef? = fieldReaderParameterizedType?.let { readerType ->

        val fieldReaderClassFieldName = combinedClassAndTableColumnName(classFieldName).withSuffix("_FieldReader")
        val fieldReaderFieldType = FieldTypes.byFqcn(readerType.fqcn)
        aClassField(fieldReaderClassFieldName, fieldReaderFieldType).build()

    }


    fun fieldWriterClassField(classFieldName: ClassFieldName): ClassFieldDef? = fieldWriterParameterizedType?.let { writerType ->

        val fieldWriterClassFieldName = combinedClassAndTableColumnName(classFieldName).withSuffix("_FieldWriter")
        val fieldWriterFieldType = FieldTypes.byFqcn(writerType.fqcn)
        aClassField(fieldWriterClassFieldName, fieldWriterFieldType).build()

    }


}

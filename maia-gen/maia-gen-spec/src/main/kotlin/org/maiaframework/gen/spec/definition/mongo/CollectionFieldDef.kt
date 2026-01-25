package org.maiaframework.gen.spec.definition.mongo

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ParameterizedType

data class CollectionFieldDef(
    val collectionFieldName: CollectionFieldName,
    val fieldReaderParameterizedType: ParameterizedType?,
    val fieldWriterParameterizedType: ParameterizedType?
) {


    private fun combinedClassAndCollectionFieldName(classFieldName: ClassFieldName): ClassFieldName {

        return classFieldName.withSuffix("_" + collectionFieldName.toValidJavaIdentifier())

    }


    fun fieldReaderClassField(classFieldName: ClassFieldName): ClassFieldDef? = fieldReaderParameterizedType?.let { readerType ->
        val fieldReaderClassFieldName = combinedClassAndCollectionFieldName(classFieldName).withSuffix("_FieldReader")
        val fieldReaderFieldType = FieldTypes.byFqcn(readerType.fqcn)
        aClassField(fieldReaderClassFieldName, fieldReaderFieldType).build()
    }


    fun fieldWriterClassField(classFieldName: ClassFieldName): ClassFieldDef? = fieldWriterParameterizedType?.let { writerType ->
        val fieldWriterClassFieldName = combinedClassAndCollectionFieldName(classFieldName).withSuffix("_FieldWriter")
        val fieldWriterFieldType = FieldTypes.byFqcn(writerType.fqcn)
        aClassField(fieldWriterClassFieldName, fieldWriterFieldType).build()
    }


}

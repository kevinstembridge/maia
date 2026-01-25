package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


class DataClassFieldDefBuilder(
    private val classFieldName: ClassFieldName,
    private val fieldType: FieldType,
    private val dataClassDefBuilder: DataClassDefBuilder,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {


    private var nullability = Nullability.NOT_NULLABLE
    private var isMasked = false
    private var fieldReaderParameterizedType: ParameterizedType? = null
    private var fieldWriterParameterizedType: ParameterizedType? = null
    private val annotationDefs = sortedSetOf<AnnotationDef>()


    private val fieldReaderClassName: ParameterizedType?
        get() = if (this.fieldReaderParameterizedType != null) {
            this.fieldReaderParameterizedType!!
        } else
            this.defaultFieldTypeFieldReaderProvider(this.fieldType)


    private val fieldWriterClassName: ParameterizedType?
        get() = if (this.fieldWriterParameterizedType != null) {
            this.fieldWriterParameterizedType
        } else
            this.defaultFieldTypeFieldWriterProvider(this.fieldType)


    constructor(
        classFieldName: ClassFieldName,
        enumDef: EnumDef,
        dataClassDefBuilder: DataClassDefBuilder,
        defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
        defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
    ) : this(
        classFieldName,
        FieldTypes.enum(enumDef),
        dataClassDefBuilder,
        defaultFieldTypeFieldReaderProvider,
        defaultFieldTypeFieldWriterProvider
    )


    fun nullable(): DataClassFieldDefBuilder {

        this.nullability = Nullability.NULLABLE
        return this

    }


    fun build(): DataClassFieldDef {

        val classFieldDef = ClassFieldDef(
            classFieldName = classFieldName,
            fieldType = fieldType,
            nullability = nullability,
            isMasked = isMasked,
            annotationDefs = annotationDefs
        )

        val fieldReaderClassName = fieldReaderClassName
        val fieldWriterClassName = fieldWriterClassName

        return DataClassFieldDef(
            classFieldDef,
            fieldReaderClassName,
            fieldWriterClassName
        )

    }


    fun fieldReader(fieldReaderFqcn: Fqcn): DataClassFieldDefBuilder {

        return fieldReader(ParameterizedType(fieldReaderFqcn))

    }


    fun fieldReader(fieldReaderParameterizedType: ParameterizedType): DataClassFieldDefBuilder {

        this.fieldReaderParameterizedType = fieldReaderParameterizedType
        return this

    }


    fun fieldWriter(fieldReaderFqcn: Fqcn): DataClassFieldDefBuilder {

        return fieldWriter(ParameterizedType(fieldReaderFqcn))

    }


    fun fieldWriter(fieldWriterParameterizedType: ParameterizedType): DataClassFieldDefBuilder {

        this.fieldWriterParameterizedType = fieldWriterParameterizedType
        return this

    }


    fun masked(): DataClassFieldDefBuilder {

        this.isMasked = true
        return this

    }


}

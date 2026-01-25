package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.PackageName


@MaiaDslMarker
class EsDocDefBuilder(
    private val packageName: PackageName,
    private val esDocBaseName: DtoBaseName,
    private val elasticIndexBaseName: ElasticIndexBaseName,
    private val esDocVersion: Int,
    private val indexDescription: Description,
    private val renderFieldEnum: Boolean,
    private val generateRefreshIndexJob: Boolean,
    private val disableRendering: Boolean
) {

    private val fieldBuilders = mutableListOf<EsDocFieldDefBuilder>()


    fun build(): EsDocDef {

        val fields = this.fieldBuilders.map { it.build() }

        return EsDocDef(
            this.packageName,
            this.esDocBaseName,
            this.elasticIndexBaseName,
            this.esDocVersion,
            this.indexDescription,
            fields,
            this.renderFieldEnum,
            this.generateRefreshIndexJob,
            this.disableRendering,
            entityDef = null
        )

    }


    fun field(
        fieldName: String,
        fieldType: FieldType,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = fieldType,
        )

        init?.invoke(fieldBuilder)

        this.fieldBuilders.add(fieldBuilder)

    }


    fun field(
        fieldName: String,
        valueClassDef: StringValueClassDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.stringValueClass(valueClassDef),
        )

        init?.invoke(fieldBuilder)

        this.fieldBuilders.add(fieldBuilder)

    }


    fun field(
        fieldName: String,
        esDocDef: EsDocDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.esDoc(esDocDef),
        )

        init?.invoke(fieldBuilder)
        this.fieldBuilders.add(fieldBuilder)

    }


    fun field(
        fieldName: String,
        enumDef: EnumDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.enum(enumDef),
        )

        init?.invoke(fieldBuilder)

        this.fieldBuilders.add(fieldBuilder)

    }


    fun field(
        fieldName: String,
        stringTypeDef: StringTypeDef,
        init: (EsDocFieldDefBuilder.() -> Unit)? = null
    ) {

        val fieldBuilder = EsDocFieldDefBuilder(
            fieldName = ClassFieldName(fieldName),
            fieldType = FieldTypes.stringType(stringTypeDef),
        )

        init?.invoke(fieldBuilder)

        this.fieldBuilders.add(fieldBuilder)

    }


}

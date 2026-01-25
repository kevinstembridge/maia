package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.FormModelClassName
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.FormModelKey
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import java.util.LinkedList
import java.util.Optional


@MaiaDslMarker
class FormModelDefBuilder(
    private val packageName: PackageName,
    private val formModelClassName: FormModelClassName
) {

    private val fieldDefBuilders = LinkedList<FormModelFieldDefBuilder>()
    private var formModelKey: FormModelKey? = null
    private var withPreAuthorizeOptional = Optional.empty<WithPreAuthorize>()


    fun build(): FormModelDef {

        val fieldDefs = buildFieldDefs()

        return FormModelDef(
            this.formModelKey ?: FormModelKey(this.formModelClassName.value),
            this.formModelClassName,
            this.packageName,
            fieldDefs,
            this.withPreAuthorizeOptional
        )

    }


    private fun buildFieldDefs(): List<ClassFieldDef> {

        return this.fieldDefBuilders.map { it.build() }.sorted()

    }


    fun formModelKey(formModelKey: String): FormModelDefBuilder {

        this.formModelKey = FormModelKey(formModelKey)
        return this

    }


    fun field(fieldName: String, fieldType: FieldType): FormModelFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), fieldType)

    }


    fun field(fieldName: String, enumDef: EnumDef): FormModelFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), enumDef)

    }


    private fun newFieldDefBuilder(classFieldName: ClassFieldName, fieldType: FieldType): FormModelFieldDefBuilder {

        return add(
            FormModelFieldDefBuilder(
                classFieldName,
                fieldType,
                this
            )
        )

    }


    private fun newFieldDefBuilder(classFieldName: ClassFieldName, stringTypeDef: StringTypeDef): FormModelFieldDefBuilder {

        return add(
            FormModelFieldDefBuilder(
                classFieldName,
                FieldTypes.stringType(stringTypeDef),
                this
            )
        )

    }


    private fun newFieldDefBuilder(classFieldName: ClassFieldName, enumDef: EnumDef): FormModelFieldDefBuilder {

        return add(
            FormModelFieldDefBuilder(
                classFieldName,
                enumDef,
                this
            )
        )


    }


    private fun add(builder: FormModelFieldDefBuilder): FormModelFieldDefBuilder {

        this.fieldDefBuilders.add(builder)
        return builder

    }


    fun field(fieldName: String, stringTypeDef: StringTypeDef): FormModelFieldDefBuilder {

        return newFieldDefBuilder(
            ClassFieldName(fieldName),
            stringTypeDef
        )

    }


    fun field(fieldName: String, listFieldType: ListFieldType): FormModelFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), listFieldType)

    }


    fun field(fieldName: String, mapFieldType: MapFieldType): FormModelFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), mapFieldType)

    }


    fun withPreAuthorize(expression: String): FormModelDefBuilder {

        this.withPreAuthorizeOptional = Optional.of(WithPreAuthorize(expression))
        return this

    }


}

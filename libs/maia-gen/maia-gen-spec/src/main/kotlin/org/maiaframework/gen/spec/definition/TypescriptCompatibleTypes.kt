package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldType


object TypescriptCompatibleTypes {


    val any = AnyTypescriptType()


    val boolean = BooleanTypescriptType()


    val enum = EnumTypescriptType()


    val number = NumberTypescriptType()


    val string = StringTypescriptType()


    val object_ = ObjectTypescriptType()


    fun record(keyType: FieldType, valueType: FieldType) = RecordTypescriptType(keyType, valueType)


    fun dto(fieldType: FieldType) = FieldTypeTypescriptCompatibleType(fieldType)


}

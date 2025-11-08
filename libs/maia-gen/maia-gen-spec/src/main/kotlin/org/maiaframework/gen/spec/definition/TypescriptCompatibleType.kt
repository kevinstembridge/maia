package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldType


sealed class TypescriptCompatibleType(val value: String) {


    override fun toString(): String = value


}


class AnyTypescriptType internal constructor() : TypescriptCompatibleType("any")


class BooleanTypescriptType internal constructor() : TypescriptCompatibleType("boolean")


class EnumTypescriptType internal constructor() : TypescriptCompatibleType("enum")


class NumberTypescriptType internal constructor() : TypescriptCompatibleType("number")


class ObjectTypescriptType internal constructor() : TypescriptCompatibleType("object")


class StringTypescriptType internal constructor() : TypescriptCompatibleType("string")


class RecordTypescriptType(keyType: FieldType, valueType: FieldType) : TypescriptCompatibleType("Record<${keyType.typescriptCompatibleType}, ${valueType.typescriptCompatibleType}>")


class ReadonlyArrayTypescriptType(type: FieldType) : TypescriptCompatibleType("ReadonlyArray<${type.typescriptCompatibleType}>")


class FieldTypeTypescriptCompatibleType(fieldType: FieldType) : TypescriptCompatibleType(fieldType.unqualifiedToString)


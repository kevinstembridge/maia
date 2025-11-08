package org.maiaframework.gen.renderers

import org.maiaframework.gen.persist.BsonCompatibleType
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.jdbc.DbColumnFieldDef
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.IdAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType


fun renderReadField(classFieldDef: ClassFieldDef, dbColumnFieldDef: DbColumnFieldDef, renderer: AbstractKotlinRenderer) {

    renderer.addImportFor(classFieldDef.fieldType)

    val unqualifiedToString = classFieldDef.unqualifiedToString
    val classFieldName = classFieldDef.classFieldName
    val tableColumnName = dbColumnFieldDef.tableColumnName

    val fieldReaderClassField = dbColumnFieldDef.fieldReaderClassField(classFieldName)

    if (fieldReaderClassField != null) {

        val fieldReaderClassFieldName = fieldReaderClassField.classFieldName
        renderer.appendLine("        val $classFieldName: $unqualifiedToString = this.$fieldReaderClassFieldName.readField(\"$tableColumnName\", \"$classFieldName\", document, collectionName)")

    } else {

        renderer.addImportFor(Fqcns.MAIA_DOCUMENT_FACADE)
        val firstParameterUqcn = TODO() // classFieldDef.fieldType.firstParameterTypeOrNull?.unqualifiedToString

        if (classFieldDef.isNullableEnum) {

            renderer.appendLine("        val $classFieldName: $unqualifiedToString = DocumentFacade.readEnumOrNull(${classFieldDef.fieldType.unqualifiedToString}::class.java, \"$tableColumnName\", this.collectionName, \"$classFieldName\", document)")

        } else if (classFieldDef.isEnumList) {

            renderer.appendLine("        val $classFieldName: $unqualifiedToString = DocumentFacade.readEnumList($firstParameterUqcn::class.java, \"$tableColumnName\", this.collectionName, \"$classFieldName\", document)")

        } else if (classFieldDef.isMap) {

            renderer.appendLine("        val $classFieldName: $unqualifiedToString = DocumentFacade.readMap(${renderMapKeyMapperInKotlin(classFieldDef)}, ${renderMapValueMapperInKotlin(classFieldDef)}, \"$tableColumnName\", this.collectionName, \"$classFieldName\", document)")

        } else {

            renderer.appendLine("        val $classFieldName: $unqualifiedToString = DocumentFacade.${getImplicitReadFieldMethodName(classFieldDef)}(${getReadFieldMapper(classFieldDef)}\"$tableColumnName\", this.collectionName, \"$classFieldName\", document)")

        }

    }

}


private fun getReadFieldMapper(classFieldDef: ClassFieldDef): String {

    val fieldType = classFieldDef.fieldType

    when (fieldType) {
        is BooleanFieldType -> TODO()
        is BooleanTypeFieldType -> TODO()
        is BooleanValueClassFieldType -> TODO()
        is DataClassFieldType -> TODO()
        is DomainIdFieldType -> TODO()
        is DoubleFieldType -> TODO()
        is EnumFieldType -> TODO()
        is EsDocFieldType -> TODO()
        is ForeignKeyFieldType -> TODO()
        is FqcnFieldType -> TODO()
        is IdAndNameFieldType -> TODO()
        is InstantFieldType -> TODO()
        is IntFieldType -> TODO()
        is IntTypeFieldType -> TODO()
        is IntValueClassFieldType -> TODO()
        is ListFieldType -> {

            val firstParameterType = fieldType.parameterFieldType

            if (firstParameterType is StringTypeFieldType) {

                return "{ v: ${firstParameterType.stringTypeDef.simpleTypeUnderlyingFieldType.unqualifiedToString} -> ${firstParameterType.unqualifiedToString}(v) }, "

            }

        }
        is LocalDateFieldType -> TODO()
        is LongFieldType -> TODO()
        is LongTypeFieldType -> TODO()
        is MapFieldType -> TODO()
        is ObjectIdFieldType -> TODO()
        is PeriodFieldType -> TODO()
        is RequestDtoFieldType -> TODO()
        is SetFieldType -> TODO()
        is SimpleResponseDtoFieldType -> TODO("YAGNI?")
        is StringFieldType -> TODO()
        is StringTypeFieldType -> TODO()
        is StringValueClassFieldType -> TODO()
        is UrlFieldType -> TODO()
    }


    if (classFieldDef.nullable) {

        if (fieldType is StringTypeFieldType) {
            return "{ ${fieldType.unqualifiedToString}(it) }, "
        }

        if (fieldType is EnumFieldType) {
            return fieldType.unqualifiedToString + "::class.java, "
        }

    }

    if (fieldType is StringTypeFieldType) {
        return "{ ${fieldType.unqualifiedToString}(it) }, "
    }

    return if (fieldType is EnumFieldType) {
        fieldType.unqualifiedToString + "::class.java, "
    } else {
        ""
    }

}


private fun getImplicitReadFieldMethodName(classFieldDef: ClassFieldDef): String {

    val listSuffix = if (classFieldDef.isList) "List" else ""
    val mapSuffix = if (classFieldDef.isMap) "Map" else ""
    val collectionSuffix = listSuffix + mapSuffix
    return getImplicitReadFieldMethodNamePrefix(classFieldDef) + collectionSuffix

}


private fun getImplicitReadFieldMethodNamePrefix(classFieldDef: ClassFieldDef): String {

    val fieldType = classFieldDef.fieldType

    if (fieldType is EnumFieldType) {
        return "readEnum"
    }

    if (fieldType is ListFieldType && fieldType.parameterFieldType is StringTypeFieldType) {
        return "read"
    }

    val bsonCompatibleType = fieldType.bsonCompatibleType ?: throw IllegalArgumentException("An implicit field type is expected to have a BsonType: $classFieldDef")

    val readMethod = when (bsonCompatibleType) {
        BsonCompatibleType.BOOLEAN -> "readBoolean"
        BsonCompatibleType.DOMAIN_ID -> "readDomainId"
        BsonCompatibleType.DOUBLE -> "readDouble"
        BsonCompatibleType.INSTANT -> "readInstant"
        BsonCompatibleType.INT -> "readInt"
        BsonCompatibleType.INTEGER -> "readInteger"
        BsonCompatibleType.LOCAL_DATE -> "readLocalDate"
        BsonCompatibleType.LONG -> "readLong"
        BsonCompatibleType.OBJECT_ID -> "readObjectId"
        BsonCompatibleType.DOCUMENT -> "readDocument"
        BsonCompatibleType.PERIOD -> "readPeriod"
        BsonCompatibleType.STRING -> "readString"
        else -> throw RuntimeException("Unable to find implicit field reader method for $classFieldDef")
    }

    val orNull = if (classFieldDef.nullable) "OrNull" else ""

    return "$readMethod$orNull"

}


private fun renderMapKeyMapper(classFieldDef: ClassFieldDef): String {

    val mapFieldType = classFieldDef.fieldType
    val keyType = TODO() // mapFieldType.firstParameterType

//    if (keyType is ParameterizedType) {
//
//        if (keyType.isSimpleTypeWrapper) {
//
//            return "${keyType.unqualifiedToString}::new"
//
//        }
//
//    } else if (keyType is EnumType) {
//
//        return "key -> ${keyType.unqualifiedToString}.valueOf(key)"
//
//    }

    return "key -> key"

}


private fun renderMapKeyMapperInKotlin(classFieldDef: ClassFieldDef): String {

//    val mapFieldType = classFieldDef.fieldType
//    val keyType = mapFieldType.firstParameterType
//
//    if (keyType is ParameterizedType) {
//
//        if (keyType.isSimpleTypeWrapper) {
//
//            return "{ v: ${keyType.simpleTypeUnderlyingFieldType?.unqualifiedToString} -> ${keyType.unqualifiedToString}(v) }"
//
//        }
//
//    } else if (keyType is EnumType) {
//
//        return "{ key -> ${keyType.unqualifiedToString}.valueOf(key) }"
//
//    }

    return "{ key -> key }"

}


private fun renderMapValueMapper(classFieldDef: ClassFieldDef): String {

//    val mapFieldType = classFieldDef.fieldType
//    val valueType = mapFieldType.secondParameterType
//
//    if (classFieldDef.simpleTypeDef != null) {
//
//        return "value -> new ${valueType.unqualifiedToString}((${classFieldDef.simpleTypeDef!!.superTypeFieldType.unqualifiedToString}) value)"
//
//    }

    return TODO() // "value -> (${valueType.unqualifiedToString}) value"

}


private fun renderMapValueMapperInKotlin(classFieldDef: ClassFieldDef): String {

//    val mapFieldType = classFieldDef.fieldType
//    val valueType = mapFieldType.secondParameterType
//
//    if (valueType.isSimpleTypeWrapper) {
//
//        return "{ v -> ${valueType.unqualifiedToString}(v as ${valueType.simpleTypeUnderlyingFieldType?.unqualifiedToString}) }"
//
//    }

    return TODO() // "{ value -> value as ${valueType.unqualifiedToString} }"

}


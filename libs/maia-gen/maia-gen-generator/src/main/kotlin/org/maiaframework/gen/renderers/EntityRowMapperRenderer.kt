package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
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

class EntityRowMapperRenderer(
    private val entityHierarchy: EntityHierarchy
): AbstractKotlinRenderer(
    entityHierarchy.entityDef.rowMapperClassDef
) {

    private val entityDef = entityHierarchy.entityDef

    init {

        if (entityHierarchy.requiresObjectMapper) {
            addConstructorArg(ClassFieldDef.aClassField("objectMapper", Fqcns.JACKSON_OBJECT_MAPPER).privat().build())
        }

    }


    override fun renderFunctions() {

        addImportFor(Fqcns.MAHANA_RESULT_SET_ADAPTER)

        blankLine()
        blankLine()
        appendLine("    override fun mapRow(rsa: ResultSetAdapter): ${entityDef.entityUqcn} {")
        blankLine()

        if (this.entityHierarchy.hasSubclasses()) {

            appendLine("        val typeDiscriminator = rsa.readString(\"type_discriminator\")")
            blankLine()
            appendLine("        return when (typeDiscriminator) {")

            this.entityHierarchy.concreteEntityDefs.forEach { entity ->

                addImportFor(entity.entityClassDef.fqcn)
                val typeDiscriminator = entity.typeDiscriminatorOrNull ?: throw RuntimeException("Expected entity to have a type discriminator: " + entity.entityBaseName)

                blankLine()
                appendLine("            \"$typeDiscriminator\" -> ${entity.entityUqcn.firstToLower()}From(rsa)")

            }

            blankLine()
            appendLine("            else -> throw RuntimeException(\"A record exists with id \" + rsa.readDomainId(\"id\") + \" but it has an unknown type discriminator: \" + typeDiscriminator)")
            blankLine()
            appendLine("        }")
            blankLine()
            appendLine("    }")

            this.entityHierarchy.concreteEntityDefs.forEach { this.renderFunction_entityFrom(it) }

        } else {

            this.entityDef.allEntityFieldsSorted.forEach { entityFieldDef ->

                // TODO uncomment all this??
                val foreignKeyFieldDef = entityFieldDef.foreignKeyFieldDef

//                if (foreignKeyFieldDef == null) {

                    renderRowMapperField(entityFieldDef, entityFieldDef.tableColumnName.value, indentSize = 8)

//                } else {
//
//                    val idAndNameDef = foreignKeyFieldDef.entityDef.entityIdAndNameDef
//                    val idEntityFieldDef = idAndNameDef.idEntityFieldDef
//
//                    val idResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Id"
//                    val nameResultSetFieldName = "${foreignKeyFieldDef.foreignKeyFieldName}Name"
//
//                    addImportFor(idAndNameDef.dtoDef.fqcn)
//
//                    appendLine("            ${idAndNameDef.dtoUqcn}(")
//                    renderRowMapperField(idEntityFieldDef, idResultSetFieldName, indent = 12)
//                    renderRowMapperField(idAndNameDef.nameEntityFieldDef, nameResultSetFieldName, indent = 12)
//                    appendLine("            ),")
//
//                }

            }

            renderCallToEntityConstructor(entityDef, "        ")
            blankLine()
            appendLine("    }")

        }

    }


    private fun renderFunction_entityFrom(entity: EntityDef) {

        addImportFor(entity.entityClassDef.fqcn)

        blankLine()
        blankLine()
        appendLine("    private fun ${entity.entityUqcn.firstToLower()}From(rsa: ResultSetAdapter): ${entity.entityUqcn} {")
        blankLine()

        entity.allEntityFieldsSorted.forEach { renderRowMapperField(it, it.tableColumnName.value, indentSize = 8) }

        renderCallToEntityConstructor(entity, "        ")
        blankLine()
        appendLine("    }")

    }


    private fun renderRowMapperField(
        entityFieldDef: EntityFieldDef,
        resultSetFieldName: String?,
        indentSize: Int = 0
    ) {

        val indentStr = "".padEnd(indentSize, ' ')

        val rsaGetterFunctionName = entityFieldDef.resultSetAdapterReadFunctionName()

        val resultSetColumnName = resultSetFieldName ?: entityFieldDef.tableColumnName
        val classFieldName = entityFieldDef.classFieldName
        val fieldType = entityFieldDef.fieldType

        // TODO refactor these renderFor... functions so that we're not passing in so many parameters
        when (fieldType) {
            is BooleanFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is BooleanTypeFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is BooleanValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is DataClassFieldType -> renderForJsonField(entityFieldDef, indentStr, classFieldName, resultSetColumnName)
            is DomainIdFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is DoubleFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is EnumFieldType -> renderForEnum(entityFieldDef, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is EsDocFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is ForeignKeyFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is FqcnFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is IdAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is IntFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is IntTypeFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is IntValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is ListFieldType -> renderForListField(entityFieldDef, fieldType, indentStr, classFieldName, resultSetColumnName)
            is LocalDateFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is LongFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is LongTypeFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is MapFieldType -> renderForJsonField(entityFieldDef, indentStr, classFieldName, resultSetColumnName)
            is ObjectIdFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is PeriodFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is RequestDtoFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is SetFieldType -> renderForJsonField(entityFieldDef, indentStr, classFieldName, resultSetColumnName)
            is SimpleResponseDtoFieldType -> renderForJsonField(entityFieldDef, indentStr, classFieldName, resultSetColumnName)
            is StringFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is StringTypeFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is StringValueClassFieldType -> renderForValueWrapper(fieldType, indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
            is UrlFieldType -> renderForPlainField(indentStr, classFieldName, rsaGetterFunctionName, resultSetColumnName)
        }

    }


    private fun renderForEnum(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        classFieldName: ClassFieldName,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any
    ) {

        addImportFor(entityFieldDef.fieldType)
        appendLine("${indentStr}val $classFieldName = rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\", ${entityFieldDef.fieldType.fqcn.uqcn}::class.java)")

    }

    private fun renderForPlainField(
        indentStr: String,
        classFieldName: ClassFieldName,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any
    ) {

        appendLine("${indentStr}val $classFieldName = rsa.$rsaGetterFunctionName(\"${resultSetColumnName}\")")

    }


    private fun renderForJsonField(
        entityFieldDef: EntityFieldDef,
        indentStr: String,
        classFieldName: ClassFieldName,
        resultSetColumnName: Any
    ) {

        addImportFor(Fqcns.JACKSON_TYPE_REFERENCE)
        addImportFor(entityFieldDef.fieldType)

        val nullableSuffix = if (entityFieldDef.nullable) "OrNull" else ""

        appendLine("${indentStr}val $classFieldName = rsa.readString$nullableSuffix(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) }")

    }


    private fun renderForListField(
        entityFieldDef: EntityFieldDef,
        fieldType: ListFieldType,
        indentStr: String,
        classFieldName: ClassFieldName,
        resultSetColumnName: Any
    ) {

        addImportFor(fieldType)

        val listElementFieldType = fieldType.parameterFieldType

        when (listElementFieldType) {
            is BooleanFieldType -> TODO("YAGNI?")
            is BooleanTypeFieldType -> TODO("YAGNI?")
            is BooleanValueClassFieldType -> TODO("YAGNI?")
            is DataClassFieldType -> renderForJsonField(entityFieldDef, indentStr, classFieldName, resultSetColumnName)
            is DomainIdFieldType -> TODO("YAGNI?")
            is DoubleFieldType -> TODO("YAGNI?")
            is EnumFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}.valueOf(it) }")
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> TODO("YAGNI?")
            is FqcnFieldType -> TODO("YAGNI?")
            is IdAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readListOfInstants(\"${resultSetColumnName}\")")
            is IntFieldType -> TODO("YAGNI?")
            is IntTypeFieldType -> TODO("YAGNI?")
            is IntValueClassFieldType -> TODO("YAGNI?")
            is ListFieldType -> TODO("YAGNI?")
            is LocalDateFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readListOfLocalDates(\"${resultSetColumnName}\")")
            is LongFieldType -> TODO("YAGNI?")
            is LongTypeFieldType -> TODO("YAGNI?")
            is MapFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readString(\"$resultSetColumnName\") { objectMapper.readValue(it, object : TypeReference<${entityFieldDef.classFieldDef.unqualifiedToString}>() {}) }")
            is ObjectIdFieldType -> TODO("YAGNI?")
            is PeriodFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readListOfStrings(\"${resultSetColumnName}\") { Period.parse(it) }")
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readListOfStrings(\"${resultSetColumnName}\")")
            is StringTypeFieldType -> appendLine("${indentStr}val $classFieldName = rsa.readListOfStrings(\"${resultSetColumnName}\") { ${listElementFieldType.fqcn.uqcn}(it) }")
            is StringValueClassFieldType -> TODO("YAGNI?")
            is UrlFieldType -> TODO("YAGNI?")
        }

    }


    private fun renderForValueWrapper(
        fieldType: FieldType,
        indentStr: String,
        classFieldName: ClassFieldName,
        rsaGetterFunctionName: String,
        resultSetColumnName: Any
    ) {

        addImportFor(fieldType)
        appendLine("${indentStr}val $classFieldName = rsa.${rsaGetterFunctionName}(\"${resultSetColumnName}\") { ${fieldType.uqcn}(it) }")

    }


    private fun renderCallToEntityConstructor(entity: EntityDef, indent: String) {

        blankLine()
        append(indent + "return ${entity.entityUqcn}(")

        val constructorArgs = entity
            .allEntityFieldsSorted
            .map { fieldDef -> "\n        " + indent + fieldDef.classFieldDef.classFieldName }
            .joinToString(",")

        append(constructorArgs)
        newLine()
        appendLine("        )")

    }

}

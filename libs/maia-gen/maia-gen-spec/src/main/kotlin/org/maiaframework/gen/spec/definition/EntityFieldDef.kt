package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.IsCreatableByUser
import org.maiaframework.gen.spec.definition.flags.IsDeltaField
import org.maiaframework.gen.spec.definition.flags.IsDeltaKey
import org.maiaframework.gen.spec.definition.flags.IsDerived
import org.maiaframework.gen.spec.definition.flags.IsPrimaryKey
import org.maiaframework.gen.spec.definition.jdbc.DbColumnFieldDef
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
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
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef
import java.util.Objects


class EntityFieldDef(
    val entityBaseName: EntityBaseName,
    packageName: PackageName,
    val classFieldDef: ClassFieldDef,
    providedTableColumnName: TableColumnName? = null,
    val withExistsEndpoint: Boolean = false,
    val isDeltaKey: IsDeltaKey = IsDeltaKey.FALSE,
    val isPrimaryKey: IsPrimaryKey = IsPrimaryKey.FALSE,
    val isDeltaField: IsDeltaField = IsDeltaField.FALSE,
    val isDerived: IsDerived = IsDerived.FALSE,
    val isCreatableByUser: IsCreatableByUser,
    val fieldReaderParameterizedType: ParameterizedType? = null,
    val fieldWriterParameterizedType: ParameterizedType? = null
) : Comparable<EntityFieldDef> {


    val classFieldName: ClassFieldName = classFieldDef.classFieldName


    val fieldType = classFieldDef.fieldType


    val typeaheadDef = classFieldDef.typeaheadDef ?: classFieldDef.foreignKeyFieldDef?.typeaheadDef


    val foreignKeyFieldDef = classFieldDef.foreignKeyFieldDef


    val nullable = classFieldDef.nullable


    val nullability = Nullability.of(nullable)


    val tableColumnName = providedTableColumnName ?: TableColumnName(classFieldDef.classFieldName.toSnakeCase())


    val dbColumnFieldDef = DbColumnFieldDef(tableColumnName, fieldReaderParameterizedType, fieldWriterParameterizedType)


    val key = EntityFieldKey(entityBaseName.toString() + "_" + classFieldName)


    val fieldReaderClassField: ClassFieldDef? = this.dbColumnFieldDef.fieldReaderClassField(classFieldName)


    val fieldWriterClassField: ClassFieldDef? = this.dbColumnFieldDef.fieldWriterClassField(classFieldName)


    val typeaheadRequiredValidatorFunctionName: String = "${this.key.firstToLower()}RequiredValidator"


    private val typeaheadRequiredValidatorFileName: String = "${key}RequiredValidator"


    val typeaheadRequiredValidatorFilePath: String = "app/gen-components/${packageName.asTypescriptDirs()}/$typeaheadRequiredValidatorFileName.ts"


    val typeaheadRequiredValidatorImportStatement: String = "import { $typeaheadRequiredValidatorFunctionName } from '@app/gen-components/${packageName.asTypescriptDirs()}/$typeaheadRequiredValidatorFileName';"


    val typeaheadRequiredValidatorTypescriptImport = TypescriptImport(typeaheadRequiredValidatorFunctionName, "@app/gen-components/${packageName.asTypescriptDirs()}/$typeaheadRequiredValidatorFileName")


    val isVersionField = this.classFieldDef.isVersionField


    init {

        if (isDeltaKey.value && isDeltaField.value) {
            throw RuntimeException("An entity field cannot be both a delta key and delta field at the same time. entityKey=$entityBaseName, fieldName=$classFieldName")
        }

    }


    fun hasValidationConstraint(type: Class<out AbstractValidationConstraintDef>): Boolean {

        return this.classFieldDef.hasValidationConstraint(type)

    }


    override fun compareTo(other: EntityFieldDef): Int {

        return this.classFieldDef.compareTo(other.classFieldDef)

    }


    fun resultSetAdapterReadFunctionName(): String {

        return resultSetAdapterReadFunctionName(this.nullable)

    }


    fun resultSetAdapterReadFunctionName(nullable: Boolean): String {

        return classFieldDef.resultSetAdapterReadFunctionName(nullable)

    }


    override fun toString(): String {

        return "${entityBaseName}:${classFieldName}"

    }


    fun filterModelHelperFunctionName(): String {

        val uqcn = this.classFieldDef.fieldType.fqcn.uqcn.value

        return when(this.classFieldDef.fieldType) {
            is BooleanFieldType -> TODO("YAGNI?")
            is BooleanTypeFieldType -> TODO("YAGNI?")
            is BooleanValueClassFieldType -> TODO("YAGNI?")
            is DataClassFieldType -> TODO("YAGNI?")
            is DomainIdFieldType -> "sqlParamStringFor"
            is DoubleFieldType -> TODO("YAGNI?")
            is EnumFieldType -> "sqlParamStringFor"
            is EsDocFieldType -> TODO("YAGNI?")
            is ForeignKeyFieldType -> TODO("YAGNI?")
            is FqcnFieldType -> TODO("YAGNI?")
            is IdAndNameFieldType -> TODO("YAGNI?")
            is InstantFieldType -> "sqlParam${uqcn}For"
            is IntFieldType -> TODO("YAGNI?")
            is IntTypeFieldType -> TODO("YAGNI?")
            is IntValueClassFieldType -> TODO("YAGNI?")
            is ListFieldType -> "sqlParam${uqcn}For"
            is LocalDateFieldType -> "sqlParam${uqcn}For"
            is LongFieldType -> TODO("YAGNI?")
            is LongTypeFieldType -> TODO("YAGNI?")
            is MapFieldType -> "sqlParam${uqcn}For"
            is ObjectIdFieldType -> "sqlParam${uqcn}For"
            is PeriodFieldType -> TODO("YAGNI?")
            is RequestDtoFieldType -> TODO("YAGNI?")
            is SetFieldType -> TODO("YAGNI?")
            is SimpleResponseDtoFieldType -> TODO("YAGNI?")
            is StringFieldType -> "sqlParamStringFor"
            is StringTypeFieldType -> TODO("YAGNI?")
            is StringValueClassFieldType -> TODO("YAGNI?")
            is UrlFieldType -> "sqlParamStringFor"
        }

    }


    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityFieldDef

        if (entityBaseName != other.entityBaseName) return false
        if (classFieldDef != other.classFieldDef) return false

        return true

    }


    override fun hashCode(): Int {

        return Objects.hash(this.entityBaseName, this.classFieldDef)

    }


}

package org.maiaframework.showcase

import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn


class MaiaShowcaseSpec : AbstractSpec(AppKey("maia")) {


    private val partySpec = MaiaShowcasePartySpec()


    val someEnumDef = enumDef("org.maiaframework.showcase.enums.SomeEnum") {
        value("OK")
        value("NOT_OK")
    }


    val someStringTypeDef = stringType("org.maiaframework.showcase.types", "SomeStringType") {
        alwaysLowerCase()
    }


    val someStringValueClassDef = stringValueClass("org.maiaframework.showcase.types", "SomeStringValueClass") {}


    val someProvidedStringTypeDef = stringType("org.maiaframework.showcase.types.SomeProvidedStringType") {
        provided()
    }


    val emailAddressStringType = stringType("org.maiaframework.domain.contact", "EmailAddress") {
        provided()
    }


    val someIntTypeDef = intType("org.maiaframework.showcase.types", "SomeIntType")


    val someProvidedIntTypeDef = intType("org.maiaframework.showcase.types.SomeProvidedIntType") {
        provided()
    }


    val someLongTypeDef = longType("org.maiaframework.showcase.types", "SomeLongType")


    val someProvidedLongTypeDef = longType("org.maiaframework.showcase.types.SomeProvidedLongType") {
        provided()
    }


    val someBooleanTypeDef = booleanType("org.maiaframework.showcase.types", "SomeBooleanType")


    val someProvidedBooleanTypeDef = booleanType("org.maiaframework.showcase.types.SomeProvidedBooleanType") {
        provided()
    }


    val simpleResponseDtoDef = simpleResponseDto("org.maiaframework.showcase", "Simple") {
        field("someString", FieldTypes.string)
        field("someInt", FieldTypes.int) { nullable() }
        field("someBoolean", FieldTypes.boolean) { nullable() }
    }


    val allFieldTypesEntityDef = entity(
        "org.maiaframework.showcase.all_field_types",
        "AllFieldTypes",
        deletable = Deletable.TRUE,
        allowFindAll = AllowFindAll.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        field("someBoolean", FieldTypes.boolean)
        field("someBooleanNullable", FieldTypes.boolean) {
            nullable()
        }
        field("someBooleanType", someBooleanTypeDef)
        field("someBooleanTypeNullable", someBooleanTypeDef) {
            nullable()
        }
        field("someBooleanTypeProvided", someProvidedBooleanTypeDef)
        field("someBooleanTypeProvidedNullable", someProvidedBooleanTypeDef) {
            nullable()
        }
        field("someInstant", FieldTypes.instant)
        field("someInstantNullable", FieldTypes.instant) {
            nullable()
        }
        field("someInstantModifiable", FieldTypes.instant) {
            editableByUser()
        }
        field("someInstantModifiableNullable", FieldTypes.instant) {
            editableByUser()
            nullable()
        }
        field("someInt", FieldTypes.int)
        field("someIntModifiable", FieldTypes.int) {
            editableByUser()
        }
        field("someIntNullable", FieldTypes.int) {
            editableByUser()
            nullable()
        }
        field("someIntType", someIntTypeDef) {
            unique()
        }
        field("someIntTypeNullable", someIntTypeDef) {
            nullable()
        }
        field("someIntTypeProvided", someProvidedIntTypeDef)
        field("someIntTypeProvidedNullable", someProvidedIntTypeDef) {
            nullable()
        }
        field("someLongType", someLongTypeDef) {
            unique()
        }
        field("someLongTypeNullable", someLongTypeDef) {
            nullable()
        }
        field("someLongTypeProvided", someProvidedLongTypeDef)
        field("someLongTypeProvidedNullable", someProvidedLongTypeDef) {
            nullable()
        }
        field("someLocalDateModifiable", FieldTypes.localDate) {
            editableByUser()
        }
        field("somePeriodModifiable", FieldTypes.period) {
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("somePeriodNullable", FieldTypes.period) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someEnum", someEnumDef) {
            lengthConstraint(max = 100)
        }
        field("someEnumNullable", someEnumDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someString", FieldTypes.string) {
            unique()
            lengthConstraint(max = 100)
        }
        field("someStringModifiable", FieldTypes.string) {
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("someStringNullable", FieldTypes.string) {
            nullable()
            unique()
            lengthConstraint(max = 100)
        }
        field("someStringType", someStringTypeDef) {
            unique()
            lengthConstraint(max = 100)
        }
        field("someStringTypeNullable", someStringTypeDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someProvidedStringType", someProvidedStringTypeDef) {
            lengthConstraint(max = 100)
        }
        field("someProvidedStringTypeNullable", someProvidedStringTypeDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someListOfEnums", fieldListOf(someEnumDef))
        field("someListOfInstants", fieldListOf(FieldTypes.instant))
        field("someListOfLocalDates", fieldListOf(FieldTypes.localDate))
        field("someListOfPeriods", fieldListOf(FieldTypes.period))
        field("someListOfStrings", fieldListOf(FieldTypes.string)) {
            editableByUser()
        }
        field("someListOfStringTypes", fieldListOf(someStringTypeDef))
        field("someMapOfStringToInteger", mapOfString().to(Fqcn.INT))
        field("someMapOfStringTypeToStringType", fieldMapOf(someStringTypeDef).to(someStringTypeDef))
        field("someDto", simpleResponseDtoDef)
        field("someDtoNullable", simpleResponseDtoDef) { nullable() }
        field_createdById(partySpec.partyEntityDef)
        field_createdByName()
        field_lastModifiedById(partySpec.partyEntityDef)
        field_lastModifiedByName()
        field_lastModifiedTimestampUtc()
        index {
            withFieldAscending("someStringModifiable")
            withFieldAscending("someBoolean")
        }
        crud {
            apis(defaultAuthority = partySpec.adminAuthority) {
                create()
                update()
                delete()
            }
        }
    }


    val simpleEntityDef = entity(
        "org.maiaframework.showcase.simple",
        "Simple"
    ) {
        cacheable {  }
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
            editableByUser()
            unique()
        }
    }


    val effectiveTimestampEntityDef = entity(
        "org.maiaframework.showcase.effective_dated",
        "EffectiveTimestamp",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        withEffectiveTimestamps()
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        index {
            withFieldAscending("someString")
        }
    }


    val historySampleEntityDef = entity(
        "org.maiaframework.showcase.history",
        "HistorySample",
        recordVersionHistory = true,
        deletable = Deletable.TRUE
    ) {
        field("someString", FieldTypes.string) {
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int) {
            editableByUser()
        }
        field_createdById(partySpec.partyEntityDef)
        field_lastModifiedById(partySpec.partyEntityDef)
        field_lastModifiedTimestampUtc()
        index {
            unique()
            withFieldAscending("someString")
        }
    }


    val historySuperEntityDef = entity(
        "org.maiaframework.showcase.history",
        "HistorySuper",
        recordVersionHistory = true,
        deletable = Deletable.TRUE
    ) {
        isAbstract = true
        field_createdById(partySpec.partyEntityDef)
        field_lastModifiedById(partySpec.partyEntityDef)
        field_lastModifiedTimestampUtc()
    }


    val historySubOneEntityDef = entity(
        "org.maiaframework.showcase.history",
        "HistorySubOne",
        recordVersionHistory = true,
        deletable = Deletable.TRUE
    ) {
        superclass(historySuperEntityDef)
        typeDiscriminator("SUB1")
        field("someString", FieldTypes.string) {
            editableByUser()
            lengthConstraint(max = 100)
        }
    }


    val historySubTwoEntityDef = entity(
        "org.maiaframework.showcase.history",
        "HistorySubTwo",
        recordVersionHistory = true,
        deletable = Deletable.TRUE
    ) {
        superclass(historySuperEntityDef)
        typeDiscriminator("SUB2")
        field("someInt", FieldTypes.int) {
            editableByUser()
        }
    }


}

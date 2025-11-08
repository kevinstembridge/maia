package org.maiaframework.gen.sample

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SampleMongoSpec : AbstractSpec(appKey = AppKey("testing"), defaultSchemaName = SchemaName("testing"), defaultDatabaseType = DatabaseType.MONGO) {


    val someEnumDef = enumDef("org.maiaframework.gen.testing.mongo.sample.simple.SomeEnum") {
        value("OK")
        value("NOT_OK")
    }


    val someStringTypeDef = stringType("org.maiaframework.gen.testing.mongo.sample.types", "SomeStringType") {
        alwaysLowerCase()
    }


    val firstNameStringTypeDef = stringType("org.maiaframework.gen.testing.mongo.sample.types", "FirstName")


    val lastNameStringTypeDef = stringType("org.maiaframework.gen.testing.mongo.sample.types", "LastName")


    val someStringTypeProvidedDef = stringType("org.maiaframework.gen.sample.types.SomeStringTypeProvided") {
        provided()
    }


    val emailAddressStringType = stringType("org.maiaframework.gen.testing.mongo.sample.contact", "EmailAddress") {
        alwaysLowerCase()
    }


    val someIntTypeDef = intType("org.maiaframework.gen.testing.mongo.sample.types", "SomeIntType")


    val someProvidedIntTypeDef = intType("org.maiaframework.gen.sample.types.SomeProvidedIntType") {
        provided()
    }

    val someLongTypeDef = longType("org.maiaframework.gen.testing.mongo.sample.types", "SomeLongType")


    val someProvidedLongTypeDef = longType("org.maiaframework.gen.sample.types.SomeProvidedLongType") {
        provided()
    }


    val someBooleanTypeDef = booleanType("org.maiaframework.gen.testing.mongo.sample.types", "SomeBooleanType")


    val someProvidedBooleanTypeDef = booleanType("org.maiaframework.gen.sample.types.SomeProvidedBooleanType") {
        provided()
    }


    val someDataClassDef = dataClass("org.maiaframework.gen.testing.mongo.sample.dataclasses", "SomeDataClass") {
        field("someStringField", FieldTypes.string)
        field("someOptionalBooleanField", FieldTypes.boolean).nullable()
    }


    val simpleEntityDef = entity(
        "org.maiaframework.gen.testing.mongo.sample.simple",
        "Simple",
        deletable = Deletable.TRUE
    ) {
            field("someBoolean", FieldTypes.boolean) {
                tableColumnName("sb")
            }
            field("someBooleanNullable", FieldTypes.boolean) {
                tableColumnName("sbn")
                nullable()
            }
            field("someBooleanType", someBooleanTypeDef) {
                tableColumnName("sbt")
            }
            field("someBooleanTypeNullable", someBooleanTypeDef) {
                tableColumnName("sbtyp_n")
                nullable()
            }
            field("someBooleanTypeProvided", someProvidedBooleanTypeDef) {
                tableColumnName("sbtp")
            }
            field("someBooleanTypeProvidedNullable", someProvidedBooleanTypeDef) {
                tableColumnName("sbt_pn")
                nullable()
            }
            field("someInstant", FieldTypes.instant) {
                tableColumnName("si")
            }
            field("someInstantNullable", FieldTypes.instant) {
                tableColumnName("si_n")
                nullable()
            }
            field("someInstantModifiable", FieldTypes.instant) {
                tableColumnName("si_m")
                modifiableBySystem()
            }
            field("someInstantModifiableNullable", FieldTypes.instant) {
                tableColumnName("si_mn")
                modifiableBySystem()
                nullable()
            }
            field("someInt", FieldTypes.int) {
                tableColumnName("sint")
            }
            field("someIntModifiable", FieldTypes.int) {
                tableColumnName("sint_m")
                modifiableBySystem()
            }
            field("someIntNullable", FieldTypes.int) {
                tableColumnName("sint_n")
                modifiableBySystem()
                nullable()
            }
            field("someIntType", someIntTypeDef) {
                tableColumnName("sintt")
                unique()
            }
            field("someIntTypeNullable", someIntTypeDef){
                tableColumnName("sit_n")
                nullable()
            }
            field("someIntTypeProvided", someProvidedIntTypeDef) {
                tableColumnName("sintt_p")
            }
            field("someIntTypeProvidedNullable", someProvidedIntTypeDef) {
                tableColumnName("spit_n")
                nullable()
            }
            field("someLongType", someLongTypeDef) {
                tableColumnName("slt")
                unique()
            }
            field("someLongTypeNullable", someLongTypeDef) {
                tableColumnName("slt_n")
                nullable()
            }
            field("someLongTypeProvided", someProvidedLongTypeDef) {
                tableColumnName("slt_p")
            }
            field("someLongTypeProvidedNullable", someProvidedLongTypeDef) {
                tableColumnName("slt_pn")
                nullable()
            }
            field("someLocalDateModifiable", FieldTypes.localDate) {
                tableColumnName("smld")
                modifiableBySystem()
            }
            field("somePeriodModifiable", FieldTypes.period) {
                tableColumnName("sp_m")
                modifiableBySystem()
            }
            field("somePeriodNullable", FieldTypes.period) {
                tableColumnName("sp_n")
                nullable()
            }
            field("someEnum", someEnumDef) {
                tableColumnName("someEnum")
            }
            field("someEnumNullable", someEnumDef) {
                tableColumnName("someEnum_n")
                nullable()
            }
            field("someString", FieldTypes.string) {
                tableColumnName("ss")
                unique()
            }
            field("someStringModifiable", FieldTypes.string) {
                tableColumnName("ss_m")
                modifiableBySystem()
            }
            field("someStringNullable", FieldTypes.string) {
                tableColumnName("ss_n")
                nullable()
                unique()
            }
            field("someStringType", someStringTypeDef) {
                tableColumnName("sst")
                unique()
            }
            field("someStringTypeNullable", someStringTypeDef) {
                tableColumnName("sst_n")
                nullable()
            }
            field("someStringTypeProvided", someStringTypeProvidedDef) {
                tableColumnName("sst_p")
            }
            field("someStringTypeProvidedNullable", someStringTypeProvidedDef) {
                tableColumnName("sst_pn")
                nullable()
            }
            field("someListOfEnums", fieldListOf(someEnumDef)) {
                tableColumnName("lostat")
            }
            field("someListOfInstants", fieldListOf(FieldTypes.instant)) {
                tableColumnName("loi")
            }
            field("someListOfLocalDates", fieldListOf(FieldTypes.localDate)) {
                tableColumnName("lold")
            }
            field("someListOfPeriods", fieldListOf(FieldTypes.period)) {
                tableColumnName("lop")
            }
            field("someListOfStrings", fieldListOf(FieldTypes.string)) {
                tableColumnName("los")
            }
            field("someListOfStringTypes", fieldListOf(someStringTypeDef)) {
                tableColumnName("lost")
            }
            field("someMapOfStringToInteger", mapOfString().to(Fqcn.INT)) {
                tableColumnName("mosti")
            }
            field("someMapOfStringTypeToStringType", fieldMapOf(someStringTypeDef).to(someStringTypeDef)) {
                tableColumnName("mosttst")
            }
            index {
                withFieldAscending("someStringModifiable")
                withFieldAscending("someBoolean")
            }
    }


    val nullableFieldsEntityDef = entity(
        "org.maiaframework.gen.testing.mongo.sample.nullable",
        "NullableFields"
    ) {
        field("someString", FieldTypes.string) {
            tableColumnName("sns")
            nullable()
            unique()
        }
        field("someBoolean", FieldTypes.boolean) {
            tableColumnName("sb")
            nullable()
        }
        field("someInt", FieldTypes.int) {
            tableColumnName("soint")
            nullable()
        }
        field("someInstant", FieldTypes.instant) {
            tableColumnName("soi")
            nullable()
        }
        field("somePeriod", FieldTypes.period) {
            tableColumnName("sop")
            nullable()
        }
        field("someLocalDate", FieldTypes.localDate) {
            tableColumnName("smld")
            nullable()
        }
        field("someEnum", someEnumDef) {
            tableColumnName("ss")
            nullable()
        }
        field("someStringType", someStringTypeDef) {
            tableColumnName("sst")
            nullable()
        }
        field("someProvidedStringType", someStringTypeProvidedDef) {
            tableColumnName("spst")
            nullable()
        }
        field("someIntType", someIntTypeDef) {
            tableColumnName("sit")
            nullable()
        }
        field("someProvidedIntType", someProvidedIntTypeDef) {
            tableColumnName("spit")
            nullable()
        }
        field("someLongType", someLongTypeDef) {
            tableColumnName("slt")
            nullable()
        }
        field("someProvidedLongType", someProvidedLongTypeDef) {
            tableColumnName("splt")
            nullable()
        }
        field("someBooleanType", someBooleanTypeDef) {
            tableColumnName("sbt")
            nullable()
        }
        field("someProvidedBooleanType", someProvidedBooleanTypeDef) {
            tableColumnName("spbt")
            nullable()
        }
    }


    val partyEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.party", "Party") {
        isAbstract = true
        typeDiscriminator("PA")
        field("emailAddress", emailAddressStringType) {
            tableColumnName("em")
        }
    }


    val organizationEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.org", "Organization") {
        superclass(partyEntityDef)
        typeDiscriminator("ORG")
        field("name", FieldTypes.string) {
            tableColumnName("n")
            modifiableBySystem()
        }
    }


    val personEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.person", "Person") {
        superclass(partyEntityDef)
        typeDiscriminator("PE")
        field("firstName", firstNameStringTypeDef) {
            tableColumnName("fn")
            nullable()
            modifiableBySystem()
        }
        field("lastName", lastNameStringTypeDef) {
            tableColumnName("ln")
            modifiableBySystem()
        }
        field("amount", FieldTypes.double) {
            tableColumnName("amt")
            nullable()
        }
    }


    val userEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.user", "User") {
        superclass(personEntityDef)
        typeDiscriminator("USR")
        field("encryptedPassword", FieldTypes.string) {
            tableColumnName("ep")
            modifiableBySystem()
            masked()
        }
    }


    val ttlEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.ttl", "Ttl") {
        field("createdAt", FieldTypes.instant) {
            tableColumnName("ca")
        }
        index {
            withFieldAscending("createdTimestampUtc").withExpireAfter(24, TimeUnit.HOURS)
        }
    }


    val historySampleEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.history", "HistorySample",
            recordVersionHistory = true
    ) {
        field("someString", FieldTypes.string) {
            tableColumnName("someString")
            modifiableBySystem()
        }
        field("someInt", FieldTypes.int) {
            tableColumnName("someInt")
            modifiableBySystem()
        }
        index {
            unique()
            withFieldAscending("someString")
        }
    }


    val someVersionedEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.versioned", "SomeVersioned",
            versioned = true
    ) {
        field("someString", FieldTypes.string) {
            tableColumnName("someString")
            modifiableBySystem()
        }
        field("someInt", FieldTypes.int) {
            tableColumnName("someInt")
            modifiableBySystem()
        }
        field_lastModifiedTimestampUtc()
        index {
            unique().withFieldAscending("someInt")
        }
    }


    val someCaseSensitivityEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.case_sensitivity", "SomeCaseSensitivity") {
        field("caseSensitiveString", FieldTypes.string)
        field("caseInsensitiveString", FieldTypes.string)
    }


    val withOptionalIndexFieldEntityDef = entity("org.maiaframework.gen.testing.mongo.sample", "WithOptionalIndexField") {
        field("someOptionalString1", someStringTypeDef) {
            tableColumnName("sos1")
            nullable()
        }
        field("someOptionalString2", someStringTypeDef) {
            tableColumnName("sos2")
            nullable()
        }
        field("someString", someStringTypeDef) {
            tableColumnName("ss")
        }
        index {
            withFieldAscending("someOptionalString1")
        }
        index {
            withFieldAscending("someOptionalString2")
            withFieldAscending("someString")
        }
    }


    val verySimpleEntityDef = entity("org.maiaframework.gen.testing.mongo.sample.simple", "VerySimple") {
        field("someString", FieldTypes.string)
    }


    val personSummarySearchableDef = searchableEntityDef(
        "org.maiaframework.gen.testing.mongo.sample.person",
        "PersonSummary",
        entityDef = personEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        withPreAuthorize("hasAuthority('ROLE_ADMIN')")
        field("id", caseSensitive = true)
        field("emailAddress", caseSensitive = true)
        field("firstName", caseSensitive = true)
        field("lastName", caseSensitive = true)
        field("createdTimestampUtc", caseSensitive = true)
        field("amount", caseSensitive = true)
    }


    val someCaseSensitivitySearchableDef = searchableEntityDef(
        "org.maiaframework.gen.testing.mongo.sample.case_sensitivity",
        "SomeCaseSensitivity",
        entityDef = someCaseSensitivityEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        withPreAuthorize("hasAuthority('ROLE_ADMIN')")
        field("id")
        field("caseSensitiveString", caseSensitive = true)
        field("caseInsensitiveString", caseSensitive = false)
        field("createdTimestampUtc")
    }


    val verySimpleSearchableDef = searchableEntityDef(
        "org.maiaframework.gen.testing.mongo.sample.simple",
        "VerySimple",
        entityDef = verySimpleEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someString")
    }


    val foreignKeyParentEntityDef = entity("org.maiaframework.gen.testing.mongo.sample", "ForeignKeyParent") {
        field("someString", FieldTypes.string)
        field("someInt", FieldTypes.int)
    }


    val foreignKeyChildEntityDef = entity("org.maiaframework.gen.testing.mongo.sample", "ForeignKeyChild") {
        field("parentId", FieldTypes.domainId)
        field("someString", FieldTypes.string)
    }


    val foreignKeyChildSummarySearchableDef = searchableEntityDef(
        "org.maiaframework.gen.testing.mongo.sample.summary_dto",
        "ForeignKeyChildSummary",
        entityDef = foreignKeyChildEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("id")
        field("createdTimestampUtc")
        field("someString")
        field("parentId")
        lookup(foreignKeyParentEntityDef, localField = "parentId")
            .lookupField("someString").asDtoField("someForeignString").and()
            .lookupField("someInt").asDtoField("someForeignInt").and()
    }


}

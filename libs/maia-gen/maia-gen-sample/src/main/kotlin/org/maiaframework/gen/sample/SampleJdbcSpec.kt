package org.maiaframework.gen.sample

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.JoinType
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import java.util.concurrent.TimeUnit

@Suppress("unused", "MemberVisibilityCanBePrivate")
class SampleJdbcSpec : AbstractSpec(appKey = AppKey("testing"), defaultSchemaName = SchemaName("testing")) {


    private val partySpec = SamplePartyJdbcSpec()


    val someEnumDef = enumDef("org.maiaframework.gen.testing.jdbc.sample.simple.SomeEnum") {
        value("OK")
        value("NOT_OK")
    }


    val someStringTypeDef = stringType("org.maiaframework.gen.sample.types", "SomeStringType") {
        alwaysLowerCase()
    }


    val someProvidedStringTypeDef = stringType("org.maiaframework.gen.sample.types.SomeProvidedStringType") {
        provided()
    }


    val emailAddressStringType = stringType("org.maiaframework.domain.contact", "EmailAddress") {
        provided()
    }


    val someIntTypeDef = intType("org.maiaframework.gen.testing.jdbc.sample.types", "SomeIntType")


    val someProvidedIntTypeDef = intType("org.maiaframework.gen.sample.types.SomeProvidedIntType") {
        provided()
    }


    val someLongTypeDef = longType("org.maiaframework.gen.testing.jdbc.sample.types", "SomeLongType")


    val someProvidedLongTypeDef = longType("org.maiaframework.gen.sample.types.SomeProvidedLongType") {
        provided()
    }


    val someBooleanTypeDef = booleanType("org.maiaframework.gen.testing.jdbc.sample.types", "SomeBooleanType")


    val someProvidedBooleanTypeDef = booleanType("org.maiaframework.gen.sample.types.SomeProvidedBooleanType") {
        provided()
    }


    val someDataClass = dataClass(
        "org.maiaframework.gen.testing.jdbc.sample.dataclasses",
        "SomeDataClass"
    ) {
        field("someStringField", FieldTypes.string)
        field("someOptionalBooleanField", FieldTypes.boolean).nullable()
    }


    val simpleResponseDto = simpleResponseDto("org.maiaframework.gen.testing.jdbc.sample", "Simple") {
        field("someString", FieldTypes.string)
        field("someInt", FieldTypes.int) { nullable() }
        field("someBoolean", FieldTypes.boolean) { nullable() }
    }


    val nullableEntityFieldsEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.nullable",
        "NullableFields"
    ) {
        field("someString", FieldTypes.string) {
            nullable()
            unique()
            lengthConstraint(max = 100)
        }
        field("someBoolean", FieldTypes.boolean) {
            nullable()
        }
        field("someInt", FieldTypes.int) {
            nullable()
        }
        field("someInstant", FieldTypes.instant) {
            nullable()
        }
        field("somePeriod", FieldTypes.period) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someLocalDate", FieldTypes.localDate) {
            nullable()
        }
        field("someEnum", someEnumDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someStringType", someStringTypeDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someProvidedStringType", someProvidedStringTypeDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someIntType", someIntTypeDef) {
            nullable()
        }
        field("someProvidedIntType", someProvidedIntTypeDef) {
            nullable()
        }
        field("someLongType", someLongTypeDef) {
            nullable()
        }
        field("someProvidedLongType", someProvidedLongTypeDef) {
            nullable()
        }
        field("someBooleanType", someBooleanTypeDef) {
            nullable()
        }
        field("someProvidedBooleanType", someProvidedBooleanTypeDef) {
            nullable()
        }
    }


    val effectiveTimestampEntity = entity(
        "org.maiaframework.gen.testing.jdbc.sample",
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


    val simpleEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.simple",
        "Simple",
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
        field("someDto", simpleResponseDto)
        field("someDtoNullable", simpleResponseDto) { nullable() }
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


    val ttlEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.ttl",
        "Ttl"
    ) {
        field("createdAt", FieldTypes.instant)
        index {
            withFieldAscending("createdTimestampUtc").withExpireAfter(24, TimeUnit.HOURS)
        }
    }


    val historySampleEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.history",
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


    val superEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.suuper",
        "Super",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        isAbstract = true
        field_createdById(partySpec.partyEntityDef)
        field_lastModifiedById(partySpec.partyEntityDef)
        field_lastModifiedTimestampUtc()
    }


    val subOneEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.suuper",
        "SubOne",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        superclass(superEntityDef)
        typeDiscriminator("SUB1")
        field("someString", FieldTypes.string) {
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("someUniqueString", FieldTypes.string) {
            unique()
            editableByUser()
            lengthConstraint(max = 100)
        }
    }


    val subTwoEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.suuper",
        "SubTwo",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        superclass(superEntityDef)
        typeDiscriminator("SUB2")
        field("someInt", FieldTypes.int) {
            editableByUser()
        }
        field("someUniqueString", FieldTypes.string) {
            unique()
            editableByUser()
            lengthConstraint(max = 100)
        }
    }


    val historySuperEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.history",
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
        "org.maiaframework.gen.testing.jdbc.sample.history",
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
        "org.maiaframework.gen.testing.jdbc.sample.history",
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


    val someVersionedEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.versioned",
        "SomeVersioned",
        versioned = true
    ) {
        field("someString", FieldTypes.string) {
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int) {
            editableByUser()
        }
        index {
            unique()
            withFieldAscending("someInt")
        }
    }


    val withOptionalIndexFieldEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample",
        "WithOptionalIndexField"
    ) {
        field("someOptionalString1", someStringTypeDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someOptionalString2", someStringTypeDef) {
            nullable()
            lengthConstraint(max = 100)
        }
        field("someString", someStringTypeDef) {
            lengthConstraint(max = 100)
        }
        index {
            withFieldAscending("someOptionalString1")
        }
        index {
            withFieldAscending("someOptionalString2")
            withFieldAscending("someString")
        }
    }


    val verySimpleEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.simple",
        "VerySimple"
    ) {
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


//        searchableEntityDef("org.maiaframework.gen.testing.jdbc.sample", "SomeCaseSensitivity", entityDef = someCaseSensitivityEntityDef, generateDto = WithGeneratedDto.TRUE)
//                .withPreAuthorize("hasAuthority('ROLE_ADMIN')")
//                .field("id").and()
//                .field("caseSensitiveString", caseSensitive = true).and()
//                .field("caseInsensitiveString", caseSensitive = false).and()
//                .field("createdTimestampUtc").and()
//                .build()


    // TODO uncomment and implement
//    val verySimpleSearchableDef = searchableEntityDef("org.maiaframework.gen.testing.jdbc.sample.simple", "VerySimple", entityDef = verySimpleEntityDef, generateDto = WithGeneratedDto.TRUE)
//            .field("someString").and()
//            .build()


    val foreignKeyParentEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample",
        "ForeignKeyParent"
    ) {
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int)
    }


    val foreignKeyEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample",
        "ForeignKeyChild"
    ) {
        field("parentId", FieldTypes.domainId)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    // TODO uncomment and implement
//    val foreignKeyChildSummarySearchableDef = searchableEntityDef("org.maiaframework.gen.testing.jdbc.sample.summary_dto", "ForeignKeyChildSummary", entityDef = foreignKeyEntityDef, generateDto = WithGeneratedDto.TRUE)
//            .field("id").and()
//            .field("createdTimestampUtc").and()
//            .field("someString").and()
//            .field("parentId").and()
//            .lookup(foreignKeyParentEntityDef, localField = "parentId")
//                .lookupField("someString").asDtoField("someForeignString").and()
//                .lookupField("someInt").asDtoField("someForeignInt").and()
//                .and()
//            .build()


    val alphaEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "Alpha",
        idAndNameFieldName = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
    ) {
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val bravoEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "Bravo",
        idAndNameFieldName = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("alpha", alphaEntityDef)
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val charlieEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "Charlie",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("bravo", bravoEntityDef) {
            editableByUser()
        }
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        crud {
            apis {
                create()
                update()
                delete()
            }
        }
    }


    val bravoSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "Bravo",
        entityDef = bravoEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAHANA
    ) {
        field("dtoStringFromAlpha", "alphaId.someString")
        field("dtoIntFromAlpha", "alphaId.someInt")
        field("dtoStringFromBravo", "someString")
        field("dtoIntFromBravo", "someInt")
        field("id", "id")
        field("createdTimestampUtc")
    }


    val charlieSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "Charlie",
        entityDef = charlieEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAHANA
    ) {
        field("dtoStringFromCharlie", "someString")
        field("dtoIntFromCharlie", "someInt")
        field("dtoStringFromBravo", "bravoId.someString")
        field("dtoIntFromBravo", "bravoId.someInt")
        field("dtoStringFromAlpha", "bravoId.alphaId.someString")
        field("dtoIntFromAlpha", "bravoId.alphaId.someInt")
        field("createdTimestampUtc", "createdTimestampUtc")
    }


    val alphaAgGridEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "AlphaAgGrid",
        idAndNameFieldName = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val bravoAgGridEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "BravoAgGrid",
        idAndNameFieldName = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("alpha", alphaAgGridEntityDef)
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val charlieAgGridEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "CharlieAgGrid",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("bravo", bravoAgGridEntityDef) {
            editableByUser()
        }
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        crud {
            apis {
                create()
                update()
                delete()
            }
        }
    }


    val bravoAgGridSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "BravoAgGrid",
        entityDef = bravoAgGridEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.AG_GRID
    ) {
        field("dtoStringFromAlpha", "alphaId.someString")
        field("dtoIntFromAlpha", "alphaId.someInt")
        field("dtoStringFromBravo", "someString")
        field("dtoIntFromBravo", "someInt")
        field("id", "id")
        field("createdTimestampUtc")
    }


    val charlieAgGridSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.gen.testing.jdbc.sample.join",
        "CharlieAgGrid",
        entityDef = charlieAgGridEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAHANA
    ) {
        field("dtoStringFromCharlie", "someString")
        field("dtoIntFromCharlie", "someInt")
        field("dtoStringFromBravo", "bravoId.someString")
        field("dtoIntFromBravo", "bravoId.someInt")
        field("dtoStringFromAlpha", "bravoId.alphaId.someString")
        field("dtoIntFromAlpha", "bravoId.alphaId.someInt")
        field("createdTimestampUtc", "createdTimestampUtc")
    }


    val charlieDtoHtmlTableDef = dtoHtmlTable(charlieSearchableDtoDef) {
        columnFromDto(dtoFieldName = "tableStringFromCharlie", fieldPathInSourceData = "dtoStringFromCharlie")
        columnFromDto(dtoFieldName = "tableStringFromBravo", fieldPathInSourceData = "dtoStringFromBravo")
        columnFromDto(dtoFieldName = "tableStringFromAlpha", fieldPathInSourceData = "dtoStringFromAlpha")
        columnFromDto(dtoFieldName = "createdTimestampUtc", fieldPathInSourceData = "createdTimestampUtc")
    }


    val leftEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.many_to_many",
        "Left",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val rightEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.many_to_many",
        "Right",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val manyToManyJoinEntityDef = manyToManyEntity(
        "org.maiaframework.gen.testing.jdbc.sample.many_to_many",
        "ManyToManyJoin",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        leftEntity = ReferencedEntity("left", "Left", leftEntityDef, IsEditableByUser.TRUE),
        rightEntity = ReferencedEntity("right", "Right", rightEntityDef, IsEditableByUser.TRUE)
    ) {
        field_lastModifiedTimestampUtc()
    }


    val leftSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.gen.testing.jdbc.sample.many_to_many",
        "LeftDetail",
        entityDef = leftEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE
    ) {
        manyToManyJoin(manyToManyJoinEntityDef)
        field("id", "id")
        field("createdTimestampUtc", "createdTimestampUtc")
        field("someIntFromLeft", "someInt")
        field("someIntFromRight", manyToManyJoinEntityDef.fieldPathOf("rightId.someInt"))
        field("someStringFromLeft", "someString")
        field("someStringFromRight", manyToManyJoinEntityDef.fieldPathOf("rightId.someString"))
    }


    val leftNotMappedToRightSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.gen.testing.jdbc.sample.many_to_many",
        "LeftNotMappedToRight",
        entityDef = leftEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE
    ) {
        manyToManyJoin(manyToManyJoinEntityDef, joinType = JoinType.LEFT_OUTER)
        field("id", "id")
        field("createdTimestampUtc", "createdTimestampUtc")
        field("someIntFromLeft", "someInt")
        field("someStringFromLeft", "someString")
        field("rightId", manyToManyJoinEntityDef.fieldPathOf("rightId")) { nullable() }
    }


    val unmodifiableEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample",
        "Unmodifiable"
    ) {
        field("someUniqueInt", FieldTypes.int) { unique() }
    }


    val compositePrimaryKeyEntityDef = entity(
        "org.maiaframework.gen.testing.jdbc.sample.composite_pk",
        "CompositePrimaryKey",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
    ) {

        field("someString", FieldTypes.string) {
            primaryKey()
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int) {
            primaryKey()
        }

    }


}

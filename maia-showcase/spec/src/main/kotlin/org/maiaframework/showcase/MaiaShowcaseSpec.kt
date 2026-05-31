package org.maiaframework.showcase

import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ReferencedEntity
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.HtmlInputType
import org.maiaframework.gen.spec.definition.JoinType
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.FormPurpose
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.FieldTypes


@Suppress("unused")
class MaiaShowcaseSpec : AbstractSpec(AppKey("maia")) {


    private val partySpec = MaiaShowcasePartySpec()


    init {
        adoptAuthoritiesDef(partySpec.authoritiesDef)
    }


    val someEnumDef = enumDef("org.maiaframework.showcase.enums.SomeEnum") {
        withTypescript(withEnumSelectionOptions = true)
        value("OK") {
            displayName = "OK"
            description = "All good"
        }
        value("NOT_OK") {
            displayName = "Not OK"
            displayName = "Not so good"
        }
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


    val someDataClass = dataClass(
        "org.maiaframework.showcase.dataclasses",
        "SomeDataClass"
    ) {
        field("someStringField", FieldTypes.string)
        field("someOptionalBooleanField", FieldTypes.boolean).nullable()
    }


    val simpleResponseDtoDef = simpleResponseDto("org.maiaframework.showcase", "Simple") {
        field("someString", FieldTypes.string)
        field("someInt", FieldTypes.int) { nullable() }
        field("someBoolean", FieldTypes.boolean) { nullable() }
    }


    val loginRequestDtoDef = requestDto(
        "org.maiaframework.showcase.login",
        "Login",
        requestMappingPath = "/api/login",
        withGeneratedEndpoint = false
    ) {
        field("emailAddress", FieldTypes.string) {
            withEmailConstraint()
        }
        field("password", FieldTypes.string) {
            masked()
        }
    }


    val loginFormDef = angularForm(loginRequestDtoDef) {
        delegateFormSubmission()
        formPurpose(FormPurpose.submit)
        field("emailAddress") {
            withLabel("Email Address")
            withoutSeparateFieldLabel()
            withPlaceholder("Email Address...")
            withHtmlAutocompleteAttribute("username")
        }
        field("password") {
            withLabel("Password")
            withoutSeparateFieldLabel()
            withPlaceholder("Password...")
            withHtmlAutocompleteAttribute("current-password")
            withHtmlInputType(HtmlInputType.password)
        }
        submitButtonText("Log in")
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


    val allFieldTypesEntityDef = entity(
        "org.maiaframework.showcase.all_field_types",
        "AllFieldTypes",
        deletable = Deletable.TRUE,
        allowFindAll = AllowFindAll.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        field("someBoolean", FieldTypes.boolean) {
            fieldDisplayName("Some Boolean")
        }
        field("someBooleanNullable", FieldTypes.boolean) {
            fieldDisplayName("Some Boolean Nullable")
            nullable()
        }
        field("someBooleanType", someBooleanTypeDef) {
            fieldDisplayName("Some Boolean Type")
        }
        field("someBooleanTypeNullable", someBooleanTypeDef) {
            fieldDisplayName("Some Boolean Type Nullable")
            nullable()
        }
        field("someBooleanTypeProvided", someProvidedBooleanTypeDef) {
            fieldDisplayName("Some Boolean Type Provided")
        }
        field("someBooleanTypeProvidedNullable", someProvidedBooleanTypeDef) {
            fieldDisplayName("Some Boolean Type Provided Nullable")
            nullable()
        }
        field("someInstant", FieldTypes.instant) {
            fieldDisplayName("Some Instant")
        }
        field("someInstantNullable", FieldTypes.instant) {
            fieldDisplayName("Some Instant Nullable")
            nullable()
        }
        field("someInstantModifiable", FieldTypes.instant) {
            fieldDisplayName("Some Instant Modifiable")
            editableByUser()
        }
        field("someInstantModifiableNullable", FieldTypes.instant) {
            fieldDisplayName("Some Instant Modifiable Nullable")
            editableByUser()
            nullable()
        }
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
        }
        field("someIntModifiable", FieldTypes.int) {
            fieldDisplayName("Some Int Modifiable")
            editableByUser()
        }
        field("someIntNullable", FieldTypes.int) {
            fieldDisplayName("Some Int Nullable")
            editableByUser()
            nullable()
        }
        field("someIntType", someIntTypeDef) {
            fieldDisplayName("Some Int Type")
            unique(withExistsEndpoint = true)
        }
        field("someIntTypeNullable", someIntTypeDef) {
            fieldDisplayName("Some Int Type Nullable")
            nullable()
        }
        field("someIntTypeProvided", someProvidedIntTypeDef) {
            fieldDisplayName("Some Int Type Provided")
        }
        field("someIntTypeProvidedNullable", someProvidedIntTypeDef) {
            fieldDisplayName("Some Int Type Provided Nullable")
            nullable()
        }
        field("someLongType", someLongTypeDef) {
            fieldDisplayName("Some Long Type")
            unique(withExistsEndpoint = true)
        }
        field("someLongTypeNullable", someLongTypeDef) {
            fieldDisplayName("Some Long Type Nullable")
            nullable()
        }
        field("someLongTypeProvided", someProvidedLongTypeDef) {
            fieldDisplayName("Some Long Type Provided")
        }
        field("someLongTypeProvidedNullable", someProvidedLongTypeDef) {
            fieldDisplayName("Some Long Type Provided Nullable")
            nullable()
        }
        field("someLocalDateModifiable", FieldTypes.localDate) {
            fieldDisplayName("Some LocalDate Modifiable")
            editableByUser()
        }
        field("somePeriodModifiable", FieldTypes.period) {
            fieldDisplayName("Some Period Modifiable")
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("somePeriodNullable", FieldTypes.period) {
            fieldDisplayName("Some Period Nullable")
            nullable()
            lengthConstraint(max = 100)
        }
        field("someEnum", someEnumDef) {
            fieldDisplayName("Some Enum")
            lengthConstraint(max = 100)
        }
        field("someEnumNullable", someEnumDef) {
            fieldDisplayName("Some Enum Nullable")
            nullable()
            lengthConstraint(max = 100)
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            unique(withExistsEndpoint = true)
            lengthConstraint(max = 100)
        }
        field("someStringModifiable", FieldTypes.string) {
            fieldDisplayName("Some String Modifiable")
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("someStringNullable", FieldTypes.string) {
            fieldDisplayName("Some String Nullable")
            nullable()
            unique(withExistsEndpoint = true)
            lengthConstraint(max = 100)
        }
        field("someStringType", someStringTypeDef) {
            fieldDisplayName("Some String Type")
            unique(withExistsEndpoint = true)
            lengthConstraint(max = 100)
        }
        field("someStringTypeNullable", someStringTypeDef) {
            fieldDisplayName("Some String Type Nullable")
            nullable()
            lengthConstraint(max = 100)
        }
        field("someProvidedStringType", someProvidedStringTypeDef) {
            fieldDisplayName("Some Provided String Type")
            lengthConstraint(max = 100)
        }
        field("someProvidedStringTypeNullable", someProvidedStringTypeDef) {
            fieldDisplayName("Some Provided String Type Nullable")
            nullable()
            lengthConstraint(max = 100)
        }
        // TODO uncomment and fix
//        field("someListOfEnums", fieldListOf(someEnumDef))
//        field("someListOfInstants", fieldListOf(FieldTypes.instant))
//        field("someListOfLocalDates", fieldListOf(FieldTypes.localDate))
//        field("someListOfPeriods", fieldListOf(FieldTypes.period))
        field("someListOfStrings", fieldListOf(FieldTypes.string)) {
            fieldDisplayName("Some List Of Strings")
            editableByUser()
        }
//        field("someListOfStringTypes", fieldListOf(someStringTypeDef))
//        field("someMapOfStringToInteger", mapOfString().to(Fqcn.INT))
//        field("someMapOfStringTypeToStringType", fieldMapOf(someStringTypeDef).to(someStringTypeDef))
//        field("someDto", simpleResponseDtoDef)
//        field("someDtoNullable", simpleResponseDtoDef) { nullable() }
        field_createdById(partySpec.partyEntityDef)
        field_createdByUsername()
        field_lastModifiedById(partySpec.partyEntityDef)
        field_lastModifiedByUsername()
        field_lastModifiedTimestampUtc()
        index {
            withFieldAscending("someStringModifiable")
            withFieldAscending("someBoolean")
        }
        crud {
            authority(partySpec.writeAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val allFieldTypesEntityDetailViewDef = entityDetailView(allFieldTypesEntityDef)


    val allFieldTypesEntityEditPageDef = entityEditPage(allFieldTypesEntityDef)


    val allFieldTypesEntityCreatePageDef = entityCreatePage(allFieldTypesEntityDef)


    val allFieldTypesSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.all_field_types",
        "AllFieldTypes",
        allFieldTypesEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someBoolean")
        field("someBooleanNullable")
        field("someBooleanType")
        field("someBooleanTypeNullable")
        field("someBooleanTypeProvided")
        field("someBooleanTypeProvidedNullable")
        field("someInstant")
        field("someInstantNullable")
        field("someInstantModifiable")
        field("someInstantModifiableNullable")
        field("someInt")
        field("someIntModifiable")
        field("someIntNullable")
        field("someIntType")
        field("someIntTypeNullable")
        field("someIntTypeProvided")
        field("someIntTypeProvidedNullable")
        field("someLongType")
        field("someLongTypeNullable")
        field("someLongTypeProvided")
        field("someLongTypeProvidedNullable")
        field("someLocalDateModifiable")
        field("somePeriodModifiable")
        field("somePeriodNullable")
        field("someEnum")
        field("someEnumNullable")
        field("someString")
        field("someStringModifiable")
        field("someStringNullable")
        field("someStringType")
        field("someStringTypeNullable")
        field("someProvidedStringType")
        field("someProvidedStringTypeNullable")
        // TODO uncomment and fix
//        field("someListOfEnums")
//        field("someListOfInstants")
//        field("someListOfLocalDates")
//        field("someListOfPeriods")
        field("someListOfStrings")
//        field("someListOfStringTypes")
//        field("someMapOfStringToInteger")
//        field("someMapOfStringTypeToStringType")
//        field("someDto")
//        field("someDtoNullable")
        field("createdBy", "createdBy.id")
        field("createdByUsername")
        field("lastModifiedBy", "lastModifiedBy.id")
        field("lastModifiedByUsername")
        field("lastModifiedTimestampUtc")
    }


    val allFieldTypesBlotterDef = blotter(
        allFieldTypesSearchableDtoDef,
        entityCreatePageDef = allFieldTypesEntityCreatePageDef,
        entityDetailViewPageDef = allFieldTypesEntityDetailViewDef,
        entityEditPageDef = allFieldTypesEntityEditPageDef,
    ) {
        editActionColumn()
        columnFromDto("someBoolean")
        columnFromDto("someBooleanNullable")
        columnFromDto("someBooleanType")
        columnFromDto("someBooleanTypeNullable")
        columnFromDto("someBooleanTypeProvided")
        columnFromDto("someBooleanTypeProvidedNullable")
        columnFromDto("someInstant")
        columnFromDto("someInstantNullable")
        columnFromDto("someInstantModifiable")
        columnFromDto("someInstantModifiableNullable")
        columnFromDto("someInt")
        columnFromDto("someIntModifiable")
        columnFromDto("someIntNullable")
        columnFromDto("someIntType")
        columnFromDto("someIntTypeNullable")
        columnFromDto("someIntTypeProvided")
        columnFromDto("someIntTypeProvidedNullable")
        columnFromDto("someLongType")
        columnFromDto("someLongTypeNullable")
        columnFromDto("someLongTypeProvided")
        columnFromDto("someLongTypeProvidedNullable")
        columnFromDto("someLocalDateModifiable")
        columnFromDto("somePeriodModifiable")
        columnFromDto("somePeriodNullable")
        columnFromDto("someEnum")
        columnFromDto("someEnumNullable")
        columnFromDto("someString")
        columnFromDto("someStringModifiable")
        columnFromDto("someStringNullable")
        columnFromDto("someStringType")
        columnFromDto("someStringTypeNullable")
        columnFromDto("someProvidedStringType")
        columnFromDto("someProvidedStringTypeNullable")
        // TODO uncomment and fix
//        columnFromDto("someListOfEnums")
//        columnFromDto("someListOfInstants")
//        columnFromDto("someListOfLocalDates")
//        columnFromDto("someListOfPeriods")
        columnFromDto("someListOfStrings")
//        columnFromDto("someListOfStringTypes")
//        columnFromDto("someMapOfStringToInteger")
//        columnFromDto("someMapOfStringTypeToStringType")
//        columnFromDto("someDto")
//        columnFromDto("someDtoNullable")
        columnFromDto("createdBy") { header("Created By ID") }
        columnFromDto("createdByUsername")
        columnFromDto("lastModifiedBy") { header("Last Modified By ID") }
        columnFromDto("lastModifiedByUsername")
        columnFromDto("lastModifiedTimestampUtc")
        deleteActionColumn()
    }


    val allFieldTypesBlotterPageDef = blotterPage(allFieldTypesBlotterDef)


    val simpleEntityDef = entity(
        "org.maiaframework.showcase.simple",
        "Simple",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        cacheable {  }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            lengthConstraint(min = 3, max = 100)
            editableByUser()
            unique(withExistsEndpoint = true)
        }
        crud {
            authority(partySpec.adminAuthority)
            create {
                authority(partySpec.adminAuthority)
                api {}
            }
            update {
                authority(partySpec.adminAuthority)
                api {}
            }
            delete {
                authority(partySpec.adminAuthority)
                api {}
            }
        }
    }


    val simpleSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.simple",
        "Simple",
        entityDef = simpleEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someString")
        field("createdTimestampUtc")
    }


    val simpleEntityDetailViewDef = entityDetailView(simpleEntityDef)


    val simpleEntityEditPageDef = entityEditPage(simpleEntityDef) {
        authority = partySpec.adminAuthority
    }


    val simpleEntityCreatePageDef = entityCreatePage(simpleEntityDef) {
        authority = partySpec.adminAuthority
    }


    val simpleBlotterDef = blotter(
        simpleSearchableDtoDef,
        entityCreatePageDef = simpleEntityCreatePageDef,
        entityDetailViewPageDef = simpleEntityDetailViewDef,
        entityEditPageDef = simpleEntityEditPageDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
    ) {
        viewActionColumn()
        editActionColumn()
        columnFromDto("someString")
        columnFromDto("createdTimestampUtc")
        deleteActionColumn()
    }


    val simpleBlotterPageDef = blotterPage(simpleBlotterDef)


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


    val grandparentEntityDef = entity(
        "org.maiaframework.showcase.hierarchy",
        "Grandparent",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        isAbstract = true
        field_createdById(partySpec.partyEntityDef)
        field_lastModifiedById(partySpec.partyEntityDef)
        field_lastModifiedTimestampUtc()
    }


    val parentOneEntityDef = entity(
        "org.maiaframework.showcase.hierarchy",
        "ParentOne",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        superclass(grandparentEntityDef)
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


    val parentOneSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.hierarchy",
        "ParentOne",
        entityDef = parentOneEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someString")
        field("someUniqueString")
        field("createdTimestampUtc")
    }


    val childOneEntityDef = entity(
        "org.maiaframework.showcase.hierarchy",
        "ChildOne",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        superclass(parentOneEntityDef)
        typeDiscriminator("CHILD1")
        field("someInt", FieldTypes.int) {
            editableByUser()
        }
    }


    val childOneSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.hierarchy",
        "ChildOne",
        entityDef = childOneEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someInt")
        field("someString")
        field("someUniqueString")
        field("createdTimestampUtc")
    }


    val parentTwoEntityDef = entity(
        "org.maiaframework.showcase.hierarchy",
        "ParentTwo",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        superclass(grandparentEntityDef)
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


    val parentTwoSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.hierarchy",
        "ParentTwo",
        entityDef = parentTwoEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someInt")
        field("someUniqueString")
        field("createdTimestampUtc")
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


    val someVersionedEntityDef = entity(
        "org.maiaframework.showcase.versioned",
        "SomeVersioned",
        versioned = true,
        deletable = Deletable.TRUE
    ) {
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            editableByUser()
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
            editableByUser()
        }
        index {
            unique()
            withFieldAscending("someInt")
        }
        crud {
            authority(partySpec.writeAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val someVersionedEntityCreateDef = entityCreatePage(someVersionedEntityDef)


    val someVersionedEntityDetailViewDef = entityDetailView(someVersionedEntityDef)


    val someVersionedEditPageDef = entityEditPage(someVersionedEntityDef)


    val someVersionedSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.versioned",
        "SomeVersioned",
        entityDef = someVersionedEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someString")
        field("someInt")
        field("version")
        field("createdTimestampUtc")
    }


    val someVersionedBlotterDef = blotter(
        someVersionedSearchableDtoDef,
        entityCreatePageDef = someVersionedEntityCreateDef,
        entityEditPageDef = someVersionedEditPageDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
    ) {
        editActionColumn()
        columnFromDto("someString")
        columnFromDto("someInt")
        columnFromDto("version")
        columnFromDto("createdTimestampUtc")
        deleteActionColumn()
    }


    val someVersionedBlotterPageDef = blotterPage(someVersionedBlotterDef)


    val withOptionalIndexFieldEntityDef = entity(
        "org.maiaframework.showcase",
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


//    val verySimpleEntityDef = entity(
//        "org.maiaframework.showcase.simple",
//        "VerySimple"
//    ) {
//        cacheable {  }
//        field("someString", FieldTypes.string) {
//            lengthConstraint(max = 100)
//            editableByUser()
//            unique()
//        }
//    }


//        searchableEntityDef("org.maiaframework.showcase", "SomeCaseSensitivity", entityDef = someCaseSensitivityEntityDef, generateDto = WithGeneratedDto.TRUE)
//                .withPreAuthorize("hasAuthority('ROLE_ADMIN')")
//                .field("id").and()
//                .field("caseSensitiveString", caseSensitive = true).and()
//                .field("caseInsensitiveString", caseSensitive = false).and()
//                .field("createdTimestampUtc").and()
//                .build()


    // TODO uncomment and implement
//    val verySimpleSearchableDef = searchableEntityDef("org.maiaframework.showcase.simple", "VerySimple", entityDef = verySimpleEntityDef, generateDto = WithGeneratedDto.TRUE)
//            .field("someString").and()
//            .build()


    val foreignKeyParentEntityDef = entity(
        "org.maiaframework.showcase",
        "ForeignKeyParent"
    ) {
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int)
    }


    val foreignKeyEntityDef = entity(
        "org.maiaframework.showcase",
        "ForeignKeyChild"
    ) {
        field("parentId", FieldTypes.domainId)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    // TODO uncomment and implement
//    val foreignKeyChildSummarySearchableDef = searchableEntityDef("org.maiaframework.showcase.summary_dto", "ForeignKeyChildSummary", entityDef = foreignKeyEntityDef, generateDto = WithGeneratedDto.TRUE)
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
        "org.maiaframework.showcase.join",
        "Alpha",
        nameFieldForPkAndNameDto = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
    ) {
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
            editableByUser()
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            lengthConstraint(max = 100)
            editableByUser()
        }
        crud {
            authority(partySpec.adminAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val alphaEntityCreatePageDef = entityCreatePage(alphaEntityDef)


    val alphaEntityDetailViewPageDef = entityDetailView(alphaEntityDef)


    val alphaEntityEditPageDef = entityEditPage(alphaEntityDef)


    val alphaTypeaheadDef = typeahead(
        "org.maiaframework.showcase.join",
        "Alpha",
        alphaEntityDef,
        sortFieldName = "someString",
        searchTermFieldName = "someString",
        indexVersion = 1
    ) {
        idFieldFromEntity(
            dtoFieldName = "id",
            entityFieldName = "id",
            esDocMappingType = EsDocMappingTypes.keyword
        )
        fieldFromEntity(
            dtoFieldName = "someString",
            entityFieldName = "someString",
            esDocMappingType = EsDocMappingTypes.searchAsYouType
        )
    }


    val alphaSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.join",
        "Alpha",
        entityDef = alphaEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAIA
    ) {
        field("someString")
        field("someInt")
        field("createdTimestampUtc")
    }


    val alphaBlotterDef = blotter(
        alphaSearchableDtoDef,
        entityDetailViewPageDef = alphaEntityDetailViewPageDef,
        entityCreatePageDef = alphaEntityCreatePageDef,
        entityEditPageDef = alphaEntityEditPageDef,
    ) {
        viewActionColumn()
        editActionColumn()
        columnFromDto(fieldPathInSourceData = "someString")
        columnFromDto(fieldPathInSourceData = "someInt")
        columnFromDto(fieldPathInSourceData = "createdTimestampUtc")
        deleteActionColumn()
    }


    val alphaBlotterPageDef = blotterPage(alphaBlotterDef)


    val bravoEntityDef = entity(
        "org.maiaframework.showcase.join",
        "Bravo",
        nameFieldForPkAndNameDto = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("alpha", alphaEntityDef) {
            fieldDisplayName("Alpha")
            typeaheadField(alphaTypeaheadDef)
        }
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
            editableByUser()
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            lengthConstraint(max = 100)
            editableByUser()
        }
        crud {
            authority(partySpec.adminAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val charlieEntityDef = entity(
        "org.maiaframework.showcase.join",
        "Charlie",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("bravo", bravoEntityDef) {
            fieldDisplayName("Bravo")
            editableByUser()
        }
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            lengthConstraint(max = 100)
        }
        crud {
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val bravoEntityCreatePageDef = entityCreatePage(bravoEntityDef)


    val bravoEntityDetailViewPageDef = entityDetailView(bravoEntityDef)


    val bravoEntityEditPageDef = entityEditPage(bravoEntityDef)


    val bravoSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.join",
        "Bravo", // TODO rename to BravoSearchable
        entityDef = bravoEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAIA
    ) {
        field("dtoStringFromAlpha", "alpha.someString")
        field("dtoIntFromAlpha", "alpha.someInt")
        field("dtoStringFromBravo", "someString")
        field("dtoIntFromBravo", "someInt")
        field("createdTimestampUtc")
    }


    val bravoBlotterDef = blotter(
        bravoSearchableDtoDef,
        entityDetailViewPageDef = bravoEntityDetailViewPageDef,
        entityCreatePageDef = bravoEntityCreatePageDef,
        entityEditPageDef = bravoEntityEditPageDef,
    ) {
        viewActionColumn()
        editActionColumn()
        columnFromDto(dtoFieldName = "tableStringFromAlpha", fieldPathInSourceData = "dtoStringFromAlpha")
        columnFromDto(dtoFieldName = "tableStringFromBravo", fieldPathInSourceData = "dtoStringFromBravo")
        columnFromDto(dtoFieldName = "createdTimestampUtc", fieldPathInSourceData = "createdTimestampUtc")
        deleteActionColumn()
    }


    val bravoBlotterPageDef = blotterPage(bravoBlotterDef)


    val charlieSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.join",
        "Charlie",
        entityDef = charlieEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAIA
    ) {
        field("dtoStringFromCharlie", "someString")
        field("dtoIntFromCharlie", "someInt")
        field("dtoStringFromBravo", "bravo.someString")
        field("dtoIntFromBravo", "bravo.someInt")
        field("dtoStringFromAlpha", "bravo.alpha.someString")
        field("dtoIntFromAlpha", "bravo.alpha.someInt")
        field("createdTimestampUtc", "createdTimestampUtc")
    }


    val alphaAgGridEntityDef = entity(
        "org.maiaframework.showcase.join",
        "AlphaAgGrid",
        nameFieldForPkAndNameDto = "someString",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        field("someInt", FieldTypes.int)
        field("someString", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
    }


    val bravoAgGridEntityDef = entity(
        "org.maiaframework.showcase.join",
        "BravoAgGrid",
        nameFieldForPkAndNameDto = "someString",
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
        "org.maiaframework.showcase.join",
        "CharlieAgGrid",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE
    ) {
        foreignKey("bravo", bravoAgGridEntityDef) {
            fieldDisplayName("Bravo")
            editableByUser()
        }
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            lengthConstraint(max = 100)
        }
        crud {
            authority(partySpec.adminAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val bravoAgGridSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.join",
        "BravoAgGrid",
        entityDef = bravoAgGridEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.AG_GRID
    ) {
        field("dtoStringFromAlpha", "alpha.someString")
        field("dtoIntFromAlpha", "alpha.someInt")
        field("dtoStringFromBravo", "someString")
        field("dtoIntFromBravo", "someInt")
        field("createdTimestampUtc")
    }


    val charlieAgGridSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.join",
        "CharlieAgGrid",
        entityDef = charlieAgGridEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE,
        searchModelType = SearchModelType.MAIA
    ) {
        field("dtoStringFromCharlie", "someString")
        field("dtoIntFromCharlie", "someInt")
        field("dtoStringFromBravo", "bravo.someString")
        field("dtoIntFromBravo", "bravo.someInt")
        field("dtoStringFromAlpha", "bravo.alpha.someString")
        field("dtoIntFromAlpha", "bravo.alpha.someInt")
        field("createdTimestampUtc", "createdTimestampUtc")
    }


    val charlieBlotterDef = blotter(charlieSearchableDtoDef) {
        columnFromDto(dtoFieldName = "tableStringFromCharlie", fieldPathInSourceData = "dtoStringFromCharlie")
        columnFromDto(dtoFieldName = "tableStringFromBravo", fieldPathInSourceData = "dtoStringFromBravo")
        columnFromDto(dtoFieldName = "tableStringFromAlpha", fieldPathInSourceData = "dtoStringFromAlpha")
        columnFromDto(dtoFieldName = "createdTimestampUtc", fieldPathInSourceData = "createdTimestampUtc")
    }


    val leftManyEntityDef = entity(
        "org.maiaframework.showcase.many_to_many",
        "LeftMany",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        nameFieldForPkAndNameDto = "someString",
    ) {
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
            editableByUser()
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            lengthConstraint(max = 100)
            editableByUser()
        }
        crud {
            authority(partySpec.adminAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val leftManyTypeaheadDef = typeahead(
        "org.maiaframework.showcase.many_to_many",
        "LeftMany",
        leftManyEntityDef,
        sortFieldName = "someString",
        searchTermFieldName = "someString",
        indexVersion = 1
    ) {
        idFieldFromEntity(
            dtoFieldName = "id",
            entityFieldName = "id",
            esDocMappingType = EsDocMappingTypes.keyword
        )
        fieldFromEntity(
            dtoFieldName = "someString",
            entityFieldName = "someString",
            esDocMappingType = EsDocMappingTypes.searchAsYouType
        )
    }


    val rightManyEntityDef = entity(
        "org.maiaframework.showcase.many_to_many",
        "RightMany",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        nameFieldForPkAndNameDto = "someString"
    ) {
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
            editableByUser()
        }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            editableByUser()
            lengthConstraint(max = 100)
        }
        crud {
            authority(partySpec.adminAuthority)
            create {
                api {  }
            }
            update {
                api {  }
            }
            delete {
                api {  }
            }
        }
    }


    val leftToRightManyToManyJoinEntityDef = manyToManyEntity(
        "org.maiaframework.showcase.many_to_many",
        "LeftToRightManyToManyJoin",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        leftEntity = ReferencedEntity(fieldName = "left", displayName = "Left", leftManyEntityDef, IsEditableByUser.TRUE),
        rightEntity = ReferencedEntity(
            fieldName = "right",
            displayName = "Right",
            rightManyEntityDef,
            IsEditableByUser.TRUE
        )
    ) {
        field_lastModifiedTimestampUtc()
    }


    val rightManyTypeaheadDef = typeahead(
        "org.maiaframework.showcase.many_to_many",
        "RightMany",
        rightManyEntityDef,
        sortFieldName = "someString",
        searchTermFieldName = "someString",
        indexVersion = 1
    ) {
        idFieldFromEntity(
            dtoFieldName = "id",
            entityFieldName = "id",
            esDocMappingType = EsDocMappingTypes.keyword
        )
        fieldFromEntity(
            dtoFieldName = "someString",
            entityFieldName = "someString",
            esDocMappingType = EsDocMappingTypes.searchAsYouType
        )
    }


    val rightManyEntityDetailViewPageDef = entityDetailView(rightManyEntityDef)


    val rightManyEntityCreatePageDef = entityCreatePage(rightManyEntityDef)


    val rightManyEntityEditPageDef = entityEditPage(rightManyEntityDef)


    val rightManySearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.many_to_many",
        "RightMany",
        entityDef = rightManyEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE
    ) {
        field("createdTimestampUtc")
        field("someIntFromLeft", "someInt")
        field("someStringFromLeft", "someString")
        manyToManyField("rightEntities", leftToRightManyToManyJoinEntityDef)
    }


    val rightManyBlotterDef = blotter(
        rightManySearchableDtoDef,
        entityCreatePageDef = rightManyEntityCreatePageDef,
        entityDetailViewPageDef = rightManyEntityDetailViewPageDef,
        entityEditPageDef = rightManyEntityEditPageDef,
    ) {
        viewActionColumn()
        editActionColumn()
        columnFromDto("someStringFromLeft", "someString") { header("Some String From Left") }
        columnFromDto("someIntFromLeft", "someInt") { header("Some Int From Left") }
        columnFromDto("rightEntities") { header("Right Entities") }
        deleteActionColumn()
    }


    val rightManyBlotterPageDef = blotterPage(rightManyBlotterDef)


    val leftManyEntityDetailViewPageDef = entityDetailView(leftManyEntityDef)


    val leftManyEntityCreatePageDef = entityCreatePage(leftManyEntityDef)


    val leftManyEntityEditPageDef = entityEditPage(leftManyEntityDef)


    val leftManySearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.many_to_many",
        "LeftMany",
        entityDef = leftManyEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE
    ) {
        field("createdTimestampUtc")
        field("someIntFromLeft", "someInt")
        field("someStringFromLeft", "someString")
        manyToManyField("rightEntities", leftToRightManyToManyJoinEntityDef)
    }


    val leftManyBlotterDef = blotter(
        leftManySearchableDtoDef,
        entityCreatePageDef = leftManyEntityCreatePageDef,
        entityDetailViewPageDef = leftManyEntityDetailViewPageDef,
        entityEditPageDef = leftManyEntityEditPageDef,
    ) {
        viewActionColumn()
        editActionColumn()
        columnFromDto("someStringFromLeft", "someString") { header("Some String From Left") }
        columnFromDto("someIntFromLeft", "someInt") { header("Some Int From Left") }
        columnFromDto("rightEntities") { header("Right Entities") }
        deleteActionColumn()
    }


    val leftManyBlotterPageDef = blotterPage(leftManyBlotterDef)


    val leftToRightManyToManySearchableDtoDef = searchableDto(
        "org.maiaframework.showcase",
        "LeftToRightManyToMany",
        leftToRightManyToManyJoinEntityDef.entityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
    ) {
        field("leftId", "left.id")
        field("rightId", "right.id")
        field("leftSomeString", "left.someString")
        field("leftSomeInt", "left.someInt")
        field("rightSomeString", "right.someString")
        field("rightSomeInt", "right.someInt")
    }


    val leftNotMappedToRightSearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.many_to_many",
        "LeftNotMappedToRight",
        entityDef = leftManyEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE
    ) {
        manyToManyJoin(leftToRightManyToManyJoinEntityDef, joinType = JoinType.LEFT_OUTER)
        field("createdTimestampUtc", "createdTimestampUtc")
        field("someIntFromLeft", "someInt")
        field("someStringFromLeft", "someString")
    }


    val unmodifiableEntityDef = entity(
        "org.maiaframework.showcase",
        "Unmodifiable"
    ) {
        field("someUniqueInt", FieldTypes.int) { unique() }
    }


    val compositePrimaryKeyEntityDef = entity(
        "org.maiaframework.showcase.composite_pk",
        "CompositePrimaryKey",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        versioned = true,
        recordVersionHistory = true
    ) {
        cacheable {  }
        field("someString", FieldTypes.string) {
            fieldDisplayName("Some String")
            primaryKey()
            lengthConstraint(max = 100)
        }
        field("someInt", FieldTypes.int) {
            fieldDisplayName("Some Int")
            primaryKey()
        }
        field("someModifiableString", FieldTypes.string) {
            fieldDisplayName("Some Modifiable String")
            lengthConstraint(max = 100)
            editableByUser()
        }
        crud {
            authority(partySpec.writeAuthority)
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    val compositePrimaryKeyEntityCreatePageDef = entityCreatePage(compositePrimaryKeyEntityDef)


    val compositePrimaryKeyEntityDetailViewDef = entityDetailView(compositePrimaryKeyEntityDef)


    val compositePrimaryKeyEntityEditPageDef = entityEditPage(compositePrimaryKeyEntityDef)


    val compositePrimaryKeySearchableDtoDef = searchableDto(
        "org.maiaframework.showcase.composite_pk",
        "CompositePrimaryKey",
        entityDef = compositePrimaryKeyEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("someString")
        field("someInt")
        field("someModifiableString")
        field("version")
        field("createdTimestampUtc")
    }


    val compositePrimaryKeyBlotterDef = blotter(
        compositePrimaryKeySearchableDtoDef,
        entityCreatePageDef = compositePrimaryKeyEntityCreatePageDef,
        entityDetailViewPageDef = compositePrimaryKeyEntityDetailViewDef,
        entityEditPageDef = compositePrimaryKeyEntityEditPageDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
    ) {
        viewActionColumn()
        editActionColumn()
        columnFromDto("someString")
        columnFromDto("someInt")
        columnFromDto("someModifiableString")
        columnFromDto("version")
        columnFromDto("createdTimestampUtc")
        deleteActionColumn()
    }


    val compositePrimaryKeyBlotterPageDef = blotterPage(compositePrimaryKeyBlotterDef)


    val nonSurrogatePrimaryKeyEntityDef = entity(
        "org.maiaframework.showcase.non_surrogate_pk",
        "NonSurrogatePrimaryKey",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        versioned = true,
        recordVersionHistory = true
    ) {
        field("someString", someStringValueClassDef) {
            primaryKey()
            lengthConstraint(max = 100)
        }
        field("someModifiableString", FieldTypes.string) {
            lengthConstraint(max = 100)
            modifiableBySystem()
        }

    }


    val nonSurrogateIdPrimaryKeyEntityDef = entity(
        "org.maiaframework.showcase.non_surrogate_pk",
        "NonSurrogateIdPrimaryKey",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        versioned = true,
        recordVersionHistory = true
    ) {
        field("id", someStringValueClassDef) {
            primaryKey()
            lengthConstraint(max = 100)
        }
        field("someModifiableString", FieldTypes.string) {
            lengthConstraint(max = 100)
            modifiableBySystem()
        }

    }


}

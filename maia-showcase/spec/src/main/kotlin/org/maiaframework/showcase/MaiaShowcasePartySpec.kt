package org.maiaframework.showcase

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused", "MemberVisibilityCanBePrivate")
class MaiaShowcasePartySpec : AbstractSpec(appKey = AppKey("maia_party"), defaultSchemaName = SchemaName("maia")) {


    val opsAuthority = authority("SYS___OPS") {
        description = "Grants access to system functions that are only available to internal employees."
    }


    val adminAuthority = authority("SYS__ADMIN") {
        description = """Grants access to administer the system. This is only for internal employees.
            |These functions would not normally be for day-to-day operations. 
            """.trimMargin()
    }


    val readAuthority = authority("READ") {
        description = "Grants read access."
    }


    val writeAuthority = authority("WRITE") {
        description = "Grants write access."
    }


    val authoritiesDef = authorities("org.maiaframework.showcase.auth", "Authority") {
        authority(adminAuthority)
        authority("SYS__DEVOPS")
        authority("SYS__DEVOPS_READONLY")
        authority(opsAuthority)
        authority("SYS__SYSTEM_USER_GROUPS_EDIT")
        authority("SYS__SYSTEM_USER_GROUPS_VIEW")
        authority(readAuthority)
        authority(writeAuthority)
    }


    val firstNameStringTypeDef = stringType("org.maiaframework.domain.party", "FirstName") {
        provided()
    }


    val lastNameStringTypeDef = stringType("org.maiaframework.domain.party", "LastName") {
        provided()
    }


    val someProvidedStringTypeDef = stringType("org.maiaframework.showcase.types.SomeProvidedStringType") {
        provided()
    }


    val emailAddressStringType = stringType("org.maiaframework.domain.contact", "EmailAddress") {
        provided()
    }


    val ipAddressStringType = stringType("org.maiaframework.domain.net", "IpAddress") {
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


    val emailAddressPurposeEnumDef = enumDef("org.maiaframework.domain.contact.EmailAddressPurpose") { provided() }


    val someDataClass = dataClass(
        "org.maiaframework.showcase.dataclasses",
        "SomeDataClass"
    ) {
        field("someStringField", FieldTypes.string)
        field("someOptionalBooleanField", FieldTypes.boolean).nullable()
    }


    val simpleResponseDto = simpleResponseDto("org.maiaframework.showcase", "Simple") {
        field("someString", FieldTypes.string)
        field("someInt", FieldTypes.int) { nullable() }
        field("someBoolean", FieldTypes.boolean) { nullable() }
    }


    val partyEntityDef = entity(
        "org.maiaframework.showcase.party",
        "Party",
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        nameFieldForPkAndNameDto = "displayName",
        versioned = true,
        recordVersionHistory = true,
    ) {
        isAbstract = true
        tableName(name = "party", viewName = "v_party")
        field("displayName", FieldTypes.string) {
            derived()
        }
        field(ClassFieldName.createdById.value, FieldTypes.domainId) {
            fieldDisplayName("Created By")
            tableColumnName(TableColumnName.createdById.value)
            nullable()
            modifiableBySystem()
            notCreatableByUser()
        }
        field(ClassFieldName.lastModifiedById.value, FieldTypes.domainId) {
            fieldDisplayName("Last Modified By")
            tableColumnName(TableColumnName.lastModifiedById.value)
            nullable()
            modifiableBySystem()
            notCreatableByUser()
        }
        field_lastModifiedTimestampUtc()
        field_lifecycleState()
    }


    val orgEntityDef = entity(
        "org.maiaframework.showcase.org",
        "Organization"
    ) {
        superclass(partyEntityDef)
        typeDiscriminator("ORG")
        field("orgName", FieldTypes.string) {
            editableByUser()
            fieldDisplayName("Organization Name")
            lengthConstraint(max = 300)
        }
    }


    val orgSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.showcase",
        "Org",
        entityDef = orgEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("orgName", "orgName")
        field("displayName", "displayName")
        field("id", "id")
        field("createdTimestampUtc")
    }


    val personEntityDef = entity(
        "org.maiaframework.showcase.person",
        "Person"
    ) {
        superclass(partyEntityDef)
        typeDiscriminator("PER")
        field("firstName", firstNameStringTypeDef) {
            nullable()
            editableByUser()
            lengthConstraint(max = 100)
            fieldDisplayName("First Name")
        }
        field("lastName", lastNameStringTypeDef) {
            editableByUser()
            lengthConstraint(max = 100)
            fieldDisplayName("Last Name")
        }
    }


    val personSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.showcase",
        "Person",
        entityDef = personEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("firstName", "firstName")
        field("lastName", "lastName")
        field("displayName", "displayName")
        field("id", "id")
        field("createdTimestampUtc")
    }


    val userEntityDef = entity(
        "org.maiaframework.showcase.user",
        "User",
        nameFieldForPkAndNameDto = "displayName",
    ) {
        superclass(personEntityDef)
        typeDiscriminator("USR")
        field("authorities", fieldListOf(authoritiesDef.enumDef)) {
            fieldDisplayName("Authorities")
            editableByUser()
        }
        field("encryptedPassword", FieldTypes.string) {
            modifiableBySystem()
            notCreatableByUser()
            masked()
            lengthConstraint(max = 100)
        }
        crud {
            apis(defaultAuthority = adminAuthority) {
                create()
                update()
            }
        }
    }


    val userSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.showcase",
        "User",
        entityDef = userEntityDef,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        field("encryptedPassword", "encryptedPassword")
        field("firstName", "firstName")
        field("lastName", "lastName")
        field("displayName", "displayName")
        field("id", "id")
        field("createdTimestampUtc")
    }


    val userDtoHtmlTableDef = dtoHtmlTable(
        userSearchableDtoDef,
        withAddButton = true,
    ) {
        columnFromDto("displayName")
        columnFromDto("firstName")
        columnFromDto("lastName")
        columnFromDto("createdTimestampUtc")
        editActionColumn()
    }


    val userCrudDef = crudTableDef(userDtoHtmlTableDef, userEntityDef.entityCrudApiDef!!)


    val userGroupEntityDef = entity(
        "org.maiaframework.showcase.user",
        "UserGroup",
        recordVersionHistory = true,
        nameFieldForPkAndNameDto = "name",
    ) {
        moduleName("ops")
        typeDiscriminator("UG")
        field("name", FieldTypes.string) {
            editableByUser()
            lengthConstraint(min = 3, max = 200)
        }
        field("description", FieldTypes.string) {
            editableByUser()
            lengthConstraint(max = 1000)
        }
        field("systemManaged", FieldTypes.boolean) {
            editableByUser()
        }
        field("authorities", fieldListOf(authoritiesDef.enumDef)) {
            editableByUser()
        }
    }


    val orgUserGroupEntityDef = entity(
        "org.maiaframework.showcase.org",
        "OrgUserGroup",
        recordVersionHistory = true
    ) {
        moduleName("ops")
        superclass(userGroupEntityDef)
        typeDiscriminator("OUG")
        foreignKey("org", orgEntityDef)
    }


    val userGroupMembershipEntityDef = manyToManyEntity(
        "org.maiaframework.showcase.user",
        "UserGroupMembership",
        recordVersionHistory = true,
        leftEntity = ReferencedEntity("userGroup", "User Group", userGroupEntityDef, IsEditableByUser.TRUE),
        rightEntity = ReferencedEntity("user", "User", userEntityDef, IsEditableByUser.TRUE)
    ) {
        moduleName("ops")
    }


    val userGroupMembershipSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.showcase.user",
        "UserGroupMembership",
        userGroupMembershipEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        moduleName("ops")
        field("id")
        field("userId")
        field("userGroupId")
        lookup(userEntityDef, "userId")
            .lookupField("firstName").and()
            .lookupField("lastName").and()
            .and()
            .lookup(userGroupEntityDef, "userGroupId")
            .lookupField("id").and()
            .lookupField("authorities").and()
            .and()
            .lookup(userEntityDef, "userId")
            .lookupField("displayName").and()
            .and()
            .build()
    }


    val personSummarySearchableDef = searchableEntityDef(
        "org.maiaframework.showcase.person",
        "PersonSummary",
        entityDef = personEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE,
        withGeneratedEndpoint = WithGeneratedEndpoint.TRUE
    ) {
        withPreAuthorize("hasAuthority('ROLE_ADMIN')")
        field("id", caseSensitive = true)
        field("firstName", caseSensitive = true)
        field("lastName", caseSensitive = true)
        field("createdTimestampUtc", caseSensitive = true)
    }


    val emailAddressEntityDef = entity("org.maiaframework.showcase.contact", "EmailAddress") {
        moduleName("ops")
        field("emailAddress", emailAddressStringType) {
            unique()
            withEmailConstraint()
        }
        field_createdById(partyEntityDef)
        field_lastModifiedById(partyEntityDef)
        field_lastModifiedTimestampUtc()
    }


    val partyEmailAddressEntityDef = entity(
        "org.maiaframework.showcase.party.contact",
        "PartyEmailAddress",
        recordVersionHistory = true
    ) {
        moduleName("ops")
        withEffectiveTimestamps(hasSingleEffectiveRecord = false)
        foreignKey("party", partyEntityDef)
        foreignKey("emailAddress", emailAddressEntityDef)
        field("isPrimaryContact", FieldTypes.boolean) {
            editableByUser()
        }
        field("purposes", fieldListOf(emailAddressPurposeEnumDef)) {
            editableByUser()
        }
        field_createdById(partyEntityDef)
        field_lastModifiedById(partyEntityDef)
        field_lastModifiedTimestampUtc()
        index {
            withFieldAscending("emailAddressId")
        }
        index {
            withFieldAscending("partyId")
        }
    }


    val emailAddressVerificationEntityDef = entity(
        "org.maiaframework.showcase.party.contact",
        "EmailAddressVerification",
        versioned = true
    ) {
        moduleName("ops")
        field("emailAddressId", FieldTypes.domainId)
        field("ipAddress", ipAddressStringType) {
            nullable()
            lengthConstraint(min = 11, max = 20)
        }
        field_createdById(partyEntityDef, nullable = true)
        field_lastModifiedById(partyEntityDef, nullable = true)
        field_lastModifiedTimestampUtc()
        withEffectiveTimestamps()
    }


}

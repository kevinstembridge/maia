package org.maiaframework.showcase

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.AllowDeleteAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused", "MemberVisibilityCanBePrivate")
class MaiaShowcasePartySpec : AbstractSpec(appKey = AppKey("sample_party"), defaultSchemaName = SchemaName("testing")) {


    val opsAuthority = authority("SYS___OPS") {
        description = "Grants access to system functions that are only available to internal employees."
    }


    val adminAuthority = authority("ROLE_ADMIN")


    val authoritiesDef = authorities("acme.auth", "Authority") {
        authority("SYS__ADMIN") {
            description = """Grants access to administer the system. This is only for internal employees.
                |These functions would not normally be for day-to-day operations. 
            """.trimMargin()
        }
        authority("SYS__DEVOPS")
        authority("SYS__DEVOPS_READONLY")
        authority("SYS__SYSTEM_ANALYTICS")
        authority(opsAuthority)
        authority("SYS__SYSTEM_USER_GROUPS_EDIT")
        authority("SYS__SYSTEM_USER_GROUPS_VIEW")
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
        field("emailAddress", emailAddressStringType) {
            withEmailConstraint()
        }
        field("displayName", FieldTypes.string) {
            derived()
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
        }
        field("lastName", lastNameStringTypeDef) {
            editableByUser()
            lengthConstraint(max = 100)
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
        "User"
    ) {
        superclass(personEntityDef)
        typeDiscriminator("USR")
        field("encryptedPassword", FieldTypes.string) {
            modifiableBySystem()
            masked()
            lengthConstraint(max = 100)
        }
        field("someStrings", fieldListOf(FieldTypes.string))
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


    val userGroupEntityDef = entity(
        "org.maiaframework.showcase.user",
        "UserGroup",
        recordVersionHistory = true
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


    val userGroupMembershipEntityDef = entity(
        "org.maiaframework.showcase.user",
        "OrgUserGroupMembership",
        recordVersionHistory = true
    ) {
        moduleName("ops")
        field("orgUserGroupId", FieldTypes.domainId)
        field("userId", FieldTypes.domainId)
        index {
            withFieldAscending("orgUserGroupId")
            withFieldAscending("userId")
            unique()
        }
        index {
            withFieldAscending("userId")
        }
    }


    val orgUserGroupMembershipSearchableDtoDef = searchableEntityDef(
        "org.maiaframework.showcase.org",
        "OrgUserGroupMembership",
        userGroupMembershipEntityDef,
        withGeneratedDto = WithGeneratedDto.TRUE
    ) {
        moduleName("ops")
        field("id")
        field("userId")
        field("orgUserGroupId")
        lookup(userEntityDef, "userId")
            .lookupField("firstName").and()
            .lookupField("lastName").and()
            .and()
            .lookup(orgUserGroupEntityDef, "orgUserGroupId")
            .lookupField("orgId").and()
            .lookupField("authorities").and()
            .and()
            .lookup(orgEntityDef, "orgId")
            .lookupField("orgName").and()
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
        field("emailAddress", caseSensitive = true)
        field("firstName", caseSensitive = true)
        field("lastName", caseSensitive = true)
        field("createdTimestampUtc", caseSensitive = true)
    }


}

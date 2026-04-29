package org.maiaframework.showcase.user

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.maiaframework.showcase.testing.fixtures.UserFixture
import org.springframework.beans.factory.annotation.Autowired


class UserGroupMembershipCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var userGroupDao: UserGroupDao


    @Autowired
    private lateinit var userGroupMembershipDao: UserGroupMembershipDao


    private lateinit var memberUser: UserFixture
    private lateinit var userGroup: UserGroupEntity


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()
        memberUser = fixtures.aUser(loginMailVerified = true)
        fixtures.resetDatabaseState()

        // Insert after resetDatabaseState() because reset truncates user_group and user_group_membership tables
        userGroup = UserGroupEntity.newInstance(
            authorities = emptyList(),
            description = "Test group",
            name = "Test Group",
            systemManaged = false
        )
        userGroupDao.insert(userGroup)

        userGroupMembershipDao.insert(
            UserGroupMembershipEntity.newInstance(
                effectiveFrom = null,
                effectiveTo = null,
                user = memberUser.userEntity.id,
                userGroup = userGroup.id
            )
        )

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(userGroupMembershipBlotterPage)

        userGroupMembershipBlotterPage.apply {

            // Read: blotter shows the pre-seeded membership
            assertTableContainsValue(memberUser.displayName)
            assertTableContainsValue(userGroup.name)

            // Create (error path): user/userGroup absent from form → backend rejects
            clickAddButton()
            clickSubmitButton()
            assertDialogShowsError()
            clickCancelButton()
            assertCreateDialogClosed()

            // Edit (success path): type in fixture DomainIds, backend accepts
            clickEditButtonForFirstRow()
            fillEditForm(
                userGroupId = userGroup.id.toString(),
                userId = memberUser.userEntity.id.toString()
            )
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue(memberUser.displayName)

        }

    }


}

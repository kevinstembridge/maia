package org.maiaframework.gen.testing.mongo.sample.party

import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.gen.testing.mongo.sample.org.OrganizationEntityTestBuilder
import org.maiaframework.gen.testing.mongo.sample.person.PersonEntityTestBuilder
import org.maiaframework.gen.testing.mongo.sample.user.UserEntityTestBuilder
import org.maiaframework.gen.testing.mongo.sample.org.OrganizationDao
import org.maiaframework.gen.testing.mongo.sample.org.OrganizationEntity
import org.maiaframework.gen.testing.mongo.sample.party.PartyDao
import org.maiaframework.gen.testing.mongo.sample.person.PersonDao
import org.maiaframework.gen.testing.mongo.sample.person.PersonEntity
import org.maiaframework.gen.testing.mongo.sample.user.UserDao
import org.maiaframework.gen.testing.mongo.sample.user.UserEntity
import org.springframework.beans.factory.annotation.Autowired
import org.testng.Assert.assertEquals
import org.testng.annotations.Test


class PartyDaoTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var partyDao: PartyDao

    @Autowired
    private lateinit var personDao: PersonDao

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var organizationDao: OrganizationDao

    @Test
    fun testInsertAndFindById() {

        val organizationEntity = OrganizationEntityTestBuilder().build()
        this.organizationDao.insert(organizationEntity)

        val personEntity = PersonEntityTestBuilder().build()
        this.personDao.insert(personEntity)

        val userEntity = UserEntityTestBuilder().build()
        this.userDao.insert(userEntity)

        val actualOrganization = this.partyDao.findById(organizationEntity.id) as OrganizationEntity

        assertEquals(actualOrganization.createdTimestampUtc, organizationEntity.createdTimestampUtc)
        assertEquals(actualOrganization.name, organizationEntity.name)

        val actualPerson = this.partyDao.findById(personEntity.id) as PersonEntity

        assertEquals(actualPerson.createdTimestampUtc, personEntity.createdTimestampUtc)
        assertEquals(actualPerson.firstName, personEntity.firstName)
        assertEquals(actualPerson.lastName, personEntity.lastName)

        val actualUser = this.partyDao.findById(userEntity.id) as UserEntity

        assertEquals(actualUser.createdTimestampUtc, userEntity.createdTimestampUtc)
        assertEquals(actualUser.firstName, userEntity.firstName)
        assertEquals(actualUser.lastName, userEntity.lastName)
        assertEquals(actualUser.encryptedPassword, userEntity.encryptedPassword)

    }


}

package org.maiaframework.showcase.derived_field

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.party.OrgEntityTestBuilder
import org.maiaframework.showcase.party.PartyDao
import org.maiaframework.showcase.party.PersonEntityTestBuilder
import org.springframework.beans.factory.annotation.Autowired


class DerivedFieldTest: AbstractBlackBoxTest() {


    @Autowired
    private lateinit var dao: PartyDao


    @Test
    fun `test for derived displayName field`() {

        val somePersonEntity = PersonEntityTestBuilder().build().also { dao.insert(it) }
        val someOrgEntity = OrgEntityTestBuilder().build().also { dao.insert(it) }

        val retrievedPersonEntity = dao.findByPrimaryKey(somePersonEntity.id)
        val retrievedOrgEntity = dao.findByPrimaryKey(someOrgEntity.id)

        assertThat(retrievedPersonEntity.displayName).isEqualTo("${somePersonEntity.firstName} ${somePersonEntity.lastName}")
        assertThat(retrievedOrgEntity.displayName).isEqualTo(someOrgEntity.orgName)

    }


}

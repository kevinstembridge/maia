package org.maiaframework.gen.testing.jdbc.derived

import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.jdbc.party.OrgEntityTestBuilder
import org.maiaframework.gen.testing.jdbc.party.PersonEntityTestBuilder
import org.maiaframework.gen.testing.sample.party.PartyDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DerivedFieldTest: AbstractJdbcTest() {


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

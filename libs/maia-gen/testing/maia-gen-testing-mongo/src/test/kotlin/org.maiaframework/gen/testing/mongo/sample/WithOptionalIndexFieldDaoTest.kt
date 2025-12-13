package org.maiaframework.gen.testing.mongo.sample

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.gen.testing.mongo.sample.types.SomeStringType
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.Test
import java.time.Instant
import java.util.UUID

class WithOptionalIndexFieldDaoTest : AbstractIntegrationTest() {


    @Autowired
    private lateinit var dao: WithOptionalIndexFieldDao


    @Test
    fun test_find_with_filter_on_optional_index_field() {

        // GIVEN 2 records with given values in the index fields
        val fieldValue1 = SomeStringType(UUID.randomUUID().toString())
        val fieldValue2 = SomeStringType(UUID.randomUUID().toString())
        val fieldValue3 = SomeStringType(UUID.randomUUID().toString())
        val entity1 = WithOptionalIndexFieldEntity(Instant.now(), DomainId.newId(), fieldValue1, fieldValue2, fieldValue3)
        val entity2 = WithOptionalIndexFieldEntity(Instant.now(), DomainId.newId(), fieldValue1, fieldValue2, fieldValue3)

        this.dao.insert(entity1)
        this.dao.insert(entity2)

        // WHEN we search with a random value
        val filter = WithOptionalIndexFieldEntityFilters.someOptionalString1().eq(SomeStringType(UUID.randomUUID().toString()))
        val searchResults1 = this.dao.findAllBy(filter)

        // THEN
        assertThat(searchResults1).isEmpty()


    }


}

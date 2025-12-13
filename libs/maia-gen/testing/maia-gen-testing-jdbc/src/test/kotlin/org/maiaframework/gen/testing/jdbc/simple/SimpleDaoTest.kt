package org.maiaframework.gen.testing.jdbc.simple

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.sample.types.SomeStringType
import org.maiaframework.gen.testing.jdbc.AbstractJdbcTest
import org.maiaframework.gen.testing.sample.simple.*
import org.maiaframework.gen.testing.sample.types.SomeIntType
import org.maiaframework.jdbc.EntityNotFoundException
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyBoolean
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


class SimpleDaoTest : AbstractJdbcTest() {


    @Autowired
    private lateinit var simpleDao: SimpleDao


    @BeforeEach
    fun beforeEach() {

        this.simpleDao.deleteAll()

    }



    @Test
    fun `test insert and findById`() {

        val simpleEntity = SimpleEntityTestBuilder(lastModifiedTimestampUtc = Instant.now().truncatedTo(ChronoUnit.MILLIS)).build()

        this.simpleDao.insert(simpleEntity)

        val actual = this.simpleDao.findByPrimaryKey(simpleEntity.id)

        assertThat(actual).isNotNull()

        assertThat(actual.id).isEqualTo(simpleEntity.id)
        assertThat(actual.createdTimestampUtc).isEqualTo(simpleEntity.createdTimestampUtc)
        assertThat(actual.lastModifiedTimestampUtc).isEqualTo(simpleEntity.lastModifiedTimestampUtc)

        assertEntityFields(actual, simpleEntity)

    }


    @Test
    fun `test findOneOrNullBySomeString`() {

        //GIVEN
        val someString = anyAlphaNumeric()
        val simpleEntity = SimpleEntityTestBuilder(someString = someString).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actualOrNull = this.simpleDao.findOneOrNullBySomeString(someString)

        //THEN
        val actual = actualOrNull ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        // AND WHEN
        val shouldBeNull = this.simpleDao.findOneOrNullBySomeString(UUID.randomUUID().toString())

        // THEN
        assertThat(shouldBeNull).isNull()

        val deleteResult = this.simpleDao.deleteBySomeString(someString)

        assertThat(deleteResult).isTrue()

        val shouldBeNullAfterDelete = this.simpleDao.findByPrimaryKeyOrNull(simpleEntity.id)
        assertThat(shouldBeNullAfterDelete).isNull()

    }


    @Test
    fun `test deleteBy for unique index field`() {

        //GIVEN
        val someString = anyAlphaNumeric()
        val simpleEntity = SimpleEntityTestBuilder(someString = someString).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actualOrNull = this.simpleDao.findOneOrNullBySomeString(someString)

        //THEN
        val actual = actualOrNull ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        val deleteResult = this.simpleDao.deleteBySomeString(someString)

        assertThat(deleteResult).isTrue()

        val shouldBeNullAfterDelete = this.simpleDao.findByPrimaryKeyOrNull(simpleEntity.id)
        assertThat(shouldBeNullAfterDelete).isNull()

    }


    @Test
    fun testFindOneOrNullBySomeStringType() {

        //GIVEN
        val simpleEntity = SimpleEntityTestBuilder().build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actualOptional = this.simpleDao.findOneOrNullBySomeStringType(simpleEntity.someStringType)

        //THEN
        val actual = actualOptional ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        // AND WHEN
        val shouldBeEmptyOptional = this.simpleDao.findOneOrNullBySomeStringType(SomeStringType(UUID.randomUUID().toString()))

        // THEN
        assertThat(shouldBeEmptyOptional).isNull()

    }


    @Test
    fun testFindOneBySomeString() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someString = someString).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actual = this.simpleDao.findOneBySomeString(someString)

        //THEN
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        try {
            this.simpleDao.findOneBySomeString(UUID.randomUUID().toString())
            fail<Any>("Should have thrown an EntityNotFoundException")
        } catch (e: EntityNotFoundException) {
            assertThat(e.tableName).isEqualTo(SimpleEntityMeta.TABLE_NAME)
        }

    }


    @Test
    fun testFindOneBySomeOrNullUniqueString() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someStringNullable = someString).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actual = this.simpleDao.findOneBySomeStringNullable(someString)

        //THEN
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        assertThatThrownBy {
            this.simpleDao.findOneBySomeStringNullable(UUID.randomUUID().toString())
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(SimpleEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun testFindOneOrNullBySomeOptionalUniqueString() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someStringNullable = someString).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actual = this.simpleDao.findOneOrNullBySomeStringNullable(someString)

        //THEN
        assertThat(actual?.id).isEqualTo(simpleEntity.id)

        val shouldBeNull = this.simpleDao.findOneOrNullBySomeStringNullable(UUID.randomUUID().toString())
        assertThat(shouldBeNull).isNull()

    }


    @Test
    fun testFindOneOrNullBySomeOptionalUniqueStringThatIsEmpty() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someStringNullable = null).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val shouldBeNull = this.simpleDao.findOneOrNullBySomeStringNullable(someString)

        //THEN
        assertThat(shouldBeNull).isNull()

    }


    @Test
    fun testFindOneBySomeOrNullUniqueStringThatIsEmpty() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someStringNullable = null).build()
        this.simpleDao.insert(simpleEntity)

        assertThatThrownBy {
            this.simpleDao.findOneBySomeStringNullable(someString)
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(SimpleEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun testFindOneBySomeUniqueStringTypeField() {

        //GIVEN
        val simpleEntity = SimpleEntityTestBuilder().build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actual = this.simpleDao.findOneBySomeStringType(simpleEntity.someStringType)

        //THEN
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        assertThatThrownBy {
            this.simpleDao.findOneBySomeStringType(SomeStringType(UUID.randomUUID().toString()))
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(SimpleEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun testExistsBySomeUniqueField() {

        //GIVEN
        val simpleEntity = SimpleEntityTestBuilder().build()
        this.simpleDao.insert(simpleEntity)

        //THEN
        assertThat(this.simpleDao.existsBySomeStringType(simpleEntity.someStringType)).isTrue()
        assertThat(this.simpleDao.existsBySomeStringType(SomeStringType(UUID.randomUUID().toString()))).isFalse()

    }


    @Test
    fun testExistsBySomeOrNullUniqueField() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someStringNullable = someString).build()
        this.simpleDao.insert(simpleEntity)

        //THEN
        assertThat(this.simpleDao.existsBySomeStringNullable(someString)).isTrue()
        assertThat(this.simpleDao.existsBySomeStringNullable(UUID.randomUUID().toString())).isFalse()

    }


    @Test
    fun testExistsBySomeOrNullUniqueFieldThatIsEmpty() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someStringNullable = null).build()
        this.simpleDao.insert(simpleEntity)

        //THEN
        assertThat(this.simpleDao.existsBySomeStringNullable(someString)).isFalse()

    }


    @Test
    fun testUpsertBySomeNonNullableField() {

        val simpleEntityTestBuilder1 = SimpleEntityTestBuilder()
        val simpleEntityOriginal = simpleEntityTestBuilder1.build()

        val actualOriginal = this.simpleDao.upsertBySomeString(simpleEntityOriginal)
        assertThat(actualOriginal.id).isEqualTo(simpleEntityOriginal.id)
        assertEntityFields(actualOriginal, simpleEntityOriginal)

        val actualOriginalDirectFromDb = this.simpleDao.findByPrimaryKey(simpleEntityOriginal.id)
        assertEntityFields(actualOriginalDirectFromDb, actualOriginal)

        val expectedUpdatedEntity = simpleEntityTestBuilder1.copy(
                id = DomainId.newId(),
                createdTimestampUtc = actualOriginal.createdTimestampUtc.plusSeconds(1),
                someIntModifiable = actualOriginal.someIntModifiable + 1,
                someBoolean = actualOriginal.someBoolean == false
        ).build()

        val actualUpdated = this.simpleDao.upsertBySomeString(expectedUpdatedEntity)
        assertThat(actualUpdated.id).isEqualTo(simpleEntityOriginal.id)
        assertThat(actualUpdated.id).isNotEqualTo(expectedUpdatedEntity.id)
        assertThat(actualUpdated.createdTimestampUtc).isNotEqualTo(expectedUpdatedEntity.createdTimestampUtc)
        assertThat(actualUpdated.someIntModifiable).isEqualTo(expectedUpdatedEntity.someIntModifiable)
        assertThat(actualUpdated.someBoolean).isEqualTo(actualOriginal.someBoolean)

    }


    @Test
    fun testUpsertBySomeStringType() {

        val simpleEntityTestBuilder = SimpleEntityTestBuilder()
        val simpleEntityOriginal = simpleEntityTestBuilder.build()

        val actualOriginal = this.simpleDao.upsertBySomeStringType(simpleEntityOriginal)
        assertThat(actualOriginal.id).isEqualTo(simpleEntityOriginal.id)
        assertEntityFields(actualOriginal, simpleEntityOriginal)

        val actualOriginalDirectFromDb = this.simpleDao.findByPrimaryKey(simpleEntityOriginal.id)
        assertEntityFields(actualOriginalDirectFromDb, actualOriginal)

        val expectedUpdatedEntity = simpleEntityTestBuilder.copy(
                id = DomainId.newId(),
                createdTimestampUtc =  actualOriginal.createdTimestampUtc.plusSeconds(1),
                someIntModifiable =  actualOriginal.someIntModifiable + 1,
                someBoolean = actualOriginal.someBoolean == false)
                .build()

        val actualUpdated = this.simpleDao.upsertBySomeStringType(expectedUpdatedEntity)
        assertThat(actualUpdated.id).isEqualTo(simpleEntityOriginal.id)
        assertThat(actualUpdated.id).isNotEqualTo(expectedUpdatedEntity.id)
        assertThat(actualUpdated.createdTimestampUtc).isNotEqualTo(expectedUpdatedEntity.createdTimestampUtc)
        assertThat(actualUpdated.someIntModifiable).isEqualTo(expectedUpdatedEntity.someIntModifiable)
        assertThat(actualUpdated.someBoolean).isEqualTo(actualOriginal.someBoolean)

    }


    @Test
    fun testCountBySomeStringTypeField() {

        val someStringType = SomeStringType(anyString())

        this.simpleDao.insert(SimpleEntityTestBuilder(someStringType = someStringType).build())

        val actualCount = this.simpleDao.count(SimpleEntityFilters().someStringType eq someStringType)

        assertThat(actualCount).isEqualTo(1)

    }


    @Test
    fun testCountBySomeString() {

        val someString = anyString()

        this.simpleDao.insert(SimpleEntityTestBuilder(someString = someString).build())

        val actualCount = this.simpleDao.count(SimpleEntityFilters().someString eq someString)

        assertThat(actualCount).isEqualTo(1)

    }


    @Test
    fun `test fetchForEdit`() {

        val simpleEntity = SimpleEntityTestBuilder().build()

        this.simpleDao.insert(simpleEntity)

        val fetchForEditDto = this.simpleDao.fetchForEdit(simpleEntity.id)

        assertThat(fetchForEditDto.someString).isEqualTo(simpleEntity.someString)


    }

    @Test
    fun testFindByIndex() {

        //GIVEN
        val someString = anyString()
        val someBoolean = anyBoolean()
        val simpleEntity1 = SimpleEntityTestBuilder(someStringModifiable = someString, someBoolean = someBoolean).build()
        val simpleEntity2 = SimpleEntityTestBuilder(someStringModifiable = someString, someBoolean = someBoolean).build()
        val simpleEntity3 = SimpleEntityTestBuilder(someStringModifiable = someString, someBoolean = someBoolean == false).build()

        this.simpleDao.insert(simpleEntity1)
        this.simpleDao.insert(simpleEntity2)
        this.simpleDao.insert(simpleEntity3)

        val expectedIds = setOf(simpleEntity1.id, simpleEntity2.id)

        //WHEN
        val actual = this.simpleDao.findBySomeBooleanAndSomeStringModifiable(someBoolean, someString)

        // THEN
        val actualIds = actual.map { it.id }.toSet()
        assertThat(actualIds).isEqualTo(expectedIds)

    }


    @Test
    fun testUpdate() {

        val entityBuilder = SimpleEntityTestBuilder()
        val entityBeforeUpdate = entityBuilder.build()
        val id = entityBeforeUpdate.id
        val modifiedInstant = anyInstant().minusSeconds(10)
        val modifiedString = UUID.randomUUID().toString()

        val expectedEntity = entityBuilder.copy(
                someInstantModifiable = modifiedInstant,
                someStringModifiable = modifiedString,
                someInstantModifiableNullable = null
        ).build()


        this.simpleDao.insert(entityBeforeUpdate)

        val entityUpdater = SimpleEntityUpdater.forPrimaryKey(id) {
            someInstantModifiable(modifiedInstant)
            someStringModifiable(modifiedString)
            someInstantModifiableNullable(null)
        }

        this.simpleDao.setFields(entityUpdater)

        val entityAfterUpdate = this.simpleDao.findByPrimaryKey(id)

        assertEntityFields(entityAfterUpdate, expectedEntity)

    }


    @Test
    fun `test findAllAsSequence`() {

        val simpleEntity1 = SimpleEntityTestBuilder().build()
        val simpleEntity2 = SimpleEntityTestBuilder().build()
        val simpleEntity3 = SimpleEntityTestBuilder().build()

        this.simpleDao.bulkInsert(listOf(simpleEntity1, simpleEntity2, simpleEntity3))

        val actual = this.simpleDao.findAllAsSequence().toList()

        assertThat(actual).hasSize(3)
        assertEntityFields(actual[0], simpleEntity1)
        assertEntityFields(actual[1], simpleEntity2)
        assertEntityFields(actual[2], simpleEntity3)

    }


    @Test
    fun `test findAllBy(filter, pageRequest)`() {

        val simpleEntity1 = SimpleEntityTestBuilder().copy(someInt = 1).build()
        val simpleEntity2 = SimpleEntityTestBuilder().copy(someInt = 2).build()
        val simpleEntity3 = SimpleEntityTestBuilder().copy(someInt = 3).build()
        val simpleEntity4 = SimpleEntityTestBuilder().copy(someInt = 4).build()
        val simpleEntity5 = SimpleEntityTestBuilder().copy(someInt = 5).build()

        this.simpleDao.bulkInsert(listOf(simpleEntity1, simpleEntity2, simpleEntity3, simpleEntity4, simpleEntity5))

        val filterForSomeString1 = SimpleEntityFilters().someInt lt 4

        val sort = Sort.by(Sort.Order.desc(SimpleEntityMeta.someInt))
        val pageRequest1 = PageRequest.of(0, 3, sort)
        val searchResult1 = this.simpleDao.findAllBy(filterForSomeString1, pageRequest1)

        assertThat(searchResult1).hasSize(3)
        assertEntityFields(searchResult1[2], simpleEntity1)
        assertEntityFields(searchResult1[1], simpleEntity2)
        assertEntityFields(searchResult1[0], simpleEntity3)

    }


    @Test
    fun `test for unique index violation`() {

        val simpleEntity = SimpleEntityTestBuilder().build()

        this.simpleDao.insert(simpleEntity)

        assertThatThrownBy {
            this.simpleDao.insert(simpleEntity)
        }.isInstanceOf(DuplicateKeyException::class.java)
            .hasMessageContaining("simple_pkey")

    }


    @Test
    fun `test the contains filter on listOfStrings field`() {


        val simpleEntity = SimpleEntityTestBuilder().build()

        this.simpleDao.insert(simpleEntity)

        val firstString = simpleEntity.someListOfStrings.first()
        val filter = SimpleEntityFilters().someListOfStrings contains firstString

        val foundEntities = this.simpleDao.findAllBy(filter)

        assertThat(foundEntities).hasSize(1)
        val foundEntity = foundEntities.first()

        assertEntityFields(foundEntity, simpleEntity)

        assertThat(this.simpleDao.findAllBy(SimpleEntityFilters().someListOfStrings contains anyString())).hasSize(0)

    }


    @Test
    fun `test the isNull filter`() {

        val simpleEntityWithNull = SimpleEntityTestBuilder()
            .copy(someIntTypeNullable = null)
            .build()

        val simpleEntityWithNonNull = SimpleEntityTestBuilder()
            .copy(someIntTypeNullable = SomeIntType(anyInt()))
            .build()

        this.simpleDao.bulkInsert(listOf(simpleEntityWithNull, simpleEntityWithNonNull))

        val filterForIsNull = SimpleEntityFilters().someIntTypeNullable.isNull()

        val foundEntitiesForIsNull = this.simpleDao.findAllBy(filterForIsNull)

        assertThat(foundEntitiesForIsNull).hasSize(1)
        val foundEntityForIsNull = foundEntitiesForIsNull.first()

        assertEntityFields(foundEntityForIsNull, simpleEntityWithNull)

        val filterForIsNotNull = SimpleEntityFilters().someIntTypeNullable.isNotNull()

        val foundEntitiesNotNull = this.simpleDao.findAllBy(filterForIsNotNull)

        assertThat(foundEntitiesNotNull).hasSize(1)
        val foundEntityNotNull = foundEntitiesNotNull.first()

        assertEntityFields(foundEntityNotNull, simpleEntityWithNonNull)

    }


    @Test
    fun `test the 'in' clause of MultiValueFilter`() {

        val strings = listOf(anyString(), anyString(), anyString())

        val simpleEntity1 = SimpleEntityTestBuilder().copy(someString = strings[1]).build()
        val simpleEntity2 = SimpleEntityTestBuilder().build()

        this.simpleDao.bulkInsert(listOf(simpleEntity1, simpleEntity2))

        val filter = SimpleEntityFilters().someString.`in`(strings)

        val foundEntities = this.simpleDao.findAllBy(filter)

        assertThat(foundEntities).hasSize(1)

        assertEntityFields(foundEntities.first(), simpleEntity1)

    }

    private fun assertEntityFields(actual: SimpleEntity, expected: SimpleEntity) {

        assertThat(actual.someBoolean).isEqualTo(expected.someBoolean)
        assertThat(actual.someBooleanNullable).isEqualTo(expected.someBooleanNullable)
        assertThat(actual.someBooleanType).isEqualTo(expected.someBooleanType)
        assertThat(actual.someBooleanTypeNullable).isEqualTo(expected.someBooleanTypeNullable)
        assertThat(actual.someBooleanTypeProvided).isEqualTo(expected.someBooleanTypeProvided)
        assertThat(actual.someBooleanTypeProvidedNullable).isEqualTo(expected.someBooleanTypeProvidedNullable)
        assertThat(actual.someDto).isEqualTo(expected.someDto)
        assertThat(actual.someDtoNullable).isEqualTo(expected.someDtoNullable)
        assertThat(actual.someEnum).isEqualTo(expected.someEnum)
        assertThat(actual.someEnumNullable).isEqualTo(expected.someEnumNullable)
        assertThat(actual.someInstant).isEqualTo(expected.someInstant.truncatedTo(ChronoUnit.MILLIS))
        assertThat(actual.someInstantModifiable).isEqualTo(expected.someInstantModifiable.truncatedTo(ChronoUnit.MILLIS))
        assertThat(actual.someInstantModifiableNullable).isEqualTo(expected.someInstantModifiableNullable?.truncatedTo(ChronoUnit.MILLIS))
        assertThat(actual.someInstantNullable).isEqualTo(expected.someInstantNullable?.truncatedTo(ChronoUnit.MILLIS))
        assertThat(actual.someInt).isEqualTo(expected.someInt)
        assertThat(actual.someIntModifiable).isEqualTo(expected.someIntModifiable)
        assertThat(actual.someIntNullable).isEqualTo(expected.someIntNullable)
        assertThat(actual.someIntType).isEqualTo(expected.someIntType)
        assertThat(actual.someIntTypeNullable).isEqualTo(expected.someIntTypeNullable)
        assertThat(actual.someIntTypeProvided).isEqualTo(expected.someIntTypeProvided)
        assertThat(actual.someIntTypeProvidedNullable).isEqualTo(expected.someIntTypeProvidedNullable)
        assertThat(actual.someListOfEnums).isEqualTo(expected.someListOfEnums)
        assertThat(actual.someListOfInstants).isEqualTo(expected.someListOfInstants.map { it.truncatedTo(ChronoUnit.MILLIS) })
        assertThat(actual.someListOfLocalDates).isEqualTo(expected.someListOfLocalDates)
        assertThat(actual.someListOfPeriods).isEqualTo(expected.someListOfPeriods)
        assertThat(actual.someListOfStrings).isEqualTo(expected.someListOfStrings)
        assertThat(actual.someListOfStringTypes).isEqualTo(expected.someListOfStringTypes)
        assertThat(actual.someLocalDateModifiable).isEqualTo(expected.someLocalDateModifiable)
        assertThat(actual.someLongType).isEqualTo(expected.someLongType)
        assertThat(actual.someLongTypeNullable).isEqualTo(expected.someLongTypeNullable)
        assertThat(actual.someLongTypeProvided).isEqualTo(expected.someLongTypeProvided)
        assertThat(actual.someLongTypeProvidedNullable).isEqualTo(expected.someLongTypeProvidedNullable)
        assertThat(actual.someMapOfStringToInteger).isEqualTo(expected.someMapOfStringToInteger)
        assertThat(actual.someMapOfStringTypeToStringType).isEqualTo(expected.someMapOfStringTypeToStringType)
        assertThat(actual.somePeriodModifiable).isEqualTo(expected.somePeriodModifiable)
        assertThat(actual.somePeriodNullable).isEqualTo(expected.somePeriodNullable)
        assertThat(actual.someString).isEqualTo(expected.someString)
        assertThat(actual.someStringModifiable).isEqualTo(expected.someStringModifiable)
        assertThat(actual.someStringNullable).isEqualTo(expected.someStringNullable)
        assertThat(actual.someStringType).isEqualTo(expected.someStringType)
        assertThat(actual.someStringTypeNullable).isEqualTo(expected.someStringTypeNullable)
        assertThat(actual.someProvidedStringType).isEqualTo(expected.someProvidedStringType)
        assertThat(actual.someProvidedStringTypeNullable).isEqualTo(expected.someProvidedStringTypeNullable)

    }


}

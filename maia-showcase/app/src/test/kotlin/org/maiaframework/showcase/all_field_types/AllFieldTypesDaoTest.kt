package org.maiaframework.showcase.all_field_types

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.jdbc.EntityNotFoundException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.maiaframework.showcase.types.SomeIntType
import org.maiaframework.showcase.types.SomeStringType
import org.maiaframework.testing.domain.Anys.anyAlphaNumeric
import org.maiaframework.testing.domain.Anys.anyBoolean
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID


class AllFieldTypesDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var allFieldTypesDao: AllFieldTypesDao


    @BeforeEach
    fun beforeEach() {

        this.allFieldTypesDao.deleteAll()

    }



    @Test
    fun `test insert and findById`() {

        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(lastModifiedTimestampUtc = Instant.now().truncatedTo(ChronoUnit.MILLIS)).build()

        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        val actual = this.allFieldTypesDao.findByPrimaryKey(AllFieldTypesEntity.id)

        assertThat(actual).isNotNull()

        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)
        assertThat(actual.createdTimestampUtc).isEqualTo(AllFieldTypesEntity.createdTimestampUtc)
        assertThat(actual.lastModifiedTimestampUtc).isEqualTo(AllFieldTypesEntity.lastModifiedTimestampUtc)

        assertEntityFields(actual, AllFieldTypesEntity)

    }


    @Test
    fun `test findOneOrNullBySomeString`() {

        //GIVEN
        val someString = anyAlphaNumeric()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someString = someString).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actualOrNull = this.allFieldTypesDao.findOneOrNullBySomeString(someString)

        //THEN
        val actual = actualOrNull ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)

        // AND WHEN
        val shouldBeNull = this.allFieldTypesDao.findOneOrNullBySomeString(UUID.randomUUID().toString())

        // THEN
        assertThat(shouldBeNull).isNull()

        val deleteResult = this.allFieldTypesDao.deleteBySomeString(someString)

        assertThat(deleteResult).isTrue()

        val shouldBeNullAfterDelete = this.allFieldTypesDao.findByPrimaryKeyOrNull(AllFieldTypesEntity.id)
        assertThat(shouldBeNullAfterDelete).isNull()

    }


    @Test
    fun `test deleteBy for unique index field`() {

        //GIVEN
        val someString = anyAlphaNumeric()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someString = someString).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actualOrNull = this.allFieldTypesDao.findOneOrNullBySomeString(someString)

        //THEN
        val actual = actualOrNull ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)

        val deleteResult = this.allFieldTypesDao.deleteBySomeString(someString)

        assertThat(deleteResult).isTrue()

        val shouldBeNullAfterDelete = this.allFieldTypesDao.findByPrimaryKeyOrNull(AllFieldTypesEntity.id)
        assertThat(shouldBeNullAfterDelete).isNull()

    }


    @Test
    fun testFindOneOrNullBySomeStringType() {

        //GIVEN
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder().build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actualOptional = this.allFieldTypesDao.findOneOrNullBySomeStringType(AllFieldTypesEntity.someStringType)

        //THEN
        val actual = actualOptional ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)

        // AND WHEN
        val shouldBeEmptyOptional = this.allFieldTypesDao.findOneOrNullBySomeStringType(
            SomeStringType(
                UUID.randomUUID().toString()
            )
        )

        // THEN
        assertThat(shouldBeEmptyOptional).isNull()

    }


    @Test
    fun testFindOneBySomeString() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someString = someString).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actual = this.allFieldTypesDao.findOneBySomeString(someString)

        //THEN
        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)

        try {
            this.allFieldTypesDao.findOneBySomeString(UUID.randomUUID().toString())
            fail<Any>("Should have thrown an EntityNotFoundException")
        } catch (e: EntityNotFoundException) {
            assertThat(e.tableName).isEqualTo(AllFieldTypesEntityMeta.TABLE_NAME)
        }

    }


    @Test
    fun testFindOneBySomeOrNullUniqueString() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someStringNullable = someString).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actual = this.allFieldTypesDao.findOneBySomeStringNullable(someString)

        //THEN
        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)

        assertThatThrownBy {
            this.allFieldTypesDao.findOneBySomeStringNullable(UUID.randomUUID().toString())
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(AllFieldTypesEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun testFindOneOrNullBySomeOptionalUniqueString() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someStringNullable = someString).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actual = this.allFieldTypesDao.findOneOrNullBySomeStringNullable(someString)

        //THEN
        assertThat(actual?.id).isEqualTo(AllFieldTypesEntity.id)

        val shouldBeNull = this.allFieldTypesDao.findOneOrNullBySomeStringNullable(UUID.randomUUID().toString())
        assertThat(shouldBeNull).isNull()

    }


    @Test
    fun testFindOneOrNullBySomeOptionalUniqueStringThatIsEmpty() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someStringNullable = null).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val shouldBeNull = this.allFieldTypesDao.findOneOrNullBySomeStringNullable(someString)

        //THEN
        assertThat(shouldBeNull).isNull()

    }


    @Test
    fun testFindOneBySomeOrNullUniqueStringThatIsEmpty() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someStringNullable = null).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        assertThatThrownBy {
            this.allFieldTypesDao.findOneBySomeStringNullable(someString)
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(AllFieldTypesEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun testFindOneBySomeUniqueStringTypeField() {

        //GIVEN
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder().build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //WHEN
        val actual = this.allFieldTypesDao.findOneBySomeStringType(AllFieldTypesEntity.someStringType)

        //THEN
        assertThat(actual.id).isEqualTo(AllFieldTypesEntity.id)

        assertThatThrownBy {
            this.allFieldTypesDao.findOneBySomeStringType(SomeStringType(UUID.randomUUID().toString()))
        }.isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining(AllFieldTypesEntityMeta.TABLE_NAME.value)

    }


    @Test
    fun testExistsBySomeUniqueField() {

        //GIVEN
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder().build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //THEN
        assertThat(this.allFieldTypesDao.existsBySomeStringType(AllFieldTypesEntity.someStringType)).isTrue()
        assertThat(this.allFieldTypesDao.existsBySomeStringType(SomeStringType(UUID.randomUUID().toString()))).isFalse()

    }


    @Test
    fun testExistsBySomeOrNullUniqueField() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someStringNullable = someString).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //THEN
        assertThat(this.allFieldTypesDao.existsBySomeStringNullable(someString)).isTrue()
        assertThat(this.allFieldTypesDao.existsBySomeStringNullable(UUID.randomUUID().toString())).isFalse()

    }


    @Test
    fun testExistsBySomeOrNullUniqueFieldThatIsEmpty() {

        //GIVEN
        val someString = anyString()
        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder(someStringNullable = null).build()
        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        //THEN
        assertThat(this.allFieldTypesDao.existsBySomeStringNullable(someString)).isFalse()

    }


    @Test
    fun testUpsertBySomeNonNullableField() {

        val allFieldTypesEntityTestBuilder1 = AllFieldTypesEntityTestBuilder()
        val AllFieldTypesEntityOriginal = allFieldTypesEntityTestBuilder1.build()

        val actualOriginal = this.allFieldTypesDao.upsertBySomeString(AllFieldTypesEntityOriginal)
        assertThat(actualOriginal.id).isEqualTo(AllFieldTypesEntityOriginal.id)
        assertEntityFields(actualOriginal, AllFieldTypesEntityOriginal)

        val actualOriginalDirectFromDb = this.allFieldTypesDao.findByPrimaryKey(AllFieldTypesEntityOriginal.id)
        assertEntityFields(actualOriginalDirectFromDb, actualOriginal)

        val expectedUpdatedEntity = allFieldTypesEntityTestBuilder1.copy(
                id = DomainId.newId(),
                createdTimestampUtc = actualOriginal.createdTimestampUtc.plusSeconds(1),
                someIntModifiable = actualOriginal.someIntModifiable + 1,
                someBoolean = actualOriginal.someBoolean == false
        ).build()

        val actualUpdated = this.allFieldTypesDao.upsertBySomeString(expectedUpdatedEntity)
        assertThat(actualUpdated.id).isEqualTo(AllFieldTypesEntityOriginal.id)
        assertThat(actualUpdated.id).isNotEqualTo(expectedUpdatedEntity.id)
        assertThat(actualUpdated.createdTimestampUtc).isNotEqualTo(expectedUpdatedEntity.createdTimestampUtc)
        assertThat(actualUpdated.someIntModifiable).isEqualTo(expectedUpdatedEntity.someIntModifiable)
        assertThat(actualUpdated.someBoolean).isEqualTo(actualOriginal.someBoolean)

    }


    @Test
    fun testUpsertBySomeStringType() {

        val allFieldTypesEntityTestBuilder = AllFieldTypesEntityTestBuilder()
        val AllFieldTypesEntityOriginal = allFieldTypesEntityTestBuilder.build()

        val actualOriginal = this.allFieldTypesDao.upsertBySomeStringType(AllFieldTypesEntityOriginal)
        assertThat(actualOriginal.id).isEqualTo(AllFieldTypesEntityOriginal.id)
        assertEntityFields(actualOriginal, AllFieldTypesEntityOriginal)

        val actualOriginalDirectFromDb = this.allFieldTypesDao.findByPrimaryKey(AllFieldTypesEntityOriginal.id)
        assertEntityFields(actualOriginalDirectFromDb, actualOriginal)

        val expectedUpdatedEntity = allFieldTypesEntityTestBuilder.copy(
                id = DomainId.newId(),
                createdTimestampUtc =  actualOriginal.createdTimestampUtc.plusSeconds(1),
                someIntModifiable =  actualOriginal.someIntModifiable + 1,
                someBoolean = actualOriginal.someBoolean == false)
                .build()

        val actualUpdated = this.allFieldTypesDao.upsertBySomeStringType(expectedUpdatedEntity)
        assertThat(actualUpdated.id).isEqualTo(AllFieldTypesEntityOriginal.id)
        assertThat(actualUpdated.id).isNotEqualTo(expectedUpdatedEntity.id)
        assertThat(actualUpdated.createdTimestampUtc).isNotEqualTo(expectedUpdatedEntity.createdTimestampUtc)
        assertThat(actualUpdated.someIntModifiable).isEqualTo(expectedUpdatedEntity.someIntModifiable)
        assertThat(actualUpdated.someBoolean).isEqualTo(actualOriginal.someBoolean)

    }


    @Test
    fun testCountBySomeStringTypeField() {

        val someStringType = SomeStringType(anyString())

        this.allFieldTypesDao.insert(AllFieldTypesEntityTestBuilder(someStringType = someStringType).build())

        val actualCount = this.allFieldTypesDao.count(AllFieldTypesEntityFilters().someStringType eq someStringType)

        assertThat(actualCount).isEqualTo(1)

    }


    @Test
    fun testCountBySomeString() {

        val someString = anyString()

        this.allFieldTypesDao.insert(AllFieldTypesEntityTestBuilder(someString = someString).build())

        val actualCount = this.allFieldTypesDao.count(AllFieldTypesEntityFilters().someString eq someString)

        assertThat(actualCount).isEqualTo(1)

    }


    @Test
    fun `test fetchForEdit`() {

        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder().build()

        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        val fetchForEditDto = this.allFieldTypesDao.fetchForEdit(AllFieldTypesEntity.id)

        assertThat(fetchForEditDto.someString).isEqualTo(AllFieldTypesEntity.someString)


    }

    @Test
    fun testFindByIndex() {

        //GIVEN
        val someString = anyString()
        val someBoolean = anyBoolean()
        val AllFieldTypesEntity1 = AllFieldTypesEntityTestBuilder(someStringModifiable = someString, someBoolean = someBoolean).build()
        val AllFieldTypesEntity2 = AllFieldTypesEntityTestBuilder(someStringModifiable = someString, someBoolean = someBoolean).build()
        val AllFieldTypesEntity3 = AllFieldTypesEntityTestBuilder(someStringModifiable = someString, someBoolean = someBoolean == false).build()

        this.allFieldTypesDao.insert(AllFieldTypesEntity1)
        this.allFieldTypesDao.insert(AllFieldTypesEntity2)
        this.allFieldTypesDao.insert(AllFieldTypesEntity3)

        val expectedIds = setOf(AllFieldTypesEntity1.id, AllFieldTypesEntity2.id)

        //WHEN
        val actual = this.allFieldTypesDao.findBySomeBooleanAndSomeStringModifiable(someBoolean, someString)

        // THEN
        val actualIds = actual.map { it.id }.toSet()
        assertThat(actualIds).isEqualTo(expectedIds)

    }


    @Test
    fun testUpdate() {

        val entityBuilder = AllFieldTypesEntityTestBuilder()
        val entityBeforeUpdate = entityBuilder.build()
        val id = entityBeforeUpdate.id
        val modifiedInstant = anyInstant().minusSeconds(10)
        val modifiedString = UUID.randomUUID().toString()

        val expectedEntity = entityBuilder.copy(
                someInstantModifiable = modifiedInstant,
                someStringModifiable = modifiedString,
                someInstantModifiableNullable = null
        ).build()


        this.allFieldTypesDao.insert(entityBeforeUpdate)

        val entityUpdater = AllFieldTypesEntityUpdater.forPrimaryKey(id) {
            someInstantModifiable(modifiedInstant)
            someStringModifiable(modifiedString)
            someInstantModifiableNullable(null)
        }

        this.allFieldTypesDao.setFields(entityUpdater)

        val entityAfterUpdate = this.allFieldTypesDao.findByPrimaryKey(id)

        assertEntityFields(entityAfterUpdate, expectedEntity)

    }


    @Test
    fun `test findAllAsSequence`() {

        val AllFieldTypesEntity1 = AllFieldTypesEntityTestBuilder().build()
        val AllFieldTypesEntity2 = AllFieldTypesEntityTestBuilder().build()
        val AllFieldTypesEntity3 = AllFieldTypesEntityTestBuilder().build()

        this.allFieldTypesDao.bulkInsert(listOf(AllFieldTypesEntity1, AllFieldTypesEntity2, AllFieldTypesEntity3))

        val actual = this.allFieldTypesDao.findAllAsSequence().toList()

        assertThat(actual).hasSize(3)
        assertEntityFields(actual[0], AllFieldTypesEntity1)
        assertEntityFields(actual[1], AllFieldTypesEntity2)
        assertEntityFields(actual[2], AllFieldTypesEntity3)

    }


    @Test
    fun `test findAllBy(filter, pageRequest)`() {

        val AllFieldTypesEntity1 = AllFieldTypesEntityTestBuilder().copy(someInt = 1).build()
        val AllFieldTypesEntity2 = AllFieldTypesEntityTestBuilder().copy(someInt = 2).build()
        val AllFieldTypesEntity3 = AllFieldTypesEntityTestBuilder().copy(someInt = 3).build()
        val AllFieldTypesEntity4 = AllFieldTypesEntityTestBuilder().copy(someInt = 4).build()
        val AllFieldTypesEntity5 = AllFieldTypesEntityTestBuilder().copy(someInt = 5).build()

        this.allFieldTypesDao.bulkInsert(listOf(AllFieldTypesEntity1, AllFieldTypesEntity2, AllFieldTypesEntity3, AllFieldTypesEntity4, AllFieldTypesEntity5))

        val filterForSomeString1 = AllFieldTypesEntityFilters().someInt lt 4

        val sort = Sort.by(Sort.Order.desc(AllFieldTypesEntityMeta.someInt))
        val pageRequest1 = PageRequest.of(0, 3, sort)
        val searchResult1 = this.allFieldTypesDao.findAllBy(filterForSomeString1, pageRequest1)

        assertThat(searchResult1).hasSize(3)
        assertEntityFields(searchResult1[2], AllFieldTypesEntity1)
        assertEntityFields(searchResult1[1], AllFieldTypesEntity2)
        assertEntityFields(searchResult1[0], AllFieldTypesEntity3)

    }


    @Test
    fun `test for unique index violation`() {

        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder().build()

        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        assertThatThrownBy {
            this.allFieldTypesDao.insert(AllFieldTypesEntity)
        }.isInstanceOf(DuplicateKeyException::class.java)
            .hasMessageContaining("all_field_types_pkey")

    }


    @Test
    fun `test the contains filter on listOfStrings field`() {


        val AllFieldTypesEntity = AllFieldTypesEntityTestBuilder().build()

        this.allFieldTypesDao.insert(AllFieldTypesEntity)

        val firstString = AllFieldTypesEntity.someListOfStrings.first()
        val filter = AllFieldTypesEntityFilters().someListOfStrings contains firstString

        val foundEntities = this.allFieldTypesDao.findAllBy(filter)

        assertThat(foundEntities).hasSize(1)
        val foundEntity = foundEntities.first()

        assertEntityFields(foundEntity, AllFieldTypesEntity)

        assertThat(this.allFieldTypesDao.findAllBy(AllFieldTypesEntityFilters().someListOfStrings contains anyString())).hasSize(0)

    }


    @Test
    fun `test the isNull filter`() {

        val AllFieldTypesEntityWithNull = AllFieldTypesEntityTestBuilder()
            .copy(someIntTypeNullable = null)
            .build()

        val AllFieldTypesEntityWithNonNull = AllFieldTypesEntityTestBuilder()
            .copy(someIntTypeNullable = SomeIntType(anyInt()))
            .build()

        this.allFieldTypesDao.bulkInsert(listOf(AllFieldTypesEntityWithNull, AllFieldTypesEntityWithNonNull))

        val filterForIsNull = AllFieldTypesEntityFilters().someIntTypeNullable.isNull()

        val foundEntitiesForIsNull = this.allFieldTypesDao.findAllBy(filterForIsNull)

        assertThat(foundEntitiesForIsNull).hasSize(1)
        val foundEntityForIsNull = foundEntitiesForIsNull.first()

        assertEntityFields(foundEntityForIsNull, AllFieldTypesEntityWithNull)

        val filterForIsNotNull = AllFieldTypesEntityFilters().someIntTypeNullable.isNotNull()

        val foundEntitiesNotNull = this.allFieldTypesDao.findAllBy(filterForIsNotNull)

        assertThat(foundEntitiesNotNull).hasSize(1)
        val foundEntityNotNull = foundEntitiesNotNull.first()

        assertEntityFields(foundEntityNotNull, AllFieldTypesEntityWithNonNull)

    }


    @Test
    fun `test the 'in' clause of MultiValueFilter`() {

        val strings = listOf(anyString(), anyString(), anyString())

        val AllFieldTypesEntity1 = AllFieldTypesEntityTestBuilder().copy(someString = strings[1]).build()
        val AllFieldTypesEntity2 = AllFieldTypesEntityTestBuilder().build()

        this.allFieldTypesDao.bulkInsert(listOf(AllFieldTypesEntity1, AllFieldTypesEntity2))

        val filter = AllFieldTypesEntityFilters().someString.`in`(strings)

        val foundEntities = this.allFieldTypesDao.findAllBy(filter)

        assertThat(foundEntities).hasSize(1)

        assertEntityFields(foundEntities.first(), AllFieldTypesEntity1)

    }

    private fun assertEntityFields(actual: AllFieldTypesEntity, expected: AllFieldTypesEntity) {

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

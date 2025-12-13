package org.maiaframework.gen.testing.mongo.sample.simple

import org.maiaframework.dao.mongo.MongoClientFacade
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.mongo.DocumentNotFoundException
import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.gen.testing.mongo.sample.types.SomeStringType
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyBoolean
import org.maiaframework.testing.domain.Anys.anyString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.testng.annotations.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID


class SimpleDaoTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var simpleDao: SimpleDao

    @Autowired
    private lateinit var mongoClientFacade: MongoClientFacade


    @Test
    fun testInsertAndFindById() {

        val simpleEntity = SimpleEntityTestBuilder().build()

        this.simpleDao.insert(simpleEntity)

        val actual = this.simpleDao.findById(simpleEntity.id)

        assertThat(actual).isNotNull()

        assertThat(actual.id).isEqualTo(simpleEntity.id)
        assertThat(actual.createdTimestampUtc).isEqualTo(simpleEntity.createdTimestampUtc)

        assertEntityFields(actual, simpleEntity)

    }


    @Test
    fun testFindOneOrNullBySomeString() {

        //GIVEN
        val someString = anyString()
        val simpleEntity = SimpleEntityTestBuilder(someString = someString).build()
        this.simpleDao.insert(simpleEntity)

        //WHEN
        val actualOptional = this.simpleDao.findOneOrNullBySomeString(someString)

        //THEN
        val actual = actualOptional ?: throw AssertionError("Expected to find a result")
        assertThat(actual.id).isEqualTo(simpleEntity.id)

        // AND WHEN
        val shouldBeNull = this.simpleDao.findOneOrNullBySomeString(UUID.randomUUID().toString())

        // THEN
        assertThat(shouldBeNull).isNull()

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
            fail<Any>("Should have thrown an DocumentNotFoundException")
        } catch (e: DocumentNotFoundException) {
            assertThat(e.collectionName).isEqualTo(this.simpleDao.collectionName)
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

        try {
            this.simpleDao.findOneBySomeStringNullable(UUID.randomUUID().toString())
            fail<Any>("Should have thrown an DocumentNotFoundException")
        } catch (e: DocumentNotFoundException) {
            assertThat(e.collectionName).isEqualTo(this.simpleDao.collectionName)
        }

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

        try {
            this.simpleDao.findOneBySomeStringNullable(someString)
            fail<Any>("Should have thrown an DocumentNotFoundException")
        } catch (e: DocumentNotFoundException) {
            assertThat(e.collectionName).isEqualTo(this.simpleDao.collectionName)
        }

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

        try {
            this.simpleDao.findOneBySomeStringType(SomeStringType(UUID.randomUUID().toString()))
            fail<Any>("Should have thrown an DocumentNotFoundException")
        } catch (e: DocumentNotFoundException) {
            assertThat(e.collectionName).isEqualTo(this.simpleDao.collectionName)
        }

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
    fun should_ignore_legacy_attributes() {

        val simpleEntity = SimpleEntityTestBuilder().build()

        this.simpleDao.insert(simpleEntity)

        val collection = this.mongoClientFacade.getCollection(this.simpleDao.collectionName)

        collection
                .updateOne(
                        Document("_id", simpleEntity.id),
                        Document("\$set", Document("someLegacyField", "Some value"))
                                .append("\$set", Document("someLegacyDocument", Document("someLegacyEmbeddedField", "Some value").append("someLegacyEmbeddedArray", listOf("one", "two"))))
                                .append("\$set", Document("someLegacyArrayField", listOf("one", "two"))))

        this.simpleDao.findById(simpleEntity.id)

    }


    @Test
    fun testUpsertBySomeNonNullableField() {

        val simpleEntityTestBuilder1 = SimpleEntityTestBuilder()
        val simpleEntityOriginal = simpleEntityTestBuilder1.build()

        val actualOriginal = this.simpleDao.upsertBySomeString(simpleEntityOriginal)
        assertThat(actualOriginal.id).isEqualTo(simpleEntityOriginal.id)
        assertEntityFields(actualOriginal, simpleEntityOriginal)

        val actualOriginalDirectFromDb = this.simpleDao.findById(simpleEntityOriginal.id)
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

        val actualOriginalDirectFromDb = this.simpleDao.findById(simpleEntityOriginal.id)
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

        val actualCount = this.simpleDao.count(SimpleEntityFilters.someStringType().eq(someStringType))

        assertThat(actualCount).isEqualTo(1)

    }


    @Test
    fun testCountBySomeString() {

        val someString = Anys.anyString()

        this.simpleDao.insert(SimpleEntityTestBuilder(someString = someString).build())

        val actualCount = this.simpleDao.count(SimpleEntityFilters.someString().eq(someString))

        assertThat(actualCount).isEqualTo(1)

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
        val modifiedInstant = Instant.now().minusSeconds(10)
        val modifiedString = UUID.randomUUID().toString()

        val expectedEntity = entityBuilder.copy(
                someInstantModifiable = modifiedInstant,
                someStringModifiable = modifiedString,
                someInstantModifiableNullable = null
        ).build()


        this.simpleDao.insert(entityBeforeUpdate)

        val entityUpdater = SimpleEntityUpdater.forId(id) {
            someInstantModifiable(modifiedInstant)
            someStringModifiable(modifiedString)
            someInstantModifiableNullable(null)
        }.build()

        this.simpleDao.setFields(entityUpdater)

        val entityAfterUpdate = this.simpleDao.findById(id)

        assertEntityFields(entityAfterUpdate, expectedEntity)

    }


    private fun assertEntityFields(actual: SimpleEntity, expected: SimpleEntity) {

        assertThat(actual.someBoolean).isEqualTo(expected.someBoolean)
        assertThat(actual.someBooleanNullable).isEqualTo(expected.someBooleanNullable)
        assertThat(actual.someBooleanType).isEqualTo(expected.someBooleanType)
        assertThat(actual.someBooleanTypeNullable).isEqualTo(expected.someBooleanTypeNullable)
        assertThat(actual.someBooleanTypeProvided).isEqualTo(expected.someBooleanTypeProvided)
        assertThat(actual.someBooleanTypeProvidedNullable).isEqualTo(expected.someBooleanTypeProvidedNullable)
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
        assertThat(actual.someStringTypeProvided).isEqualTo(expected.someStringTypeProvided)
        assertThat(actual.someStringTypeProvidedNullable).isEqualTo(expected.someStringTypeProvidedNullable)

    }


}

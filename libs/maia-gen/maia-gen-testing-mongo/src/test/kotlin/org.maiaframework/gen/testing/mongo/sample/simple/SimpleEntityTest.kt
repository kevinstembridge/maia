package mahana.gen.testing.mongo.sample.simple


import org.maiaframework.domain.AbstractEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*


class SimpleEntityTest {


    //TODO should also confirm field type, not just name
    private val actualFieldNames: Set<String>
        get() {

            val declaredFields = SimpleEntity::class.java.declaredFields
            return declaredFields.map { it.name }.toSortedSet()

        }


    @Test
    fun should_extend_AbstractEntity() {

        assertThat(SimpleEntity::class.java.superclass).isEqualTo(AbstractEntity::class.java)

    }


    @Test
    fun testGetter() {

        val expectedSomeNonNullableString = UUID.randomUUID().toString()
        val simpleEntity = SimpleEntityTestBuilder(someString = expectedSomeNonNullableString).build()

        assertThat(simpleEntity.someString).isEqualTo(expectedSomeNonNullableString)

    }


    @Test
    fun testAllFieldsExist() {

        val expectedFieldNames = sortedSetOf(
            "lastModifiedTimestampUtc",
            "someString",
            "someStringNullable",
            "someInstant",
            "someInstantModifiable",
            "someIntModifiable",
            "somePeriodModifiable",
            "someStringModifiable",
            "someLocalDateModifiable",
            "someStringNullable",
            "someBoolean",
            "someEnum",
            "someListOfEnums",
            "someListOfInstants",
            "someListOfLocalDates",
            "someListOfPeriods",
            "someListOfStringTypes",
            "someListOfStrings",
            "someEnumNullable",
            "someBooleanNullable",
            "someIntNullable",
            "someInstantNullable",
            "somePeriodNullable",
            "someStringTypeNullable",
            "someStringTypeProvidedNullable",
            "someStringTypeProvided",
            "someStringType",
            "someIntType",
            "someInt",
            "someBooleanType",
            "someLongType",
            "someMapOfStringToInteger",
            "someMapOfStringTypeToStringType",
            "someIntTypeProvided",
            "someBooleanTypeProvided",
            "someLongTypeProvided",
            "someInstantModifiableNullable",
            "someIntTypeProvidedNullable",
            "someBooleanTypeProvidedNullable",
            "someLongTypeProvidedNullable",
            "someIntTypeNullable",
            "someBooleanTypeNullable",
            "someLongTypeNullable")

        val actualFieldNames = actualFieldNames.filterNot { it == "Companion" }.toSortedSet()

        assertThat(actualFieldNames).isEqualTo(expectedFieldNames)

        assertThat(actualFieldNames.contains("id")).isFalse()
        assertThat(actualFieldNames.contains("createdTimestampUtc")).isFalse()

    }


    @Test(dataProvider = "provideUnwantedSetterNames")
    fun should_not_have_setters_for_unmodifiable_fields(unwantedSetterName: String) {

        confirmMethodDoesNotExist(unwantedSetterName)

    }


    private fun confirmMethodDoesNotExist(unwantedMethodName: String) {

        val declaredMethods = SimpleEntity::class.java.declaredMethods

        for (method in declaredMethods) {

            if (method.name == unwantedMethodName) {
                fail<Any>("Class must not contain a method named $unwantedMethodName")
            }

        }

    }


    @DataProvider(name = "provideUnwantedSetterNames")
    fun provideUnwantedSetterNames(): Array<Array<String>> {

        return arrayOf(arrayOf("setSomeNonNullableString"), arrayOf("setSomeOptionalString"), arrayOf("setSomeBoolean"), arrayOf("setSomeStatus"))

    }


}

package mahana.gen.testing.mongo.sample.simple

import org.maiaframework.domain.DomainId
import org.maiaframework.gen.sample.types.SomeProvidedBooleanType
import org.maiaframework.gen.sample.types.SomeProvidedIntType
import org.maiaframework.gen.sample.types.SomeProvidedLongType
import org.maiaframework.gen.sample.types.SomeStringTypeProvided
import org.maiaframework.gen.testing.mongo.sample.types.SomeBooleanType
import org.maiaframework.gen.testing.mongo.sample.types.SomeIntType
import org.maiaframework.gen.testing.mongo.sample.types.SomeLongType
import org.maiaframework.gen.testing.mongo.sample.types.SomeStringType
import org.maiaframework.testing.domain.Anys.anyBoolean
import org.maiaframework.testing.domain.Anys.anyBooleanOrNull
import org.maiaframework.testing.domain.Anys.anyEnumOf
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyInstantOrNull
import org.maiaframework.testing.domain.Anys.anyInt
import org.maiaframework.testing.domain.Anys.anyIntOrNull
import org.maiaframework.testing.domain.Anys.anyLocalDate
import org.maiaframework.testing.domain.Anys.anyLong
import org.maiaframework.testing.domain.Anys.anyLongOrNull
import org.maiaframework.testing.domain.Anys.anyPeriod
import org.maiaframework.testing.domain.Anys.anyString
import org.maiaframework.testing.domain.Anys.anyStringOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.Period

data class SimpleEntityTestBuilder(
    val createdTimestampUtc: Instant = anyInstant(),
    val id: DomainId = DomainId.newId(),
    val someBoolean: Boolean = anyBoolean(),
    val someBooleanNullable: Boolean? = anyBooleanOrNull(),
    val someBooleanType: SomeBooleanType = SomeBooleanType(anyBoolean()),
    val someBooleanTypeNullable: SomeBooleanType? = anyBooleanOrNull()?.let { SomeBooleanType(it) },
    val someBooleanTypeProvided: SomeProvidedBooleanType = SomeProvidedBooleanType(anyBoolean()),
    val someBooleanTypeProvidedNullable: SomeProvidedBooleanType? = anyBooleanOrNull()?.let { SomeProvidedBooleanType(it) },
    val someEnum: SomeEnum = anyEnumOf<SomeEnum>(),
    val someEnumNullable: SomeEnum? = anyBooleanOrNull()?.let { anyEnumOf<SomeEnum>() },
    val someInstant: Instant = anyInstant(),
    val someInstantModifiable: Instant = anyInstant(),
    val someInstantModifiableNullable: Instant? = anyInstantOrNull(),
    val someInstantNullable: Instant? = anyInstantOrNull(),
    val someInt: Int = anyInt(),
    val someIntModifiable: Int = anyInt(),
    val someIntNullable: Int? = anyIntOrNull(),
    val someIntType: SomeIntType = SomeIntType(anyInt()),
    val someIntTypeNullable: SomeIntType? = anyIntOrNull()?.let { SomeIntType(it) },
    val someIntTypeProvided: SomeProvidedIntType = SomeProvidedIntType(anyInt()),
    val someIntTypeProvidedNullable: SomeProvidedIntType? = anyIntOrNull()?.let { SomeProvidedIntType(it) },
    val someListOfEnums: List<SomeEnum> = listOf(SomeEnum.NOT_OK, SomeEnum.OK),
    val someListOfInstants: List<Instant> = listOf(Instant.now(), Instant.now().plusSeconds(5)),
    val someListOfLocalDates: List<LocalDate> = listOf(LocalDate.now(), LocalDate.now().plusDays(5)),
    val someListOfPeriods: List<Period> = listOf(Period.of(1, 1, 1), Period.of(2, 0, 0)),
    val someListOfStrings: List<String> = listOf("string1", "string2"),
    val someListOfStringTypes: List<SomeStringType> = someListOfStrings.map { SomeStringType(it) },
    val someLocalDateModifiable: LocalDate = anyLocalDate(),
    val someLongType: SomeLongType = SomeLongType(anyLong()),
    val someLongTypeNullable: SomeLongType? = anyLongOrNull()?.let { SomeLongType(it) },
    val someLongTypeProvided: SomeProvidedLongType = SomeProvidedLongType(anyLong()),
    val someLongTypeProvidedNullable: SomeProvidedLongType? = anyBooleanOrNull()?.let { SomeProvidedLongType(anyLong()) },
    val someMapOfStringToInteger: Map<String, Int> = mapOf("one" to 1, "two" to 2),
    val someMapOfStringTypeToStringType: Map<SomeStringType, SomeStringType> = mapOf(
        SomeStringType("one") to SomeStringType(
            "two"
        )
    ),
    val somePeriodModifiable: Period = anyPeriod(),
    val somePeriodNullable: Period? = anyBooleanOrNull()?.let { anyPeriod() },
    val someString: String = anyString(),
    val someStringModifiable: String = anyString(),
    val someStringNullable: String? = null,
    val someStringType: SomeStringType = SomeStringType(anyString()),
    val someStringTypeNullable: SomeStringType? = anyStringOrNull()?.let { SomeStringType(it) },
    val someStringTypeProvided: SomeStringTypeProvided = SomeStringTypeProvided(anyString()),
    val someStringTypeProvidedNullable: SomeStringTypeProvided? = anyStringOrNull()?.let { SomeStringTypeProvided(it) }
) {


    fun build(): SimpleEntity {

        return SimpleEntity(
            this.createdTimestampUtc,
            this.id,
            this.someBoolean,
            this.someBooleanNullable,
            this.someBooleanType,
            this.someBooleanTypeNullable,
            this.someBooleanTypeProvided,
            this.someBooleanTypeProvidedNullable,
            this.someEnum,
            this.someEnumNullable,
            this.someInstant,
            this.someInstantModifiable,
            this.someInstantModifiableNullable,
            this.someInstantNullable,
            this.someInt,
            this.someIntModifiable,
            this.someIntNullable,
            this.someIntType,
            this.someIntTypeNullable,
            this.someIntTypeProvided,
            this.someIntTypeProvidedNullable,
            this.someListOfEnums,
            this.someListOfInstants,
            this.someListOfLocalDates,
            this.someListOfPeriods,
            this.someListOfStringTypes,
            this.someListOfStrings,
            this.someLocalDateModifiable,
            this.someLongType,
            this.someLongTypeNullable,
            this.someLongTypeProvided,
            this.someLongTypeProvidedNullable,
            this.someMapOfStringToInteger,
            this.someMapOfStringTypeToStringType,
            this.somePeriodModifiable,
            this.somePeriodNullable,
            this.someString,
            this.someStringModifiable,
            this.someStringNullable,
            this.someStringType,
            this.someStringTypeNullable,
            this.someStringTypeProvided,
            this.someStringTypeProvidedNullable
        )

    }


}

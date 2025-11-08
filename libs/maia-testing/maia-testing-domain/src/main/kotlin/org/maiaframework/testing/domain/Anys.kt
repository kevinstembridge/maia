package org.maiaframework.testing.domain

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.auth.EncryptedPassword
import org.maiaframework.domain.contact.EmailAddress
import org.maiaframework.domain.country.CountryCodeAlpha3
import org.maiaframework.domain.net.IpAddress
import org.maiaframework.domain.party.FirstName
import org.maiaframework.domain.party.LastName
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object Anys {

    val defaultCreatedById: DomainId = DomainId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    private val random = Random(System.currentTimeMillis())

    private val firstNames = listOf("Nigel", "Albert", "Philomena", "Charlie", "David", "Jack", "Roger").map { FirstName(it) }

    private val lastNames = listOf("Nigelson", "Albertson", "Phillips", "Charleston", "Davidson", "Jackson", "Rogers").map { LastName(it) }

    val orgNames = listOf("Mega Corp", "Acme", "Big Deal Ltd")
    val otherOrgTypes = listOf("Some Org Type 1", "Some Org Type 2", "Some Org Type 3")
    val domainNames = listOf("megacorp.com", "acme.com", "mumanddad.org", "bigdeal.com", "internet.net", "disorganised.org")
    val addressLine1s = listOf("1 Orange St", "34 High St", "47 Downtown Rd", "132 Mahana Rd", "78 Lauriston Rd", "4 Goodness Dr")
    val addressLine2s = listOf("Downtown", "Uptown", "Midtown", "Westside", "Eastside")
    val addressLine3s = listOf("Downtown", "Uptown", "Midtown", "Westside", "Eastside")
    val cities = listOf("Auckland", "Kingsport", "London", "New York", "Brevard", "Bedrock", "Ngatea", "Earlsfield")
    val countries = listOf("United Kingdom", "United States", "New Zealand", "Thailand", "Costa Rica", "Uruguay", "France", "Malta")
    val countryCodes = listOf("en", "us", "nz", "es", "fr")
    val countryCodeAlpha3s = listOf("GBR", "USA", "NZL", "ESP", "FRA").map { CountryCodeAlpha3(it) }
    val postalCodes = listOf("47660", "28712", "E9 7LH", "SW18 3PY", "3210")
    val usStates = listOf("AL", "TN", "NC", "NY", "VA", "VT", "KY")

    private val hexadecimalCharacters = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")
    private val alphaCharacters = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
    private val digits = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    private val alphaNumericCharacters = alphaCharacters.plus(digits.map { it.toString() })


    fun anyBoolean(): Boolean {

        return this.random.nextBoolean()

    }


    fun anyBooleanOrNull(): Boolean? {

        return if (this.random.nextBoolean()) this.random.nextBoolean() else null

    }


    fun anyFirstName(): FirstName {

        return anyOf(firstNames)

    }


    fun anyLastName(): LastName {

        return anyOf(lastNames)

    }


    fun anyFirstAndLastName(): String {

        return "${anyOf(firstNames)} ${anyLastName()}"

    }


    fun anyDomainName(): String {

        return anyOf(domainNames)

    }


    fun anyOrgWebsiteURL(): URL {

        return URL("https://${anyOf(domainNames)}")

    }


    fun anyOtherOrgType(): String {

        return anyOf(otherOrgTypes)

    }


    fun anyInstantOrNull(): Instant? {

        return if (anyBoolean()) now() else null

    }


    fun anyInstant(): Instant {

        return now().minusMillis(anyLongBound(500_000_000))

    }


    fun anyPastInstantWithin(pastPeriod: Period): Instant {

        val randomPastPeriodInDays = randomPeriodInDays(pastPeriod)

        return now().minus(randomPastPeriodInDays, ChronoUnit.DAYS)

    }


    fun anyPastInstantWithin(pastPeriod: Period, dateTimeFormatter: DateTimeFormatter): String {

        val pastInstant = anyPastInstantWithin(pastPeriod)
        return dateTimeFormatter.format(pastInstant)

    }


    fun anyFutureInstantWithin(futurePeriod: Period): Instant {

        val randomPeriodInDays = randomPeriodInDays(futurePeriod)
        return now().plus(randomPeriodInDays, ChronoUnit.DAYS)

    }


    private fun now(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)


    fun anyFutureInstantWithin(pastPeriod: Period, dateTimeFormatter: DateTimeFormatter): String {

        val pastLocalDate = anyFutureLocalDateWithin(pastPeriod)
        return pastLocalDate.format(dateTimeFormatter)

    }


    @SafeVarargs
    fun <T> anyOf(vararg values: T): T {

        return anyOf(listOf(*values))

    }


    fun <T> anyOf(values: List<T>): T {

        return values[random.nextInt(values.size)]

    }


    fun <T> anyOf(values: Set<T>): T {

        return values.toList()[random.nextInt(values.size)]

    }


    fun <T> anyExcept(exception: T, vararg candidates: T): T {

        return anyExcept(exception, listOf(*candidates))

    }


    fun <T> anyExcept(exception: T, candidates: List<T>): T {

        if (candidates.isEmpty() || candidates.size == 1) {
            throw RuntimeException("candidate list is too small: $candidates")
        }

        val candidate = anyOf(candidates)

        return if (candidate == exception) {
            anyExcept(exception, candidates)
        } else {
            candidate
        }

    }


    fun <T> anyExcept(exceptions: Set<T>, candidates: Set<T>): T {

        if (candidates.isEmpty() || candidates.size == 1) {
            throw RuntimeException("candidate list is too small: $candidates")
        }

        if (candidates == exceptions) {
            throw RuntimeException("The set of exceptions cannot be the same as the set of candidates: $candidates")
        }

        val candidate: T = anyOf(candidates)

        return if (exceptions.contains(candidate)) {
            anyExcept(exceptions, candidates)
        } else {
            candidate
        }

    }


    fun anyEmailAddress(
        firstName: FirstName,
        lastName: LastName
    ): EmailAddress {

        return EmailAddress("$firstName.$lastName@${anyOf(domainNames)}")

    }


    fun anyEmailAddress(): EmailAddress {

        return EmailAddress("${anyFirstName()}.${anyLastName()}@${anyOf(domainNames)}")

    }


    fun anyUniqueEmailAddress(): EmailAddress {

        return EmailAddress("${UUID.randomUUID()}@unique.com")

    }


    fun anyFirstNameExcept(firstName: FirstName): FirstName {

        return anyExcept(firstName, firstNames)

    }


    fun anyLastNameExcept(lastName: LastName): LastName {

        return anyExcept(lastName, lastNames)

    }


    fun anyEmailAddressExcept(exception: EmailAddress): EmailAddress {

        val candidate = anyEmailAddress()

        return if (candidate == exception) {
            anyEmailAddressExcept(exception)
        } else {
            candidate
        }

    }


    fun anyAddressLine1(): String {

        return anyOf(addressLine1s)

    }


    fun anyAddressLine2(): String {

        return anyOf(addressLine2s)

    }


    fun anyAddressLine3(): String {

        return anyOf(addressLine3s)

    }


    fun anyCity(): String {

        return anyOf(cities)

    }


    fun anyCountry(): String {

        return anyOf(countries)

    }


    fun anyCountryCodeAlpha3(): CountryCodeAlpha3 {

        return anyOf(countryCodeAlpha3s)

    }


    fun anyCountryCode(): String {

        return anyOf(countryCodes)

    }


    fun anyAddressState(): String {

        return anyOf(usStates)

    }


    fun anyPostalCode(): String {

        return anyOf(postalCodes)

    }


    fun anyAddressLine1Except(exception: String): String {

        return anyExcept(exception, addressLine1s)

    }


    fun anyCityExcept(exception: String): String {

        return anyExcept(exception, cities)

    }


    fun anyAddressStateExcept(exception: String): String {

        return anyExcept(exception, usStates)

    }


    fun anyCountryExcept(exception: String): String {

        return anyExcept(exception, countries)

    }


    fun anyCountryCodeExcept(exception: String): String {

        return anyExcept(exception, countryCodes)

    }


    fun anyPostalCodeExcept(exception: String): String {

        return anyExcept(exception, postalCodes)

    }


    fun anyPassword(): String {

        return "password_" + System.currentTimeMillis()

    }


    fun anyEncryptedPassword(): EncryptedPassword {

        return EncryptedPassword("password_" + System.currentTimeMillis())

    }


    fun anyPastYearWithin(yearsAgo: Int): Int {

        return LocalDate.now().year - random.nextInt(yearsAgo)

    }


    fun anyLocalDate(): LocalDate {

        return LocalDate.now().minusDays(anyIntBound(500).toLong())

    }


    fun anyPastLocalDateWithin(pastPeriod: Period): LocalDate {

        val randomPastPeriodInDays = randomPeriodInDays(pastPeriod)

        return LocalDate.now().minusDays(randomPastPeriodInDays)

    }


    fun anyPastLocalDateWithin(pastPeriod: Period, dateTimeFormatter: DateTimeFormatter): String {

        val pastLocalDate = anyPastLocalDateWithin(pastPeriod)
        return pastLocalDate.format(dateTimeFormatter)

    }


    fun anyFutureLocalDateWithin(futurePeriod: Period): LocalDate {

        val randomPeriodInDays = randomPeriodInDays(futurePeriod)
        return LocalDate.now().plusDays(randomPeriodInDays)

    }


    fun anyFutureLocalDateWithin(futurePeriod: Period, dateTimeFormatter: DateTimeFormatter): String {

        val futureLocalDate = anyFutureLocalDateWithin(futurePeriod)
        return futureLocalDate.format(dateTimeFormatter)

    }


    private fun randomPeriodInDays(pastPeriod: Period): Long {

        val today = LocalDate.now()
        val pastDate = LocalDate.now().minus(pastPeriod)
        val pastPeriodInDays = ChronoUnit.DAYS.between(pastDate, today).toInt()
        return random.nextInt(pastPeriodInDays).toLong()

    }


    fun anyString(length: Int = anyIntPositiveBound(10)): String {

        return anyAlphaNumeric(length)

    }


    fun anyStringOrNull(length: Int = anyIntPositiveBound(10)): String? {

        return if (anyBoolean()) anyAlphaNumeric(length) else null

    }


    fun anyAlphaNumeric(length: Int = anyIntPositiveBound(10)): String {

        return sequenceFrom { randomAlphanumeric() }.take(length).joinToString("")

    }


    fun anyHexidecimalOfLength(length: Int): String {

        return sequenceFrom { randomHexidecimalChar() }.take(length).joinToString("")

    }


    fun anyNumericOfLength(length: Int): String {

        return sequenceFrom { randomDigit() }.take(length).joinToString("")

    }


    private fun <T> sequenceFrom(fn: () -> T) = sequence {

        // this sequence is infinite
        while (true) {
            yield(fn())
        }

    }


    private fun randomAlphanumeric(): String {

        return randomElementFrom(alphaNumericCharacters)

    }


    private fun randomHexidecimalChar(): String {

        return randomElementFrom(hexadecimalCharacters)

    }


    fun anyAlphaChar(): String {

        return randomElementFrom(alphaCharacters)

    }


    private fun randomDigit(): Int {

        return randomElementFrom(digits)

    }


    private fun <T> randomElementFrom(list: List<T>): T {

        val index = random.nextInt(list.size)
        return list[index]

    }


    fun anyInt(): Int {

        return this.random.nextInt()

    }


    fun anyIntBound(bound: Int): Int {

        return this.random.nextInt(bound)

    }


    fun anyLongBound(bound: Long): Long {

        return this.random.nextLong(bound)

    }


    fun anyIntPositiveBound(bound: Int): Int {

        val nextInt = this.random.nextInt(bound)
        return if (nextInt == 0) 1 else nextInt

    }


    fun anyIntOrNull(): Int? {

        return if (anyBoolean()) this.random.nextInt() else null

    }


    fun anyLong(): Long {

        return this.random.nextLong()

    }


    fun anyLongOrNull(): Long? {

        return if (anyBoolean()) anyLong() else null

    }


    fun anyIntOfLength(length: Int): Int {

        return anyNumericOfLength(length).toInt()

    }


    inline fun <reified T: Enum<*>> anyEnumOf(): T {

        return anyOf(*T::class.java.enumConstants)

    }


    fun anyIpAddress(): IpAddress {

        val first = this.random.nextInt(255)
        val second = this.random.nextInt(255)
        val third = this.random.nextInt(255)

        return IpAddress("${first}.${second}.${third}")

    }


    fun anyDomainId(): DomainId {
        return DomainId.newId()
    }


    fun anyOrgName(): String {
        return anyOf(orgNames)
    }

    fun anyPeriod(): Period {

        return Period.of(anyInt(), anyRandomMonth(), anyRandomDayOfMonth())

    }


    fun anyRandomMonth(): Int {

        return this.random.nextInt(12)

    }


    fun anyRandomDayOfMonth(): Int {

        return this.random.nextInt(28)

    }


    fun anyPeriodOrNull(): Period? {

        return if (anyBoolean()) {
            anyPeriod()
        } else {
            null
        }

    }


    fun randomiseCase(input: String): String {

        val chars = input.toCharArray()

        for (i in chars.indices) {
            chars[i] = if (anyBoolean()) chars[i].lowercaseChar() else chars[i].uppercaseChar()
        }

        return String(chars)

    }


}

package org.maiaframework.props

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.ChangeType
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.maiaframework.props.repo.PropsRepo
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import java.time.Instant
import java.time.LocalDate

class PropsManagerTest {


    private val propsRepo = InMemoryPropsRepo()

    private val environment = StandardEnvironment().apply {
        propertySources.addFirst(MapPropertySource("test", mapOf(
            "test.prop" to "abc",
            "test.secret" to "xyz",
        )))
    }

    private val propsManager = PropsManager(propsRepo, environment)


    @Test
    fun `override matching environment value is redundant`() {

        propsRepo.setPropertyOverride("test.prop", "abc", "user", null, null)

        assertThat(propertyNamed("test.prop").isRedundant).isTrue()

    }


    @Test
    fun `override differing from environment value is not redundant`() {

        propsRepo.setPropertyOverride("test.prop", "xyz", "user", null, null)

        assertThat(propertyNamed("test.prop").isRedundant).isFalse()

    }


    @Test
    fun `override with no environment value is not redundant`() {

        propsRepo.setPropertyOverride("test.override-only", "abc", "user", null, null)

        assertThat(propertyNamed("test.override-only").isRedundant).isFalse()

    }


    @Test
    fun `property without override is not redundant`() {

        assertThat(propertyNamed("test.prop").isRedundant).isFalse()

    }


    @Test
    fun `setProperty response flags redundant override`() {

        val result = propsManager.setProperty("test.prop", "abc", "user", null, null)

        assertThat(result.isRedundant).isTrue()

    }


    @Test
    fun `setProperty response includes review date`() {

        val result = propsManager.setProperty("test.prop", "xyz", "user", null, LocalDate.of(2026, 12, 31))

        assertThat(result.reviewDate).isEqualTo(LocalDate.of(2026, 12, 31))

    }


    @Test
    fun `editing an override can clear its review date`() {

        propsManager.setProperty("test.prop", "xyz", "user", null, LocalDate.of(2026, 12, 31))

        val result = propsManager.setProperty("test.prop", "xyz", "user", null, null)

        assertThat(result.reviewDate).isNull()

    }


    @Test
    fun `property without override has no review date`() {

        assertThat(propertyNamed("test.prop").reviewDate).isNull()

    }


    @Test
    fun `non-sensitive property is not flagged as sensitive`() {

        assertThat(propertyNamed("test.prop").isSensitive).isFalse()

    }


    @Test
    fun `sensitive property is flagged as sensitive`() {

        assertThat(propertyNamed("test.secret").isSensitive).isTrue()

    }


    @Test
    fun `sensitive property environment value is masked when no override exists`() {

        val property = propertyNamed("test.secret")

        assertThat(property.effectiveValue).isEqualTo("******")
        assertThat(property.environmentValue).isEqualTo("******")

    }


    @Test
    fun `sensitive property override value is masked`() {

        propsRepo.setPropertyOverride("test.secret", "hunter2", "user", null, null)

        val property = propertyNamed("test.secret")

        assertThat(property.effectiveValue).isEqualTo("******")
        assertThat(property.environmentValue).isEqualTo("******")

    }


    @Test
    fun `redundancy detection is unaffected by masking`() {

        propsRepo.setPropertyOverride("test.secret", "xyz", "user", null, null)

        assertThat(propertyNamed("test.secret").isRedundant).isTrue()

    }


    @Test
    fun `non-sensitive property value is not masked`() {

        assertThat(propertyNamed("test.prop").effectiveValue).isEqualTo("abc")

    }


    @Test
    fun `sensitive property history values are masked`() {

        // InMemoryPropsRepo.getPropertyHistory() is a stub that always returns emptyList(), so history
        // masking is exercised here via a fake repo that returns real PropsHistoryEntity rows.
        val manager = PropsManager(
            fakeHistoryRepo(historyEntry(propertyName = "test.secret", propertyValue = "hunter2")),
            environment
        )

        val history = manager.getPropertyHistory("test.secret")

        assertThat(history.map { it.propertyValue }).containsOnly("******")

    }


    @Test
    fun `non-sensitive property history values are not masked`() {

        val manager = PropsManager(
            fakeHistoryRepo(historyEntry(propertyName = "test.prop", propertyValue = "xyz")),
            environment
        )

        val history = manager.getPropertyHistory("test.prop")

        assertThat(history.map { it.propertyValue }).containsOnly("xyz")

    }


    private fun propertyNamed(propertyName: String): PropertyResponseDto {

        return propsManager.getAllProperties().single { it.propertyName == propertyName }

    }


    private fun historyEntry(propertyName: String, propertyValue: String): PropsHistoryEntity {

        val now = Instant.now()

        return PropsHistoryEntity(
            changeType = ChangeType.UPDATE,
            comment = null,
            createdTimestamp = now,
            lastModifiedByUsername = "user",
            lastModifiedTimestamp = now,
            propertyName = propertyName,
            propertyValue = propertyValue,
            reviewDate = null,
            version = 1L,
        )

    }


    private fun fakeHistoryRepo(vararg entries: PropsHistoryEntity): PropsRepo {

        return object : PropsRepo by propsRepo {
            override fun getPropertyHistory(propertyName: String): List<PropsHistoryEntity> = entries.toList()
        }

    }


}

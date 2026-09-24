package org.maiaframework.props

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import java.time.LocalDate

class PropsManagerTest {


    private val propsRepo = InMemoryPropsRepo()

    private val environment = StandardEnvironment().apply {
        propertySources.addFirst(MapPropertySource("test", mapOf("test.prop" to "abc")))
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


    private fun propertyNamed(propertyName: String): PropertyResponseDto {

        return propsManager.getAllProperties().single { it.propertyName == propertyName }

    }


}

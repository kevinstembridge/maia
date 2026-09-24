package org.maiaframework.props

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

class PropsManagerTest {


    private val propsRepo = InMemoryPropsRepo()

    private val environment = StandardEnvironment().apply {
        propertySources.addFirst(MapPropertySource("test", mapOf("test.prop" to "abc")))
    }

    private val propsManager = PropsManager(propsRepo, environment)


    @Test
    fun `override matching environment value is redundant`() {

        propsRepo.setPropertyOverride("test.prop", "abc", "user", null)

        assertThat(propertyNamed("test.prop").isRedundant).isTrue()

    }


    @Test
    fun `override differing from environment value is not redundant`() {

        propsRepo.setPropertyOverride("test.prop", "xyz", "user", null)

        assertThat(propertyNamed("test.prop").isRedundant).isFalse()

    }


    @Test
    fun `override with no environment value is not redundant`() {

        propsRepo.setPropertyOverride("test.override-only", "abc", "user", null)

        assertThat(propertyNamed("test.override-only").isRedundant).isFalse()

    }


    @Test
    fun `property without override is not redundant`() {

        assertThat(propertyNamed("test.prop").isRedundant).isFalse()

    }


    @Test
    fun `setProperty response flags redundant override`() {

        val result = propsManager.setProperty("test.prop", "abc", "user", null)

        assertThat(result.isRedundant).isTrue()

    }


    private fun propertyNamed(propertyName: String): PropertyResponseDto {

        return propsManager.getAllProperties().single { it.propertyName == propertyName }

    }


}

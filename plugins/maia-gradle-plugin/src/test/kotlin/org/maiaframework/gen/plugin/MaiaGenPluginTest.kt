package org.maiaframework.gen.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Disabled
class MaiaGenPluginTest {

    @field:TempDir
    lateinit var projectDir: File

    private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }
    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }

    @Test
    fun `can run greeting task`() {
        // Arrange: write a tiny build that applies the plugin
        settingsFile.writeText("") // single-project build
        buildFile.writeText(
            """
            plugins {
                id("org.maiaframework.maia-gen")
            }
            
            maia {
                specificationClassNames.set(listOf("org.maiaframework.gen.plugin.SampleSpec"))
                dependencies {
                    getImplementation().add("org.maiaframework:maia-gen-testing-jdbc")
                }
            }
            """.trimIndent()
        )

        // Act: execute the task in an isolated Gradle build
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()      // picks up your plugin-under-test from the test classpath
            .withArguments("maiaGeneration", "-i", "--stacktrace")
            .forwardOutput()
            .build()

        // Assert: verify console output and successful task outcome
        assertThat(result.task(":generateMaiaModel")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    }


}

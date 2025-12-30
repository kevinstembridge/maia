package org.maiaframework.gen.plugin

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

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
            import org.maiaframework.gen.plugin.ModuleType
                
            plugins {
                id("org.maiaframework.maia-gen")
            }
            
            maia {
                moduleType.set(ModuleType.DOMAIN)
                specificationClassNames.set(listOf("org.maiaframework.gen.plugin.SampleSpec"))
                dependencies {
                    implementation(project(":libs:maia-gen:maia-gen-spec"))
                }
            }
            """.trimIndent()
        )

        // Act: execute the task in an isolated Gradle build
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()      // picks up your plugin-under-test from the test classpath
            .withArguments("generateMaiaModel", "-i", "--stacktrace")
            .forwardOutput()
            .build()

        // Assert: verify console output and successful task outcome
        assertThat(result.task(":generateMaiaModel")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    }


}

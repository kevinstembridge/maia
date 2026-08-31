package org.maiaframework.gen.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class MaiaSchemaCheckTaskFunctionalTest {

    @Test
    fun `plugin registers maiaSchemaCheck task wired to the maia extension`() {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.maiaframework.maia-generation")

        val extension = project.extensions.getByType(MaiaGenerationExtension::class.java)
        extension.schemaCheckApplicationSpecClassName.set("com.example.ExampleApplicationSpec")
        extension.schemaCheckJdbcUrl.set("jdbc:postgresql://localhost:5432/example")
        extension.schemaCheckUsername.set("example_user")
        extension.schemaCheckPassword.set("example_password")

        // Extension properties are wired at task-registration time (not lazily re-read), so
        // force configuration to run now rather than waiting for Gradle's normal task-graph
        // realization (there is none, in a ProjectBuilder test with no build executed).
        project.tasks.getByName("maiaSchemaCheck")

        val task = project.tasks.getByName("maiaSchemaCheck") as MaiaSchemaCheckTask

        assertThat(task.applicationSpecClassName.get()).isEqualTo("com.example.ExampleApplicationSpec")
        assertThat(task.jdbcUrl.get()).isEqualTo("jdbc:postgresql://localhost:5432/example")
        assertThat(task.username.get()).isEqualTo("example_user")
        assertThat(task.outputFormat.get()).isEqualTo("text") // convention default
        assertThat(task.ignoreErrors.get()).isFalse() // convention default

        val configuration = project.configurations.getByName("maiaSchemaCheckImplementation")
        assertThat(configuration.dependencies.map { "${it.group}:${it.name}" }).contains("org.maiaframework:maia-gen-schema-check")

    }

}

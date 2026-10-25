package org.maiaframework.gen.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class MaiaDomainDocsTaskFunctionalTest {

    @Test
    fun `plugin registers maiaDomainDocs task wired to the maia extension`() {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.maiaframework.maia-generation")

        val extension = project.extensions.getByType(MaiaGenerationExtension::class.java)
        extension.domainDocsSpecClassNames.set(listOf("com.example.ExampleApplicationSpec"))

        val task = project.tasks.getByName("maiaDomainDocs") as MaiaDomainDocsTask

        assertThat(task.specClassNames.get()).containsExactly("com.example.ExampleApplicationSpec")
        assertThat(task.outputDir.get().asFile).isEqualTo(project.layout.buildDirectory.dir("maia-docs").get().asFile)

        val configuration = project.configurations.getByName("maiaDomainDocsImplementation")
        assertThat(configuration.dependencies.map { "${it.group}:${it.name}" }).contains("org.maiaframework:maia-gen-docs")

    }

}

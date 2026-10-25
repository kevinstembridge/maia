package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.exists


class DomainDocsMainTest {


    @TempDir
    lateinit var outputDir: Path


    @Test
    fun `writes a site for Specs named by class`() {

        runDomainDocs(listOf(DomainDocsTest.WidgetApplicationSpec::class.java.name), outputDir)

        assertThat(outputDir.resolve("entities/Widget.html").exists()).isTrue()

    }


    @Test
    fun `fails clearly when a Spec class cannot be loaded`() {

        assertThatThrownBy { runDomainDocs(listOf("com.example.DoesNotExist"), outputDir) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("com.example.DoesNotExist")

    }


}

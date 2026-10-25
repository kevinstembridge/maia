package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText


class DomainDocsTest {


    class WidgetModelSpec : AbstractSpec(AppKey("DomainDocsTest")) {

        val widget = entity("com.example", "Widget") {
            description("A thing <that> we sell")
            field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            field("nickname", FieldTypes.string) { nullable() }
        }

    }


    class WidgetApplicationSpec : ApplicationSpec("com.example") {

        override val modelDefs: List<ModelDef> = listOf(WidgetModelSpec().modelDef)

    }


    @TempDir
    lateinit var outputDir: Path


    @Test
    fun `writes index, stylesheet and one page per Entity`() {

        generateDomainDocs(listOf(WidgetApplicationSpec()), outputDir)

        assertThat(outputDir.resolve("style.css").exists()).isTrue()
        assertThat(outputDir.resolve("index.html").readText())
            .contains("""<link rel="stylesheet" href="style.css">""")
            .contains("""<a href="entities/Widget.html">Widget</a>""")
            .contains("WidgetApplicationSpec")
            .doesNotContain("<script")

    }


    @Test
    fun `index shows a summary of entity, field, table and column counts`() {

        generateDomainDocs(listOf(WidgetApplicationSpec()), outputDir)

        val index = outputDir.resolve("index.html").readText()

        assertThat(index)
            .contains("<dt>Entities</dt><dd>1</dd>")
            .contains("<dt>Database tables</dt><dd>1</dd>")
            .containsPattern("<dt>Entity fields</dt><dd>\\d+</dd>")
            .containsPattern("<dt>Database columns</dt><dd>\\d+</dd>")

    }


    @Test
    fun `Entity page shows description, spec, flags and fields with nullability`() {

        generateDomainDocs(listOf(WidgetApplicationSpec()), outputDir)

        val page = outputDir.resolve("entities/Widget.html").readText()
        assertThat(page)
            .contains("""<link rel="stylesheet" href="../style.css">""")
            .contains("""<a href="../index.html">Index</a>""")
            .contains("A thing &lt;that&gt; we sell")
            .contains("WidgetApplicationSpec")
            .contains("<td>name</td>")
            .contains("<td>nickname</td>")
            .doesNotContain("<script")
        assertThat(page).containsPattern("<td>nickname</td>\\s*<td>[^<]*</td>\\s*<td>yes</td>")
        assertThat(page).containsPattern("<td>name</td>\\s*<td>[^<]*</td>\\s*<td>no</td>")
        assertThat(page).contains("Abstract").contains("Versioned")

    }


    @Test
    fun `creates the output directory when it does not exist`() {

        val target = outputDir.resolve("a/b")

        generateDomainDocs(listOf(WidgetApplicationSpec()), target)

        assertThat(target.resolve("index.html").exists()).isTrue()

    }


}

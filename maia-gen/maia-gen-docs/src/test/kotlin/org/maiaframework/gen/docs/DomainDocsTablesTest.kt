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
import kotlin.io.path.readText


class DomainDocsTablesTest {


    class TablesModelSpec : AbstractSpec(AppKey("DomainDocsTablesTest")) {

        val widget = entity("com.example", "Widget") {
            field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            field("nickname", FieldTypes.string) { nullable() }
            index {
                indexName("widget_name_idx")
                withFieldAscending("name")
                unique()
            }
        }

        val gadget = entity("com.example", "Gadget") {
            foreignKey("widget", widget)
        }

    }


    class TablesApplicationSpec : ApplicationSpec("com.example") {

        override val modelDefs: List<ModelDef> = listOf(TablesModelSpec().modelDef)

    }


    @TempDir
    lateinit var outputDir: Path


    @Test
    fun `table page shows columns, indexes and foreign keys and links to its Entity`() {

        generateDomainDocs(listOf(TablesApplicationSpec()), outputDir)

        val tablePage = outputDir.resolve("tables/${tablePageFileFor("Widget")}").readText()
        assertThat(tablePage)
            .contains("""<a href="../entities/Widget.html">Widget</a>""")
            .contains("""<a href="../tables/index.html">Tables</a>""")
            .contains("<td>nickname</td>")
            .contains("widget_name_idx")
            .doesNotContain("<script")
        assertThat(tablePage).containsPattern("<td>nickname</td>\\s*<td>[^<]*</td>\\s*<td>yes</td>\\s*<td>no</td>")

        val gadgetPage = outputDir.resolve("tables/${tablePageFileFor("Gadget")}").readText()
        assertThat(gadgetPage).contains("""<a href="${tablePageFileFor("Widget")}">""")

    }


    @Test
    fun `Entity page links to its table and the index links to the Tables section`() {

        generateDomainDocs(listOf(TablesApplicationSpec()), outputDir)

        assertThat(outputDir.resolve("entities/Widget.html").readText())
            .contains("""<a href="../tables/${tablePageFileFor("Widget")}">""")
            .contains("""<a href="../tables/index.html">Tables</a>""")
        assertThat(outputDir.resolve("index.html").readText()).contains("""<a href="tables/index.html">Tables</a>""")
        assertThat(outputDir.resolve("tables/index.html").readText())
            .contains("""<a href="${tablePageFileFor("Widget")}">""")
            .contains("""<a href="${tablePageFileFor("Gadget")}">""")

    }


    private fun tablePageFileFor(entityName: String): String {

        val page = outputDir.resolve("entities/$entityName.html").readText()
        return Regex("""href="\.\./tables/(?!index)([^"]+)"""").find(page)!!.groupValues[1]

    }


}

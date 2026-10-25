package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.nio.file.Path
import kotlin.io.path.readText


class DomainDocsCrudCachingTest {


    class CrudCachingModelSpec : AbstractSpec(AppKey("DomainDocsCrudCachingTest")) {

        private val editAuthority = authority("WIDGET_EDIT")

        private val deleteAuthority = authority("WIDGET_DELETE")

        val gadget = entity("com.example", "Gadget", deletable = Deletable.TRUE) {
            field("name", FieldTypes.string) { fieldDisplayName("Name"); editableByUser() }
            crud {
                create {
                    authority(editAuthority)
                    api {}
                }
                update {
                    authority(editAuthority)
                    api {}
                }
                delete {
                    authority(deleteAuthority)
                    api {}
                }
            }
            cacheable { cacheName = "gadget_cache" }
        }

        val plain = entity("com.example", "Plain") {
            field("name", FieldTypes.string)
        }

        val listenerOnly = entity("com.example", "ListenerOnly") {
            field("name", FieldTypes.string)
            crud {}
        }

    }


    class CrudCachingApplicationSpec : ApplicationSpec("com.example") {

        override val modelDefs: List<ModelDef> = listOf(CrudCachingModelSpec().modelDef)

    }


    @TempDir
    lateinit var outputDir: Path


    private fun page(name: String): String {

        generateDomainDocs(listOf(CrudCachingApplicationSpec()), outputDir)
        return outputDir.resolve("entities/$name.html").readText()

    }


    private fun crudPanel(name: String) = page(name).substringAfter("<h2>CRUD</h2>").substringBefore("<h2>Caching</h2>")


    private fun indexItem(name: String): String {

        generateDomainDocs(listOf(CrudCachingApplicationSpec()), outputDir)
        return outputDir.resolve("index.html").readText().lines().single { it.contains("entities/$name.html") }

    }


    @Test
    fun `CRUD panel shows enabled operations with authorities, listener and API exposure`() {

        val panel = crudPanel("Gadget")

        assertThat(panel).containsPattern("<td>Create</td>\\s*<td>enabled</td>\\s*<td>WIDGET_EDIT</td>")
        assertThat(panel).containsPattern("<td>Update</td>\\s*<td>enabled</td>\\s*<td>WIDGET_EDIT</td>")
        assertThat(panel).containsPattern("<td>Delete</td>\\s*<td>enabled</td>\\s*<td>WIDGET_DELETE</td>")
        assertThat(panel).containsPattern("CRUD listener</dt>\\s*<dd>yes")
        assertThat(panel).containsPattern("REST API</dt>\\s*<dd>yes")

    }


    @Test
    fun `CRUD panel says none when CRUD is disabled`() {

        assertThat(crudPanel("Plain")).contains("none").doesNotContain("<table")

    }


    @Test
    fun `CRUD panel shows listener without operations`() {

        val panel = crudPanel("ListenerOnly")

        assertThat(panel).containsPattern("CRUD listener</dt>\\s*<dd>yes")
        assertThat(panel).containsPattern("REST API</dt>\\s*<dd>no")
        assertThat(panel).containsPattern("<td>Create</td>\\s*<td>disabled</td>")

    }


    @Test
    fun `Caching panel shows the cache name or Not cached`() {

        assertThat(page("Gadget").substringAfter("<h2>Caching</h2>")).contains("Cached").contains("gadget_cache")
        assertThat(page("Plain").substringAfter("<h2>Caching</h2>")).contains("Not cached")

    }


    @Test
    fun `index badges reflect CRUD and caching`() {

        assertThat(indexItem("Gadget")).contains("badge-crud").contains("badge-cached")
        assertThat(indexItem("Plain")).doesNotContain("badge-crud").doesNotContain("badge-cached")

    }


}

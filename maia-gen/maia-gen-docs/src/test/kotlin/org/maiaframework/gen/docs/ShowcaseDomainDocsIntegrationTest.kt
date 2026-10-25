package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.elasticsearch.spec.MaiaElasticsearchApplicationSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.job.spec.MaiaJobApplicationSpec
import org.maiaframework.props.spec.PropsApplicationSpec
import org.maiaframework.showcase.MaiaShowcaseApplicationSpec
import org.maiaframework.storage.spec.StorageApplicationSpec
import org.maiaframework.toggles.spec.TogglesApplicationSpec
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.walk


class ShowcaseDomainDocsIntegrationTest {


    private val specs: List<ApplicationSpec> = listOf(
        MaiaShowcaseApplicationSpec(),
        MaiaJobApplicationSpec(),
        TogglesApplicationSpec(),
        PropsApplicationSpec(),
        StorageApplicationSpec(),
        MaiaElasticsearchApplicationSpec()
    )


    private val hrefRegex = Regex("""href="([^"]*)"""")


    private val idRegex = Regex("""\bid="([^"]*)"""")


    @TempDir
    lateinit var outputDir: Path


    private fun htmlPages(root: Path): List<Path> =
        root.walk().filter { it.extension == "html" }.toList()


    private fun relativeLinks(page: Path): List<String> =
        hrefRegex.findAll(page.readText()).map { it.groupValues[1] }
            .filter { !it.contains("://") && !it.startsWith("mailto:") }
            .toList()


    @Test
    fun `no relative link in the generated site is broken`() {

        generateDomainDocs(specs, outputDir)
        val broken = mutableListOf<String>()

        htmlPages(outputDir).forEach { page ->
            relativeLinks(page).forEach { link ->
                val file = link.substringBefore('#')
                val anchor = link.substringAfter('#', "")
                val target = if (file.isEmpty()) page else page.parent.resolve(file).normalize()
                if (!target.isRegularFile()) {
                    broken.add("${outputDir.relativize(page)} -> $link (missing file)")
                } else if (anchor.isNotEmpty() && target.extension == "html" &&
                    idRegex.findAll(target.readText()).none { it.groupValues[1] == anchor }) {
                    broken.add("${outputDir.relativize(page)} -> $link (missing anchor)")
                }
            }
        }

        assertThat(broken).isEmpty()

    }


    @Test
    fun `every Entity has a page that links to a table page which links back to the Entity`() {

        generateDomainDocs(specs, outputDir)
        val entityPages = outputDir.resolve("entities").walk().filter { it.extension == "html" }.toList()

        val expectedEntityNames = specs
            .flatMap { s -> s.applicationModelDef.entityHierarchies.map { it.entityDef.entityBaseName.toString() } }
            .toSet()
        assertThat(entityPages.map { it.name.removeSuffix(".html") }).containsExactlyInAnyOrderElementsOf(expectedEntityNames)

        val problems = mutableListOf<String>()

        entityPages.forEach { entityPage ->
            val tableLinks = relativeLinks(entityPage).filter { it.startsWith("../tables/") && !it.endsWith("index.html") }
            if (tableLinks.isEmpty()) {
                problems.add("${entityPage.name} has no table link")
            }
            tableLinks.forEach { link ->
                val tablePage = entityPage.parent.resolve(link.substringBefore('#')).normalize()
                if (!tablePage.isRegularFile() || "../entities/${entityPage.name}" !in relativeLinks(tablePage)) {
                    problems.add("${entityPage.name} -> $link does not link back")
                }
            }
        }

        assertThat(problems).isEmpty()

    }


}

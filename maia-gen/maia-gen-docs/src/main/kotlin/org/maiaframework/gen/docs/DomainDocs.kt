package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.EntityDef
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText


/**
 * Writes a static HTML site (no JavaScript, relative links only) describing the domain model of
 * the given Specs into [outputDir], creating it if necessary.
 */
fun generateDomainDocs(specs: List<ApplicationSpec>, outputDir: Path) {

    `fail on name clashes`(specs)

    val entities = specs.flatMap { spec ->
        spec.applicationModelDef.entityHierarchies.map { SpecEntity(spec.javaClass.simpleName, it.entityDef) }
    }.sortedBy { it.entityDef.entityBaseName.value }

    val entitiesDir = outputDir.resolve("entities").createDirectories()

    outputDir.resolve("style.css").writeText(STYLESHEET)
    outputDir.resolve("index.html").writeText(`render index`(entities))

    entities.forEach {
        entitiesDir.resolve("${it.entityDef.entityBaseName}.html").writeText(`render entity page`(it))
    }

}


private data class SpecEntity(val specName: String, val entityDef: EntityDef)


private fun `render index`(entities: List<SpecEntity>): String {

    val sections = entities.groupBy { it.specName }.entries.joinToString("\n") { (specName, group) ->
        val items = group.joinToString("\n") {
            val name = it.entityDef.entityBaseName.toString()
            "<li><a href=\"entities/${escape(name)}.html\">${escape(name)}</a></li>"
        }
        "<h2>${escape(specName)}</h2>\n<ul>\n$items\n</ul>"
    }

    return page("Domain docs", "", "<h1>Domain docs</h1>\n$sections")

}


private fun `render entity page`(specEntity: SpecEntity): String {

    val entityDef = specEntity.entityDef
    val name = escape(entityDef.entityBaseName.toString())
    val description = entityDef.description?.value?.let { "<p>${escape(it)}</p>" } ?: ""

    val inherited = entityDef.superclassEntityDef?.allEntityFields?.map { it.classFieldName }?.toSet() ?: emptySet()

    val fieldRows = entityDef.allEntityFields.joinToString("\n") {
        val fieldName = it.classFieldName
        val inheritedCell = if (fieldName in inherited) "yes" else "no"
        "<tr><td>${escape(fieldName.toString())}</td><td>${escape(it.fieldType.unqualifiedToString)}</td>" +
            "<td>${if (it.nullable) "yes" else "no"}</td><td>$inheritedCell</td></tr>"
    }

    val body = """
        <h1>$name</h1>
        $description
        <dl>
        <dt>Spec</dt><dd>${escape(specEntity.specName)}</dd>
        <dt>Abstract</dt><dd>${yesNo(!entityDef.isConcrete)}</dd>
        <dt>Versioned</dt><dd>${yesNo(entityDef.versioned.value)}</dd>
        <dt>Version history</dt><dd>${yesNo(entityDef.withVersionHistory.value)}</dd>
        </dl>
        <h2>Fields</h2>
        <table>
        <tr><th>Name</th><th>Type</th><th>Nullable</th><th>Inherited</th></tr>
        $fieldRows
        </table>
    """.trimIndent()

    return page(name, "../", body)

}


private fun page(title: String, root: String, body: String): String = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="utf-8">
    <title>${escape(title)}</title>
    <link rel="stylesheet" href="${root}style.css">
    </head>
    <body>
    <header><nav><a href="${root}index.html">Index</a></nav></header>
    <main>
    $body
    </main>
    </body>
    </html>
""".trimIndent() + "\n"


private fun yesNo(value: Boolean) = if (value) "yes" else "no"


private fun escape(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")


private val STYLESHEET = """
    body { font-family: sans-serif; margin: 0; color: #222; }
    header { background: #263238; padding: 0.75rem 1.5rem; }
    header a { color: #fff; text-decoration: none; margin-right: 1rem; }
    main { padding: 1rem 1.5rem; max-width: 70rem; }
    table { border-collapse: collapse; }
    th, td { border: 1px solid #ccc; padding: 0.25rem 0.75rem; text-align: left; }
    th { background: #eceff1; }
    dt { font-weight: bold; float: left; clear: left; width: 10rem; }
    dd { margin-left: 10rem; }
""".trimIndent() + "\n"

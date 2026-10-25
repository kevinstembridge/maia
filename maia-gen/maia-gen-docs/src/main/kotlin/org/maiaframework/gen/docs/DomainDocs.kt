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

    val specNames = specDisplayNames(specs)

    val entities = specs.flatMap { spec ->
        spec.applicationModelDef.entityHierarchies.map { SpecEntity(specKey(spec), it.entityDef) }
    }.sortedBy { it.entityDef.entityBaseName.value }

    val entitiesDir = outputDir.resolve(ENTITIES_DIR).createDirectories()

    outputDir.resolve(STYLESHEET_FILE).writeText(STYLESHEET)
    `write enums and value types pages`(specs, outputDir)
    outputDir.resolve(INDEX_PAGE).writeText(`render index`(entities, specNames))

    writeTablePages(specs, outputDir)

    entities.forEach {
        entitiesDir.resolve(entityPageFileName(it.entityDef))
            .writeText(`render entity page`(it, specNames.getValue(it.specKey), entities.map { e -> e.entityDef }))
    }

}


private data class SpecEntity(val specKey: String, val entityDef: EntityDef)


private fun `render index`(entities: List<SpecEntity>, specNames: Map<String, String>): String {

    val sections = entities.groupBy { it.specKey }.entries.joinToString("\n") { (specKey, group) ->
        val items = group.joinToString("\n") {
            "<li>${entityLink(it.entityDef, "$ENTITIES_DIR/")}${`render index badges`(it.entityDef)}</li>"
        }
        "<h2>${escapeHtml(specNames.getValue(specKey))}</h2>\n<ul>\n$items\n</ul>"
    }

    return renderPage("Domain docs", "", "<h1>Domain docs</h1>\n$sections")

}


private fun `render entity page`(specEntity: SpecEntity, specName: String, allEntities: List<EntityDef>): String {

    val entityDef = specEntity.entityDef
    val name = entityDef.entityBaseName.toString()
    val description = entityDef.description?.value?.let { "<p>${escapeHtml(it)}</p>" } ?: ""

    val fieldRows = entityDef.allEntityFields.joinToString("\n") {
        val fieldName = it.classFieldName
        val inheritedCell = `render inherited cell`(entityDef, fieldName.toString())
        "<tr><td>${escapeHtml(fieldName.toString())}</td><td>${`render field type cell`(it.fieldType, PARENT_ROOT)}</td>" +
            "<td>${yesNo(it.nullable)}</td><td>$inheritedCell</td></tr>"
    }

    val body = """
        <h1>${escapeHtml(name)}</h1>
        $description
        <dl>
        <dt>Spec</dt><dd>${escapeHtml(specName)}</dd>
        <dt>Table</dt><dd>${tableLinkFor(entityDef, PARENT_ROOT)}</dd>
        <dt>Abstract</dt><dd>${yesNo(!entityDef.isConcrete)}</dd>
        <dt>Versioned</dt><dd>${yesNo(entityDef.versioned.value)}</dd>
        <dt>Version history</dt><dd>${yesNo(entityDef.withVersionHistory.value)}</dd>
        ${`render structure rows`(entityDef, allEntities)}
        </dl>
        <h2>Fields</h2>
        <table>
        <tr><th>Name</th><th>Type</th><th>Nullable</th><th>Inherited</th></tr>
        $fieldRows
        </table>
        ${`render relationship panels`(entityDef, allEntities)}
        ${`render crud panel`(entityDef)}
        ${`render caching panel`(entityDef)}
    """.trimIndent()

    return renderPage(name, PARENT_ROOT, body)

}

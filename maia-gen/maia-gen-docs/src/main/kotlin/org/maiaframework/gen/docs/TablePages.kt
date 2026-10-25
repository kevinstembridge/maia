package org.maiaframework.gen.docs

import org.maiaframework.gen.schema.expected.ExpectedSchemaExtractor
import org.maiaframework.gen.schema.expected.ExpectedTableDef
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.EntityDef
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText


private data class TableEntry(val table: ExpectedTableDef, val entityDefs: List<EntityDef>)


/**
 * Writes the Tables section: `tables/index.html` plus one page per database table, derived from the Entity
 * definitions. Entities in an inheritance hierarchy share the table of their root Entity.
 */
internal fun writeTablePages(specs: List<ApplicationSpec>, outputDir: Path) {

    val entries = specs.flatMap { spec ->
        val hierarchies = spec.applicationModelDef.rootEntityHierarchies
        val tables = ExpectedSchemaExtractor().extract(hierarchies)
        tables.zip(hierarchies).map { (table, hierarchy) -> TableEntry(table, hierarchy.entityDefs) }
    }.sortedBy { it.table.schemaAndTableName }

    val tablesDir = outputDir.resolve("tables").createDirectories()

    tablesDir.resolve("index.html").writeText(`render tables index`(entries))

    entries.forEach {
        tablesDir.resolve(tablePageFileName(it.table.schemaAndTableName)).writeText(`render table page`(it, entries))
    }

}


/** The link from a page at [root] to the table page backing [entityDef]. */
internal fun tableLinkFor(entityDef: EntityDef, root: String): String {

    var rootEntityDef = entityDef

    while (true) {
        rootEntityDef = rootEntityDef.superclassEntityDef ?: break
    }

    val key = "${rootEntityDef.schemaName}.${rootEntityDef.tableName.rawTableName}"

    return "<a href=\"${root}tables/${tablePageFileName(key)}\">${escape(key)}</a>"

}


private fun tablePageFileName(schemaAndTableName: String) =
    schemaAndTableName.replace(Regex("[^A-Za-z0-9._-]"), "_") + ".html"


private fun `render tables index`(entries: List<TableEntry>): String {

    val items = entries.joinToString("\n") {
        val name = it.table.schemaAndTableName
        "<li><a href=\"${tablePageFileName(name)}\">${escape(name)}</a></li>"
    }

    return page("Tables", "../", "<h1>Tables</h1>\n<ul>\n$items\n</ul>")

}


private fun `render table page`(entry: TableEntry, allEntries: List<TableEntry>): String {

    val table = entry.table
    val name = escape(table.schemaAndTableName)

    val entityLinks = entry.entityDefs.joinToString(", ") {
        val entityName = escape(it.entityBaseName.toString())
        "<a href=\"../entities/$entityName.html\">$entityName</a>"
    }

    val columnRows = table.columns.joinToString("\n") {
        "<tr><td>${escape(it.name.value)}</td><td>${escape(it.postgresType)}</td>" +
            "<td>${yesNo(it.nullable)}</td><td>${yesNo(it.isPrimaryKey)}</td></tr>"
    }

    val indexRows = (table.indexes + table.exclusionIndexes).joinToString("\n") {
        "<tr><td>${escape(it.name)}</td><td>${escape(it.columns.joinToString(", ") { c -> c.value })}</td>" +
            "<td>${yesNo(it.unique)}</td></tr>"
    }

    val knownTables = allEntries.map { it.table.schemaAndTableName }.toSet()

    fun tableRef(referenced: String) =
        if (referenced in knownTables) {
            "<a href=\"${tablePageFileName(referenced)}\">${escape(referenced)}</a>"
        } else {
            escape(referenced)
        }

    val simpleForeignKeyRows = table.foreignKeys.map {
        "<tr><td>${escape(it.columnName.value)}</td><td>${tableRef(it.referencedSchemaAndTable)}</td>" +
            "<td>${escape(it.referencedColumn.value)}</td></tr>"
    }

    val compositeForeignKeyRows = table.compositeForeignKeys.map {
        "<tr><td>${escape(it.columnNames.joinToString(", ") { c -> c.value })}</td>" +
            "<td>${tableRef(it.referencedSchemaAndTable)}</td>" +
            "<td>${escape(it.referencedColumns.joinToString(", ") { c -> c.value })}</td></tr>"
    }

    val foreignKeyRows = (simpleForeignKeyRows + compositeForeignKeyRows).joinToString("\n")

    val body = """
        <h1>$name</h1>
        <dl>
        <dt>Entities</dt><dd>$entityLinks</dd>
        </dl>
        <h2>Columns</h2>
        <table>
        <tr><th>Name</th><th>Type</th><th>Nullable</th><th>Primary key</th></tr>
        $columnRows
        </table>
        <h2>Indexes</h2>
        <table>
        <tr><th>Name</th><th>Columns</th><th>Unique</th></tr>
        $indexRows
        </table>
        <h2>Foreign keys</h2>
        <table>
        <tr><th>Columns</th><th>References table</th><th>References columns</th></tr>
        $foreignKeyRows
        </table>
    """.trimIndent()

    return page(table.schemaAndTableName, "../", body)

}

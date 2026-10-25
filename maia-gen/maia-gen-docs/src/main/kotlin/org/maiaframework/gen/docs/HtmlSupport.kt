package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.definition.EntityDef


/* Page and directory names of the generated site. Every link and the nav are built from these. */

internal const val STYLESHEET_FILE = "style.css"

internal const val INDEX_PAGE = "index.html"

internal const val ENUMS_PAGE = "enums.html"

internal const val VALUE_TYPES_PAGE = "value-types.html"

internal const val ENTITIES_DIR = "entities"

internal const val TABLES_DIR = "tables"

internal const val TABLES_INDEX_PAGE = "index.html"

/** Path from a page inside one of the site's sub-directories back to the site root. */
internal const val PARENT_ROOT = "../"


internal fun entityPageFileName(entityDef: EntityDef) = "${entityDef.entityBaseName}.html"


internal fun renderPage(title: String, root: String, body: String): String = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="utf-8">
    <title>${escapeHtml(title)}</title>
    <link rel="stylesheet" href="${escapeHtml(root + STYLESHEET_FILE)}">
    </head>
    <body>
    <header><nav>${renderNav(root)}</nav></header>
    <main>
    $body
    </main>
    </body>
    </html>
""".trimIndent() + "\n"


private fun renderNav(root: String) = listOf(
    "Index" to INDEX_PAGE,
    "Tables" to "$TABLES_DIR/$TABLES_INDEX_PAGE",
    "Enums" to ENUMS_PAGE,
    "Value types" to VALUE_TYPES_PAGE
).joinToString("") { (text, page) -> link(root + page, text) }


/** An anchor whose [href] and [text] are both HTML-escaped. */
internal fun link(href: String, text: String): String = "<a href=\"${escapeHtml(href)}\">${escapeHtml(text)}</a>"


/**
 * A link to the page of [entityDef]. [entitiesDirPath] is the relative path from the linking page to the
 * entities directory: empty from an Entity page, `entities/` from the site root, `../entities/` from the
 * tables directory.
 */
internal fun entityLink(entityDef: EntityDef, entitiesDirPath: String): String =
    link(entitiesDirPath + entityPageFileName(entityDef), entityDef.entityBaseName.toString())


internal fun yesNo(value: Boolean) = if (value) "yes" else "no"


internal fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")


internal val STYLESHEET = """
    body { font-family: sans-serif; margin: 0; color: #222; }
    header { background: #263238; padding: 0.75rem 1.5rem; }
    header a { color: #fff; text-decoration: none; margin-right: 1rem; }
    main { padding: 1rem 1.5rem; max-width: 70rem; }
    table { border-collapse: collapse; }
    th, td { border: 1px solid #ccc; padding: 0.25rem 0.75rem; text-align: left; }
    th { background: #eceff1; }
    dt { font-weight: bold; float: left; clear: left; width: 10rem; }
    dd { margin-left: 10rem; }
    .badge { font-size: 0.75rem; padding: 0 0.4rem; border-radius: 0.6rem; margin-left: 0.4rem; color: #fff; }
    .badge-crud { background: #2e7d32; }
    .badge-cached { background: #1565c0; }
""".trimIndent() + "\n"

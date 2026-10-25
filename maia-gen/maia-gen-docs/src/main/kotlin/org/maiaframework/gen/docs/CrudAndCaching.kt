package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.EntityDef


internal const val CRUD_CACHING_STYLES =
    ".badge { font-size: 0.75rem; padding: 0 0.4rem; border-radius: 0.6rem; margin-left: 0.4rem; color: #fff; }\n" +
        ".badge-crud { background: #2e7d32; }\n" +
        ".badge-cached { background: #1565c0; }\n"


private fun `is crud enabled`(entityDef: EntityDef): Boolean {

    val crudDef = entityDef.crudDef
    val ops = crudDef.crudOperationDefs
    return crudDef.withCrudListener.value
        || ops.createOperationDef != null
        || ops.updateOperationDef != null
        || ops.deleteOperationDef != null

}


/** Small CRUD and cached badges for the index entry of an Entity. */
internal fun `render index badges`(entityDef: EntityDef): String {

    val crud = if (`is crud enabled`(entityDef)) " <span class=\"badge badge-crud\">CRUD</span>" else ""
    val cached = if (entityDef.cacheableDef != null) " <span class=\"badge badge-cached\">cached</span>" else ""
    return crud + cached

}


internal fun `render crud panel`(entityDef: EntityDef): String {

    if (!`is crud enabled`(entityDef)) {
        return "<h2>CRUD</h2>\n<p>none</p>"
    }

    val ops = entityDef.crudDef.crudOperationDefs

    val rows = listOf(
        Triple("Create", ops.createOperationDef?.authorityDef, ops.createOperationDef != null),
        Triple("Update", ops.updateOperationDef?.authorityDef, ops.updateOperationDef != null),
        Triple("Delete", ops.deleteOperationDef?.authorityDef, ops.deleteOperationDef != null)
    ).joinToString("\n") { (name, authority, enabled) -> `render crud row`(name, authority, enabled) }

    val apiExposed = listOfNotNull(
        ops.createOperationDef?.crudApiDef,
        ops.updateOperationDef?.crudApiDef,
        ops.deleteOperationDef?.crudApiDef
    ).isNotEmpty()

    return """
        <h2>CRUD</h2>
        <table>
        <tr><th>Operation</th><th>Status</th><th>Required authority</th></tr>
        $rows
        </table>
        <dl>
        <dt>CRUD listener</dt><dd>${yesNoText(entityDef.crudDef.withCrudListener.value)}</dd>
        <dt>REST API</dt><dd>${yesNoText(apiExposed)}</dd>
        </dl>
    """.trimIndent()

}


private fun `render crud row`(name: String, authority: AuthorityDef?, enabled: Boolean): String {

    val status = if (enabled) "enabled" else "disabled"
    val authorityText = authority?.let { escapeHtml(it.name) } ?: ""
    return "<tr><td>$name</td><td>$status</td><td>$authorityText</td></tr>"

}


internal fun `render caching panel`(entityDef: EntityDef): String {

    val cacheName = entityDef.cacheableDef?.cacheName?.value
    val text = if (cacheName != null) "Cached: ${escapeHtml(cacheName)}" else "Not cached"
    return "<h2>Caching</h2>\n<p>$text</p>"

}


private fun yesNoText(value: Boolean) = if (value) "yes" else "no"


private fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

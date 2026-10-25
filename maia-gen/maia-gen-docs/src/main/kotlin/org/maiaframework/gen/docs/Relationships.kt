package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.definition.EntityDef


/**
 * Entity pages all live in the same directory, so a link to another Entity (in any Spec) is just
 * its file name. Entities are keyed by base name, which is unique across the merged Specs.
 */
private fun `entity link`(entityDef: EntityDef) = entityLink(entityDef, "")


private fun `same entity`(a: EntityDef, b: EntityDef) = a.entityBaseName == b.entityBaseName


/** Inheritance, Type discriminator, History entity and many-to-many details for the header list of an Entity page. */
internal fun `render structure rows`(entityDef: EntityDef, allEntities: List<EntityDef>): String {

    val rows = mutableListOf<String>()

    entityDef.superclassEntityDef?.let { rows += "<dt>Superclass</dt><dd>${`entity link`(it)}</dd>" }

    val subclasses = allEntities.filter { sub -> sub.superclassEntityDef?.let { `same entity`(it, entityDef) } == true }
    if (subclasses.isNotEmpty()) {
        rows += "<dt>Subclasses</dt><dd>${subclasses.joinToString(", ") { `entity link`(it) }}</dd>"
    }

    entityDef.typeDiscriminatorOrNull?.let { rows += "<dt>Type discriminator</dt><dd>${escapeHtml(it.value)}</dd>" }

    entityDef.historyEntityDef?.let { rows += "<dt>History entity</dt><dd>${`entity link`(it)}</dd>" }

    if (entityDef.isHistoryEntity) {
        val source = allEntities.firstOrNull { candidate ->
            candidate.historyEntityDef?.let { `same entity`(it, entityDef) } == true
        }
        if (source != null) {
            rows += "<dt>History of</dt><dd>${`entity link`(source)}</dd>"
        }
    }

    rows += "<dt>Many-to-many join</dt><dd>${yesNo(entityDef.isManyToManyJoinEntity)}</dd>"

    if (entityDef.isManyToManyJoinEntity) {
        val associations = allEntities
            .flatMap { it.manyToManyAssociations }
            .filter { `same entity`(it.entityDef, entityDef) }
            .distinctBy { it.leftEntity.entityDef.entityBaseName to it.rightEntity.entityDef.entityBaseName }
        associations.forEach { association ->
            rows += "<dt>Left side</dt><dd>${`entity link`(association.leftEntity.entityDef)}</dd>"
            rows += "<dt>Right side</dt><dd>${`entity link`(association.rightEntity.entityDef)}</dd>"
        }
    }

    return rows.joinToString("\n")

}


/** The cell showing where an inherited field is declared, linked to that Entity, or "no" if it is declared here. */
internal fun `render inherited cell`(entityDef: EntityDef, fieldName: String): String {

    var declaring: EntityDef? = entityDef.superclassEntityDef

    while (declaring != null) {
        if (declaring.entityFieldsNotInherited.any { it.classFieldName.toString() == fieldName }) {
            return "yes (${`entity link`(declaring)})"
        }
        declaring = declaring.superclassEntityDef
    }

    return "no"

}


private fun `outgoing references`(entityDef: EntityDef) = entityDef.entityFieldsNotInherited.mapNotNull { field ->
    field.foreignKeyFieldDef?.let { field to it.foreignEntityDef }
}


/** The 'References' (outgoing foreign keys) and 'Referenced by' (incoming) sections of an Entity page. */
internal fun `render relationship panels`(entityDef: EntityDef, allEntities: List<EntityDef>): String {

    val outgoing = `outgoing references`(entityDef).joinToString("\n") { (field, target) ->
        "<tr><td>${escapeHtml(field.classFieldName.toString())}</td><td>${`entity link`(target)}</td></tr>"
    }

    val incoming = allEntities.flatMap { other ->
        `outgoing references`(other)
            .filter { (_, target) -> `same entity`(target, entityDef) }
            .map { (field, _) -> other to field }
    }.joinToString("\n") { (other, field) ->
        "<tr><td>${`entity link`(other)}</td><td>${escapeHtml(field.classFieldName.toString())}</td></tr>"
    }

    return """
        <h2>References</h2>
        ${if (outgoing.isEmpty()) "<p>none</p>" else "<table>\n<tr><th>Field</th><th>Entity</th></tr>\n$outgoing\n</table>"}
        <h2>Referenced by</h2>
        ${if (incoming.isEmpty()) "<p>none</p>" else "<table>\n<tr><th>Entity</th><th>Field</th></tr>\n$incoming\n</table>"}
    """.trimIndent()

}

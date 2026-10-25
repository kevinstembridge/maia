package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.BooleanTypeDef
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.IntTypeDef
import org.maiaframework.gen.spec.definition.LongTypeDef
import org.maiaframework.gen.spec.definition.SimpleTypeDef
import org.maiaframework.gen.spec.definition.StringTypeDef
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import java.nio.file.Path
import kotlin.io.path.writeText


private const val ENUMS_PAGE = "enums.html"

private const val VALUE_TYPES_PAGE = "value-types.html"


/**
 * Returns the HTML for the type cell of a field: a link to the enums or value types page when the
 * field uses an enum or value type (directly or as a list/set element), else the escaped type name.
 * [root] is the relative path from the page to the site root, e.g. `../`.
 */
internal fun `render field type cell`(fieldType: FieldType, root: String): String {

    val target = `enum or value type of`(fieldType) ?: return escape(fieldType.unqualifiedToString)

    val page = if (target is EnumDef) ENUMS_PAGE else VALUE_TYPES_PAGE
    val link = "<a href=\"$root$page#${escape(`key of`(target))}\">${escape(`name of`(target))}</a>"

    return when (fieldType) {
        is ListFieldType -> "List&lt;$link&gt;"
        is SetFieldType -> "Set&lt;$link&gt;"
        else -> link
    }

}


internal fun `write enums and value types pages`(specs: List<ApplicationSpec>, outputDir: Path) {

    val usages = `collect usages`(specs)
    outputDir.resolve(ENUMS_PAGE).writeText(`render enums page`(specs, usages))
    outputDir.resolve(VALUE_TYPES_PAGE).writeText(`render value types page`(specs, usages))

}


private fun `enum or value type of`(fieldType: FieldType): Any? = when (fieldType) {
    is EnumFieldType -> fieldType.enumDef
    is StringTypeFieldType -> fieldType.stringTypeDef
    is IntTypeFieldType -> fieldType.intTypeDef
    is LongTypeFieldType -> fieldType.longTypeDef
    is BooleanTypeFieldType -> fieldType.booleanTypeDef
    is ListFieldType -> `enum or value type of`(fieldType.parameterFieldType)
    is SetFieldType -> `enum or value type of`(fieldType.parameterFieldType)
    else -> null
}


private fun `key of`(target: Any): String = when (target) {
    is EnumDef -> target.fqcn.toString()
    is SimpleTypeDef -> target.fqcn.toString()
    else -> throw IllegalArgumentException("Unexpected $target")
}


private fun `name of`(target: Any): String = when (target) {
    is EnumDef -> target.uqcn.toString()
    is SimpleTypeDef -> target.uqcn.toString()
    else -> throw IllegalArgumentException("Unexpected $target")
}


/** Map of type fqcn to the "Entity.field" usages, each already rendered as HTML. */
private fun `collect usages`(specs: List<ApplicationSpec>): Map<String, List<String>> {

    val usages = linkedMapOf<String, MutableList<String>>()

    specs.flatMap { spec -> spec.applicationModelDef.entityHierarchies.map { it.entityDef } }
        .distinctBy { it.entityBaseName.value }
        .sortedBy { it.entityBaseName.value }
        .forEach { entityDef ->
            entityDef.entityFieldsNotInherited.forEach { field ->
                `enum or value type of`(field.fieldType)?.let { target ->
                    val entityName = escape(entityDef.entityBaseName.toString())
                    usages.getOrPut(`key of`(target)) { mutableListOf() }
                        .add("<a href=\"entities/$entityName.html\">$entityName</a>.${escape(field.classFieldName.toString())}")
                }
            }
        }

    return usages

}


private fun `render used by`(usages: List<String>?): String {

    if (usages.isNullOrEmpty()) return "<p>Used by: none</p>"

    return "<p>Used by:</p>\n<ul>\n" + usages.joinToString("\n") { "<li>$it</li>" } + "\n</ul>"

}


private fun `render enums page`(specs: List<ApplicationSpec>, usages: Map<String, List<String>>): String {

    val sections = specs.flatMap { spec -> spec.applicationModelDef.enumDefs.map { spec.javaClass.simpleName to it } }
        .sortedBy { it.second.uqcn.toString() }
        .joinToString("\n") { (specName, enumDef) ->
            val rows = enumDef.enumValueDefs.joinToString("\n") {
                "<tr><td>${escape(it.name)}</td><td>${escape(it.displayName?.value ?: "")}</td></tr>"
            }
            """
            <section id="${escape(enumDef.fqcn.toString())}">
            <h2>${escape(enumDef.uqcn.toString())}</h2>
            <p>Spec: ${escape(specName)}</p>
            <table>
            <tr><th>Value</th><th>Display name</th></tr>
            $rows
            </table>
            ${`render used by`(usages[enumDef.fqcn.toString()])}
            </section>
            """.trimIndent()
        }

    return page("Enums", "", "<h1>Enums</h1>\n$sections")

}


private fun `render value types page`(specs: List<ApplicationSpec>, usages: Map<String, List<String>>): String {

    val sections = specs.flatMap { spec ->
        val def = spec.applicationModelDef
        val all: List<SimpleTypeDef> = def.stringTypeDefs + def.intTypeDefs + def.longTypeDefs + def.booleanTypeDefs
        all.map { spec.javaClass.simpleName to it }
    }.sortedBy { it.second.uqcn.toString() }
        .joinToString("\n") { (specName, typeDef) ->
            val properties = buildList {
                add("Underlying kind" to `kind of`(typeDef))
                if (typeDef is StringTypeDef) add("Case mode" to typeDef.caseMode.name)
                add("Provided" to if (typeDef.isNotProvided) "no" else "yes")
            }.joinToString("\n") { "<dt>${escape(it.first)}</dt><dd>${escape(it.second)}</dd>" }
            """
            <section id="${escape(typeDef.fqcn.toString())}">
            <h2>${escape(typeDef.uqcn.toString())}</h2>
            <p>Spec: ${escape(specName)}</p>
            <dl>
            $properties
            </dl>
            ${`render used by`(usages[typeDef.fqcn.toString()])}
            </section>
            """.trimIndent()
        }

    return page("Value types", "", "<h1>Value types</h1>\n$sections")

}


private fun `kind of`(typeDef: SimpleTypeDef): String = when (typeDef) {
    is StringTypeDef -> "string"
    is IntTypeDef -> "int"
    is LongTypeDef -> "long"
    is BooleanTypeDef -> "boolean"
    else -> throw IllegalArgumentException("Unexpected $typeDef")
}

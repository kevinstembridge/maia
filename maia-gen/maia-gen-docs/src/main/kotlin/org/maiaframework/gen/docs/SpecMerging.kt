package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.ApplicationSpec


/** A Spec is identified by its fully qualified class name. */
internal fun specKey(spec: ApplicationSpec): String = spec.javaClass.name


/**
 * Maps each Spec's [specKey] to the name to display for it: the simple class name, or the fully
 * qualified name when two Specs share a simple name.
 */
internal fun specDisplayNames(specs: List<ApplicationSpec>): Map<String, String> {

    val classes = specs.map { it.javaClass }.distinctBy { it.name }
    val simpleNameCounts = classes.groupingBy { it.simpleName }.eachCount()

    return classes.associate { it.name to if (simpleNameCounts.getValue(it.simpleName) > 1) it.name else it.simpleName }

}


/**
 * Fails if two Specs declare Entities with the same name, root Entities backed by the same table,
 * or enums or value types with the same name but a different definition. Identical declarations,
 * such as the framework's built-in enums, are one shared type and are listed under the first Spec that declares them.
 */
internal fun `fail on name clashes`(specs: List<ApplicationSpec>) {

    val displayNames = specDisplayNames(specs)
    val entityOwners = mutableMapOf<String, String>()
    val tableOwners = mutableMapOf<String, Pair<String, String>>()
    val enumOwners = mutableMapOf<String, Pair<String, Any>>()
    val valueTypeOwners = mutableMapOf<String, Pair<String, Any>>()

    specs.forEach { spec ->

        val specKey = specKey(spec)
        val specName = displayNames.getValue(specKey)

        val modelDef = spec.applicationModelDef

        modelDef.enumDefs.forEach { enumDef ->
            val name = enumDef.fqcn.toString()
            val signature = `enum signature`(enumDef)
            val previous = enumOwners.getOrPut(name) { specKey to signature }
            check(previous.second == signature) {
                "Enum name clash: enum '$name' is declared differently by ${displayNames.getValue(previous.first)} and $specName"
            }
        }

        val valueTypeDefs = modelDef.stringTypeDefs + modelDef.intTypeDefs + modelDef.longTypeDefs + modelDef.booleanTypeDefs

        valueTypeDefs.forEach { typeDef ->
            val name = typeDef.fqcn.toString()
            val signature = `value type signature`(typeDef)
            val previous = valueTypeOwners.getOrPut(name) { specKey to signature }
            check(previous.second == signature) {
                "Value type name clash: value type '$name' is declared differently by ${displayNames.getValue(previous.first)} and $specName"
            }
        }

        modelDef.entityHierarchies.map { it.entityDef }.forEach { entityDef ->

            val entityName = entityDef.entityBaseName.toString()
            val previousSpecKey = entityOwners.put(entityName, specKey)

            check(previousSpecKey == null || previousSpecKey == specKey) {
                "Entity name clash: Entity '$entityName' is declared by both ${displayNames.getValue(previousSpecKey!!)} and $specName"
            }

            if (entityDef.superclassEntityDef == null) {

                val table = entityDef.schemaAndTableName
                val previous = tableOwners.put(table, specKey to entityName)

                check(previous == null || previous.second == entityName) {
                    "Table name clash: table '$table' is used by Entity '${previous!!.second}' " +
                        "(${displayNames.getValue(previous.first)}) and Entity '$entityName' ($specName)"
                }

            }

        }

    }

}

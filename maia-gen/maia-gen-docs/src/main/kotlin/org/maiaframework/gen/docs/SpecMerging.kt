package org.maiaframework.gen.docs

import org.maiaframework.gen.spec.ApplicationSpec


/**
 * Instantiates the Specs with the given fully qualified class names, failing with a clear message
 * if any class cannot be loaded or instantiated.
 */
fun loadSpecs(classNames: List<String>): List<ApplicationSpec> = classNames.map { className ->

    try {
        Class.forName(className).getDeclaredConstructor().newInstance() as ApplicationSpec
    } catch (e: Exception) {
        throw IllegalArgumentException("Unable to load Spec class '$className': ${e.javaClass.simpleName}: ${e.message}", e)
    } catch (e: LinkageError) {
        throw IllegalArgumentException("Unable to load Spec class '$className': ${e.javaClass.simpleName}: ${e.message}", e)
    }

}


/**
 * Fails if two Specs declare Entities with the same name, or root Entities backed by the same table.
 */
internal fun `fail on name clashes`(specs: List<ApplicationSpec>) {

    val entityOwners = mutableMapOf<String, String>()
    val tableOwners = mutableMapOf<String, Pair<String, String>>()

    specs.forEach { spec ->

        val specName = spec.javaClass.simpleName

        spec.applicationModelDef.entityHierarchies.map { it.entityDef }.forEach { entityDef ->

            val entityName = entityDef.entityBaseName.toString()
            val previousSpec = entityOwners.put(entityName, specName)

            check(previousSpec == null || previousSpec == specName) {
                "Entity name clash: Entity '$entityName' is declared by both $previousSpec and $specName"
            }

            if (entityDef.superclassEntityDef == null) {

                val table = entityDef.schemaAndTableName
                val previous = tableOwners.put(table, specName to entityName)

                check(previous == null || previous.second == entityName) {
                    "Table name clash: table '$table' is used by Entity '${previous!!.second}' (${previous.first}) " +
                        "and Entity '$entityName' ($specName)"
                }

            }

        }

    }

}

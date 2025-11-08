package org.maiaframework.gen.spec.definition

import org.maiaframework.domain.types.TypeDiscriminator
import java.util.SortedSet

class EntityHierarchy(val entityDef: EntityDef) {


    private val children = mutableListOf<EntityHierarchy>()


    val entityDefs: List<EntityDef>
        get() = children.map { it.entityDefs }.flatten().plus(this.entityDef)


    val entityHierarchies: List<EntityHierarchy>
        get() = children.map { it.entityHierarchies }.flatten().plus(this)


    val allFieldDefs: List<EntityFieldDef>
        get() {

            val subclassEntityFieldDefs = mutableListOf<EntityFieldDef>()
            val fieldDefConsumer: (EntityFieldDef) -> Unit = { subclassEntityFieldDefs.add(it) }
            collectSubclassFields(fieldDefConsumer)

            return listOf(this.entityDef.allEntityFields, subclassEntityFieldDefs).flatten()

        }


    val allFieldDefsSorted: List<EntityFieldDef>
        get() = allFieldDefs.sorted()


    val concreteEntityDefs: List<EntityDef>
        get() = entityDefs.filter { it.isConcrete }


    fun addToHierarchyIfItBelongs(subclassEntityDef: EntityDef): Boolean {

        if (subclassEntityDef.isSubclassOf(this.entityDef)) {
            this.children.add(EntityHierarchy(subclassEntityDef))
            return true
        }

        for (child in this.children) {

            if (child.addToHierarchyIfItBelongs(subclassEntityDef)) {
                return true
            }

        }

        return false

    }


    private fun collectSubclassFields(subclassFieldConsumer: (EntityFieldDef) -> Unit) {

        this.children.forEach { child ->
            child.entityDef.entityFieldsNotInherited.forEach(subclassFieldConsumer)
            child.collectSubclassFields(subclassFieldConsumer)
        }

    }


    fun hasSubclasses(): Boolean {

        return this.children.isNotEmpty()

    }


    val requiresObjectMapper
        get() = entityDefs.any { it.hasAnyJsonFields } || entityDefs.any { it.hasAnyMapFields }


    fun typeDiscriminators(): SortedSet<TypeDiscriminator> {

        return entityDefs.asSequence().mapNotNull { it.typeDiscriminatorOrNull }.toSortedSet()

    }


    override fun toString(): String {

        return "EntityHierarchy{${entityDef.entityBaseName}}"

    }


}

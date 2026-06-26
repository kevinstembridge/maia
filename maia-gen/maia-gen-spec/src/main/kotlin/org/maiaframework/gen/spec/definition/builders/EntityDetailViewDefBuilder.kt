package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.ManyToManySearchableDtoFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability

class EntityDetailViewDefBuilder(private val entityDef: EntityDef) {


    private var pageTitle = entityDef.entityBaseName.toTitleCase()


    internal var entityEditPageDef: EntityEditPageDef? = null


    private val manyToManyFieldDefs = mutableListOf<ManyToManySearchableDtoFieldDef>()


    fun build(): EntityDetailViewDef {

        return EntityDetailViewDef(
            this.entityDef,
            this.pageTitle,
            this.manyToManyFieldDefs.toList()
        )

    }


    fun pageTitle(pageTitle: String) {

        this.pageTitle = pageTitle

    }


    fun manyToManyField(fieldName: String, manyToManyEntityDef: ManyToManyEntityDef) {

        require(!this.entityDef.hasCompositePrimaryKey) {
            "manyToManyField is not supported for entities with composite primary keys (entity: ${this.entityDef.entityBaseName})"
        }

        val otherSide = manyToManyEntityDef.otherSideFrom(this.entityDef)
        val fieldType = FieldTypes.list(FieldTypes.pkAndName(otherSide.entityDef.entityPkAndNameDef))
        val classFieldDef = aClassField(fieldName, fieldType) {
            displayName("${otherSide.displayName} Entities")
        }.build()

        this.manyToManyFieldDefs.add(
            ManyToManySearchableDtoFieldDef(
                classFieldDef,
                manyToManyEntityDef,
                sortIndexAndDirection = null,
                nullability = Nullability.NOT_NULLABLE,
            )
        )

    }


}

package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef

class EntityDetailViewDefBuilder(private val entityDef: EntityDef) {


    private var pageTitle = entityDef.entityBaseName.toTitleCase()


    internal var entityEditPageDef: EntityEditPageDef? = null


    fun build(): EntityDetailViewDef {

        return EntityDetailViewDef(
            this.entityDef,
            this.pageTitle
        )

    }


    fun pageTitle(pageTitle: String) {

        this.pageTitle = pageTitle

    }


}

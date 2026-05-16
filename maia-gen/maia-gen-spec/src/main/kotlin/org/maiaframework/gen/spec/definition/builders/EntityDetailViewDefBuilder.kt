package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef

class EntityDetailViewDefBuilder(private val entityDef: EntityDef) {


    private var pageTitle = entityDef.entityBaseName.toTitleCase()


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

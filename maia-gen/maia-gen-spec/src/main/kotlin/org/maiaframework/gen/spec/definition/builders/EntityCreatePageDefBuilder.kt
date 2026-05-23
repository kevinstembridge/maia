package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EntityDef

@MaiaDslMarker
class EntityCreatePageDefBuilder(private val entityDef: EntityDef) {


    var pageTitle: String = "Create ${entityDef.entityBaseName.toTitleCase()}"


    var authority: AuthorityDef? = entityDef.entityCrudApiDef?.createApiDef?.crudApiDef?.authorityDef


    fun build(): EntityCreatePageDef {
        return EntityCreatePageDef(entityDef, pageTitle, authority)
    }


}

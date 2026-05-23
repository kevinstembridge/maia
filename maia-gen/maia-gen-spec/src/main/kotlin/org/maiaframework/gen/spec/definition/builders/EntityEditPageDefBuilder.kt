package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef

@MaiaDslMarker
class EntityEditPageDefBuilder(private val entityDef: EntityDef) {

    var pageTitle: String = "Edit ${entityDef.entityBaseName.toTitleCase()}"

    var authority: AuthorityDef? = entityDef.entityCrudApiDef?.updateApiDef?.crudApiDef?.authorityDef

    fun build(): EntityEditPageDef {
        return EntityEditPageDef(entityDef, pageTitle, authority)
    }

}

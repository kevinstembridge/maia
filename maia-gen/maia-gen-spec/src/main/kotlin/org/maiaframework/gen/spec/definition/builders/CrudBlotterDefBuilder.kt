package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.CrudBlotterDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef

@MaiaDslMarker
class CrudBlotterDefBuilder(
    private val blotterDef: BlotterDef,
    private val entityCrudApiDef: EntityCrudApiDef
) {


    fun build(): CrudBlotterDef {

        return CrudBlotterDef(blotterDef, entityCrudApiDef)

    }


}

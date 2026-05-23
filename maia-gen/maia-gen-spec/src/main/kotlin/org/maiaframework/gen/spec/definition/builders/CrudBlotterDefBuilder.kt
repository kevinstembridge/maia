package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.CrudBlotterDef
import org.maiaframework.gen.spec.definition.CrudBlotterPageDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef

@MaiaDslMarker
class CrudBlotterDefBuilder(
    private val blotterDef: BlotterDef,
    private val entityCrudApiDef: EntityCrudApiDef
) {


    internal var crudBlotterPageDef: CrudBlotterPageDef? = null


    fun withBlotterPage(init: CrudBlotterPageDefBuilder.() -> Unit) {

        val builder = CrudBlotterPageDefBuilder(blotterDef)
        builder.init()
        this.crudBlotterPageDef = builder.build()

    }


    fun build(): CrudBlotterDef {

        return CrudBlotterDef(blotterDef, entityCrudApiDef)

    }


}

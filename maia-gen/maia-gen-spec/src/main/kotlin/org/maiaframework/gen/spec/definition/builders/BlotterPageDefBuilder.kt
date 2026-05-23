package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.CrudBlotterPageDef


@MaiaDslMarker
class BlotterPageDefBuilder(private val blotterDef: BlotterDef) {


    var pageTitle: String = blotterDef.dtoBaseName.toTitleCase()


    fun build(): CrudBlotterPageDef {

        return CrudBlotterPageDef(blotterDef, pageTitle)

    }


}

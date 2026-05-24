package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterPageDef


@MaiaDslMarker
class BlotterPageDefBuilder(private val blotterDef: BlotterDef) {


    var pageTitle: String = blotterDef.dtoBaseName.toTitleCase()


    fun build(): BlotterPageDef {

        return BlotterPageDef(blotterDef, pageTitle)

    }


}

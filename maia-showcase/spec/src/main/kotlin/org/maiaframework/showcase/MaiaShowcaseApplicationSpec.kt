package org.maiaframework.showcase

import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.ModelDef


@Suppress("unused")
class MaiaShowcaseApplicationSpec : ApplicationSpec("org.maiaframework.showcase") {


    override val modelDefs: List<ModelDef> = listOf(
        MaiaShowcaseSpec().modelDef,
        MaiaShowcasePartySpec().modelDef
    )


}

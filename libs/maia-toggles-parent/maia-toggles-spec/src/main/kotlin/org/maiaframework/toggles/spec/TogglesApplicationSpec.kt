package org.maiaframework.toggles.spec

import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.ModelDef


@Suppress("unused")
class TogglesApplicationSpec: ApplicationSpec(
    defaultPackageName = "org.maiaframework.toggles",
    hazelcastConfigRequiresSpringComponentAnnotation = false
) {


    override val modelDefs: List<ModelDef> = listOf(TogglesSpec().modelDef)


}

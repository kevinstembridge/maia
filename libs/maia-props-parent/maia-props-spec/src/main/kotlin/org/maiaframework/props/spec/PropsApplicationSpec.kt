package org.maiaframework.props.spec

import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.ModelDef


@Suppress("unused")
class PropsApplicationSpec: ApplicationSpec("org.maiaframework.props", hazelcastConfigRequiresSpringComponentAnnotation = false) {


    override val modelDefs: List<ModelDef> = listOf(PropsSpec().modelDef)


}

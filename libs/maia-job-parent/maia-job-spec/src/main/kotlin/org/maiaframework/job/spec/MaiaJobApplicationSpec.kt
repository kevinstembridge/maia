package org.maiaframework.job.spec

import org.maiaframework.gen.spec.ApplicationSpec


@Suppress("unused")
class MaiaJobApplicationSpec: ApplicationSpec("org.maiaframework.job", hazelcastConfigRequiresSpringComponentAnnotation = false) {


    override val modelDefs = listOf(MaiaJobSpec().modelDef)


}

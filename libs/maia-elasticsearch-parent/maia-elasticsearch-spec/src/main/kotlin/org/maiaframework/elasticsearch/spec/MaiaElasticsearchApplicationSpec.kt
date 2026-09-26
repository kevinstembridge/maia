package org.maiaframework.elasticsearch.spec

import org.maiaframework.gen.spec.ApplicationSpec


@Suppress("unused")
class MaiaElasticsearchApplicationSpec: ApplicationSpec("org.maiaframework.job", hazelcastConfigRequiresSpringComponentAnnotation = false) {


    override val modelDefs = listOf(MaiaElasticSearchSpec().modelDef)


}

package org.maiaframework.storage.spec

import org.maiaframework.gen.spec.ApplicationSpec


@Suppress("unused")
class StorageApplicationSpec: ApplicationSpec("org.maiaframework.storage") {


    override val modelDefs = listOf(StorageSpec().modelDef)


}

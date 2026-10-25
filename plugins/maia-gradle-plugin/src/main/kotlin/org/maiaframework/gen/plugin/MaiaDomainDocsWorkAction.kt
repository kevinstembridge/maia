package org.maiaframework.gen.plugin

import org.gradle.workers.WorkAction
import org.maiaframework.gen.docs.runDomainDocs

abstract class MaiaDomainDocsWorkAction : WorkAction<MaiaDomainDocsWorkParameters> {

    override fun execute() {

        runDomainDocs(
            parameters.specClassNames.get(),
            parameters.outputDir.get().asFile.toPath()
        )

    }

}

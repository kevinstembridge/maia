package org.maiaframework.gen.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.workers.WorkParameters

interface MaiaDomainDocsWorkParameters : WorkParameters {

    val specClassNames: ListProperty<String>

    val outputDir: DirectoryProperty

}

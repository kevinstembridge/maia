package org.maiaframework.gen.plugin

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface GenerateModelWorkParameters : WorkParameters {


    val specificationClassName: Property<String>


}

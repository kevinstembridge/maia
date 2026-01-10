package org.maiaframework.gen.plugin

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector

interface MaiaGenerationDependencies : Dependencies {


    fun getImplementation(): DependencyCollector


}

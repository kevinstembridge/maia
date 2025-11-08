package org.maiaframework.gen.spec.definition.lang

data class TypescriptImport(
    val name: String,
    val from: String,
    val isModule: Boolean = false
)

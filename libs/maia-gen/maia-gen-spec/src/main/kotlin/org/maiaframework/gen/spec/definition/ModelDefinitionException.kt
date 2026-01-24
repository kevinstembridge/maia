package org.maiaframework.gen.spec.definition

open class ModelDefinitionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
)

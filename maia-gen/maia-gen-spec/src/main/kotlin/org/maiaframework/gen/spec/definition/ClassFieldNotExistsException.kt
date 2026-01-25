package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldName

class ClassFieldNotExistsException(
    val fieldName: ClassFieldName,
    errorMessage: String? = null
) : ModelDefinitionException(
    "Class definition does not have a field named '$fieldName'${if (errorMessage.isNullOrBlank()) "" else ": $errorMessage"}"
)

package org.maiaframework.gen.spec.definition.lang


enum class ClassVisibility(val javaKeyword: String, val kotlinKeyword: String) {


    PUBLIC("public", ""),
    PROTECTED("protected", "protected "),
    PACKAGE_PRIVATE("", "internal "),
    PRIVATE("private", "private ")


}

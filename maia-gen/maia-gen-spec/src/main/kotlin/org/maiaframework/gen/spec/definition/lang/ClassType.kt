package org.maiaframework.gen.spec.definition.lang

enum class ClassType(val javaText: String, val kotlinText: String) {

    CLASS("class", "class"),
    INTERFACE("interface", "interface"),
    ENUM("enum", "enum class"),
    DATA_CLASS("class", "data class"),
    OBJECT("class", "object");

}

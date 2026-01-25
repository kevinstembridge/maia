package org.maiaframework.gen.spec.definition

enum class JoinType(val sql: String) {

    INNER("inner"),
    LEFT_OUTER("left outer"),
    RIGHT_OUTER("right outer")
    
}

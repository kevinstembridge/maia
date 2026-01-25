package org.maiaframework.gen.spec.definition

enum class HazelcastCompatibleType(val kotlinUqcn: String) {

    BOOLEAN("Boolean"),
    COMPACT("YAGNI"),
    INT8("Byte"),
    INT32("Integer"),
    INT64("Long"),
    FLOAT32("Float"),
    FLOAT64("Double"),
    LOCAL_DATE("YAGNI"),
    OFFSET_DATE_TIME("Instant"),
    STRING("String");



}

package org.maiaframework.gen.schema.expected

data class ExpectedIndexDef(
    val name: String,
    val columns: List<String>,
    val unique: Boolean,
)

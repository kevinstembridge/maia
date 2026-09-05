package org.maiaframework.gen.schemacheck


data class ColumnMismatch(
    val name: String,
    val expectedType: String,
    val actualType: String,
    val expectedNullable: Boolean,
    val actualNullable: Boolean,
)

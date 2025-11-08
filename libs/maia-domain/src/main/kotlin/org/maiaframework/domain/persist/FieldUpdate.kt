package org.maiaframework.domain.persist

data class FieldUpdate(
    val classFieldName: String,
    val dbColumnName: String,
    val value: Any?
)

package org.maiaframework.webapp.domain

data class ForeignKeyReferencesExistResponseDto(
    val foreignEntityId: Any,
    val exists: Boolean,
    val entityKey: String?
)

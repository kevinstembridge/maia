package org.maiaframework.webapp.domain

import org.maiaframework.domain.DomainId

data class ForeignKeyReferencesExistResponseDto(
    val foreignEntityId: DomainId,
    val exists: Boolean,
    val entityKey: String?
)

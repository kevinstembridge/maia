package org.maiaframework.props

data class SetPropertyRequestDto(
    val propertyValue: String,
    val comment: String?
)

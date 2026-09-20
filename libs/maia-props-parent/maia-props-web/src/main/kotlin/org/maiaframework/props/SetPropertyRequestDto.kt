package org.maiaframework.props

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SetPropertyRequestDto(
    @field:NotBlank
    @field:Size(max = 2000)
    val propertyValue: String,
    @field:Size(max = 200)
    val comment: String?
)

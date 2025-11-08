package org.maiaframework.webapp.domain

/**
 * When the UI needs to make a call to the server to check any validation
 * constraints for data entered into a form by a user,  this is the response
 * the server sends back.
 */
data class FormValidationResponseDto(
    val invalid: Boolean,
    val message: String?
)

package org.maiaframework.props

import jakarta.validation.Valid
import org.maiaframework.webapp.domain.auth.CurrentUserHolder
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping($$"${maia.props.web.base-url:/api/ops}")
class MaiaPropsEndpoint(private val propsManager: PropsManager) {


    @GetMapping("/props", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_PROPS_READ')")
    fun getAllProperties(): List<PropertyResponseDto> {

        return this.propsManager.getAllProperties()

    }


    @GetMapping("/props/{propertyName}/history", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_PROPS_READ')")
    fun getPropertyHistory(
        @PathVariable propertyName: String
    ): List<PropertyHistoryItemResponseDto> {

        return this.propsManager.getPropertyHistory(propertyName)

    }


    @PostMapping("/props/{propertyName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('MAIA_PROPS_WRITE')")
    fun setProperty(
        @PathVariable propertyName: String,
        @Valid @RequestBody request: SetPropertyRequestDto
    ): PropertyResponseDto {

        val username = CurrentUserHolder.currentUsernameOrNull ?: "unknown"
        return this.propsManager.setProperty(propertyName, request.propertyValue, username, request.comment)

    }


    @DeleteMapping("/props/{propertyName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MAIA_PROPS_WRITE')")
    fun removeProperty(
        @PathVariable propertyName: String,
        @RequestParam(required = false) comment: String?
    ) {

        val username = CurrentUserHolder.currentUsernameOrNull ?: "unknown"
        this.propsManager.removeProperty(propertyName, username, comment)

    }


}

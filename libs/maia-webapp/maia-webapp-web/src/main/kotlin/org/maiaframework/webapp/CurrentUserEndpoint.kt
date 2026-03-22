package org.maiaframework.webapp

import org.maiaframework.webapp.domain.auth.CurrentUserHolder
import org.maiaframework.webapp.domain.user.UserSummaryDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
class CurrentUserEndpoint {


    @GetMapping("/api/current_user", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun currentUser(): UserSummaryDto {

        return CurrentUserHolder.currentUserSummaryOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    }


}

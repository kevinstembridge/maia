package org.maiaframework.toggles

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FeatureToggleEndpoint(private val toggleService: ToggleService) {


    @GetMapping("/api/maia/toggles", produces = ["application/json"])
    fun getFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleService.getAllFeatureToggles()

    }


}

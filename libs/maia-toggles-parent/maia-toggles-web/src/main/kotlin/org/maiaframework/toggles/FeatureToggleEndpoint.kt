package org.maiaframework.toggles

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class FeatureToggleEndpoint(private val toggleService: ToggleService) {


    @GetMapping("/api/maia_toggles/toggles", produces = ["application/json"])
    fun getFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleService.getAllFeatureToggles()

    }


    @GetMapping("/api/maia_toggles/{featureName}/is_active", produces = ["application/json"])
    fun isActive(@PathVariable featureName: FeatureName): FeatureToggleIsActiveResponseDto {

        return this.toggleService.isActive(featureName)

    }


}

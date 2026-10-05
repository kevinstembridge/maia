package org.maiaframework.toggles

import org.maiaframework.toggles.activation.ActivationStrategyDefinition
import org.maiaframework.toggles.activation.ActivationStrategyRegistry
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class FeatureToggleEndpoint(
    private val toggleService: ToggleService,
    private val activationStrategyRegistry: ActivationStrategyRegistry
) {


    @GetMapping("/api/ops/toggles/toggles", produces = ["application/json"])
    @PreAuthorize("hasAuthority('MAIA_TOGGLES_READ')")
    fun getFeatureToggles(): List<FeatureToggleResponseDto> {

        return this.toggleService.getAllFeatureToggles()

    }


    @GetMapping("/api/ops/toggles/strategies", produces = ["application/json"])
    @PreAuthorize("hasAuthority('MAIA_TOGGLES_READ')")
    fun getActivationStrategies(): List<ActivationStrategyDefinition> {

        return this.activationStrategyRegistry.getStrategyDefinitions()

    }


    @GetMapping("/api/ops/toggles/{featureName}/is-active", produces = ["application/json"])
    @PreAuthorize("hasAuthority('MAIA_TOGGLES_READ')")
    fun isActive(@PathVariable featureName: FeatureName): FeatureToggleIsActiveResponseDto {

        return this.toggleService.isActive(featureName)

    }


}

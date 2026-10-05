package org.maiaframework.toggles

import org.maiaframework.domain.DomainId
import org.maiaframework.problem.MaiaProblems
import org.maiaframework.toggles.activation.ActivationStrategyDescriptor
import org.maiaframework.toggles.activation.ActivationStrategyRegistry
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * Wraps the generated [FeatureToggleCrudService] so that activation strategies are validated
 * against the registered strategies before they are saved.
 */
@Component
class ToggleCrudService(
    private val delegate: FeatureToggleCrudService,
    private val activationStrategyRegistry: ActivationStrategyRegistry,
    private val maiaProblems: MaiaProblems
) {


    fun fetchForEdit(id: DomainId): FeatureToggleFetchForEditDto {

        return this.delegate.fetchForEdit(id)

    }


    fun update(editDto: FeatureToggleUpdateRequestDto) {

        validate(editDto.activationStrategies)
        this.delegate.update(editDto)

    }


    fun updateActivationStrategies(editDto: FeatureToggleUpdate_activationStrategiesRequestDto) {

        validate(editDto.activationStrategies)
        this.delegate.updateActivationStrategies(editDto)

    }


    private fun validate(activationStrategies: List<ActivationStrategyDescriptor>) {

        val problems = this.activationStrategyRegistry.validate(activationStrategies)

        if (problems.isNotEmpty()) {

            throw this.maiaProblems.errorResponse(
                "invalid_activation_strategies",
                "Invalid Activation Strategies",
                problems.joinToString("; "),
                HttpStatus.BAD_REQUEST,
                properties = mapOf("validationErrors" to problems)
            )

        }

    }


}

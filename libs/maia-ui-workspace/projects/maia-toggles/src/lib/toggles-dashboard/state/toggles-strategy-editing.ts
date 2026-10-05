import {ActivationStrategyDefinitionDto, ActivationStrategyParameterDefinitionDto} from '../models/ActivationStrategyDefinitionDto';
import {ActivationStrategyDescriptor, ActivationStrategyParameter} from '../models/ActivationStrategyDescriptor';


export interface StrategyRowValue {
    id: string;
    parameters: Record<string, string>;
}


/**
 * The parameters to show for a strategy. For a known strategy these are its defined parameters.
 * For a strategy the server no longer knows about, we fall back to whatever parameters are
 * already saved so that they remain visible and the strategy can be removed or kept as is.
 */
export function parameterDefinitionsFor(
    strategyId: string,
    definitions: ActivationStrategyDefinitionDto[],
    currentParameters: ActivationStrategyParameter[]
): ActivationStrategyParameterDefinitionDto[] {

    const definition = definitions.find(d => d.id === strategyId);

    if (definition) {
        return definition.parameters;
    }

    return currentParameters.map(p => ({name: p.name, description: null, required: false}));

}


export function toDescriptors(rows: StrategyRowValue[]): ActivationStrategyDescriptor[] {

    return rows.map(row => ({
        id: row.id,
        parameters: Object.entries(row.parameters)
            .map(([name, value]) => ({name, value: value.trim()}))
            .filter(p => p.value !== ''),
    }));

}


export function describeStrategy(descriptor: ActivationStrategyDescriptor): string {

    if (descriptor.parameters.length === 0) {
        return descriptor.id;
    }

    const parameters = descriptor.parameters.map(p => `${p.name}=${p.value}`).join(', ');
    return `${descriptor.id} (${parameters})`;

}

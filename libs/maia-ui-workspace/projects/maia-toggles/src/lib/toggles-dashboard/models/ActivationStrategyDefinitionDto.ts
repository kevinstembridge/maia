export interface ActivationStrategyParameterDefinitionDto {
    name: string;
    description: string | null;
    required: boolean;
}


export interface ActivationStrategyDefinitionDto {
    id: string;
    description: string | null;
    parameters: ActivationStrategyParameterDefinitionDto[];
}

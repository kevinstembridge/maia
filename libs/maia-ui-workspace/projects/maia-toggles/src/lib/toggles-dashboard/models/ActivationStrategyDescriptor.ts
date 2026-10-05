export interface ActivationStrategyParameter {
    name: string;
    value: string;
}


export interface ActivationStrategyDescriptor {
    id: string;
    parameters: ActivationStrategyParameter[];
}

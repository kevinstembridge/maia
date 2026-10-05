import {ActivationStrategyDescriptor} from './ActivationStrategyDescriptor';

export interface FeatureToggleResponseDto {
    id: string;
    featureName: string;
    enabled: boolean;
    description: string | null;
    ticketKey: string | null;
    reviewDate: string | null;
    contactPerson: string | null;
    infoLink: string | null;
    attributes: Record<string, string> | null;
    comment: string | null;
    activationStrategies: ActivationStrategyDescriptor[];
    lastModifiedBy: string;
    lastModifiedTimestamp: string;
    createdTimestamp: string;
    version: number;
}

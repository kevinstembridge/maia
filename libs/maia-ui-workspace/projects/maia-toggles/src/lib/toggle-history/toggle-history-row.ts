export interface ToggleHistoryRow {
    activationStrategies: ReadonlyArray<object>;
    attributes?: Record<string, string>;
    changeType: string;
    comment?: string;
    contactPerson?: string;
    description?: string;
    enabled: boolean;
    featureName: string;
    infoLink?: string;
    lastModifiedByUsername: string;
    lastModifiedTimestamp: string;
    reviewDate?: string;
    ticketKey?: string;
    version: number;
}

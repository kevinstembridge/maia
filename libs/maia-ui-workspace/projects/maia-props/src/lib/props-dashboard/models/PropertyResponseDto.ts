export interface PropertyResponseDto {
    propertyName: string;
    effectiveValue: string | null;
    isOverridden: boolean;
    isRedundant: boolean;
    environmentValue: string | null;
    sourceName: string | null;
    lastModifiedByUsername: string | null;
    lastModifiedTimestamp: string | null;
    comment: string | null;
    reviewDate: string | null;
}

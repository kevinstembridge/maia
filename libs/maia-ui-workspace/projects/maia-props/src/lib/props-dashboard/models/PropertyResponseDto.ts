export interface PropertyResponseDto {
    propertyName: string;
    effectiveValue: string | null;
    isOverridden: boolean;
    environmentValue: string | null;
    sourceName: string | null;
    lastModifiedByUsername: string | null;
    lastModifiedTimestamp: string | null;
    comment: string | null;
}

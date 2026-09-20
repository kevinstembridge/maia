export type ChangeType = 'CREATE' | 'UPDATE' | 'DELETE';

export interface PropertyHistoryItemResponseDto {
    propertyName: string;
    propertyValue: string;
    changeType: ChangeType;
    lastModifiedByUsername: string;
    lastModifiedTimestamp: string;
    comment: string | null;
    version: number;
}

import {PropertyResponseDto} from '../models/PropertyResponseDto';


export function filterProperties(
    properties: PropertyResponseDto[],
    nameFilter: string,
    overriddenOnly: boolean
): PropertyResponseDto[] {

    const normalizedFilter = nameFilter.trim().toLowerCase();

    return properties
        .filter(p => !overriddenOnly || p.isOverridden)
        .filter(p => normalizedFilter === '' || p.propertyName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.propertyName.localeCompare(b.propertyName));

}


export function countOverridden(properties: PropertyResponseDto[]): number {

    return properties.filter(p => p.isOverridden).length;

}

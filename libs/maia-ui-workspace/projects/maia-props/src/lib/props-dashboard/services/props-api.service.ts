import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PropertyResponseDto} from '../models/PropertyResponseDto';
import {PropertyHistoryItemResponseDto} from '../models/PropertyHistoryItemResponseDto';
import {PROPS_API_BASE_URL} from './props-api-base-url.token';


@Injectable()
export class PropsApiService {


    private baseUrl = inject(PROPS_API_BASE_URL);


    constructor(private http: HttpClient) {}


    getAllProperties(): Observable<PropertyResponseDto[]> {

        return this.http.get<PropertyResponseDto[]>(`${this.baseUrl}/props`);

    }


    getPropertyHistory(propertyName: string): Observable<PropertyHistoryItemResponseDto[]> {

        return this.http.get<PropertyHistoryItemResponseDto[]>(`${this.baseUrl}/props/${encodeURIComponent(propertyName)}/history`);

    }


    setProperty(propertyName: string, propertyValue: string, comment: string | null, reviewDate: string | null): Observable<PropertyResponseDto> {

        return this.http.post<PropertyResponseDto>(`${this.baseUrl}/props/${encodeURIComponent(propertyName)}`, {
            propertyValue,
            comment,
            reviewDate,
        });

    }


    removeProperty(propertyName: string, comment: string | null): Observable<void> {

        const params: Record<string, string> = comment ? {comment} : {};
        return this.http.delete<void>(`${this.baseUrl}/props/${encodeURIComponent(propertyName)}`, {params});

    }


}

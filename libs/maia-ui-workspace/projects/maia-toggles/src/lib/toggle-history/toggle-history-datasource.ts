import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {IDatasource, IGetRowsParams} from 'ag-grid-community';
import {TOGGLES_API_BASE_URL} from '../toggles-dashboard/services/toggles-api-base-url.token';
import {ToggleHistoryRow} from './toggle-history-row';


interface ToggleHistorySearchResult {
    results: ToggleHistoryRow[];
    totalResultCount: number;
}


@Injectable()
export class ToggleHistoryDatasource implements IDatasource {


    private readonly http = inject(HttpClient);
    private readonly baseUrl = inject(TOGGLES_API_BASE_URL);

    private toggleId!: string;


    setToggleId(id: string): void {

        this.toggleId = id;

    }


    getRows(params: IGetRowsParams): void {

        this.http.post<ToggleHistorySearchResult>(
            `${this.baseUrl}/feature-toggle/${this.toggleId}/history/search`,
            params
        ).subscribe({
            next: page => params.successCallback(page.results, page.totalResultCount),
            error: () => params.failCallback(),
        });

    }


}

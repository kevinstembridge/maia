import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
import {ElasticIndicesApiService} from './services/elastic-indices-api-service';
import {ElasticIndicesPageStore} from './state/elastic-indices-page-store';
import {ElasticIndex} from './components/elastic-index/elastic-index';
import {CreateIndexDialog} from './dialogs/create-index-dialog/create-index-dialog';
import {SetIndexVersionActiveDialog} from './dialogs/set-index-version-active-dialog/set-index-version-active-dialog';
import {
    buildElasticIndicesQueryParams,
    DisplayStatus,
    parseElasticIndicesFiltersFromParams,
    STATUS_COLORS,
    STATUS_TILE_ORDER
} from './state/elastic-indices-filtering';

const STATUS_LABELS: Record<DisplayStatus, string> = {
    green: 'Green',
    yellow: 'Yellow',
    red: 'Red',
    'not-created': 'Not created',
};


@Component({
    imports: [ElasticIndex, MatSlideToggle, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatButtonModule],
    providers: [ElasticIndicesApiService, ElasticIndicesPageStore],
    selector: 'maia-elastic-indices-page',
    templateUrl: './elastic-indices-page.html',
    styleUrl: './elastic-indices-page.scss'
})
export class ElasticIndicesPage implements OnInit {


    readonly store = inject(ElasticIndicesPageStore);

    readonly statusTileOrder = STATUS_TILE_ORDER;
    readonly statusColors = STATUS_COLORS;
    readonly statusLabels = STATUS_LABELS;

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private elasticIndicesService: ElasticIndicesApiService,
        private dialog: MatDialog
    ) {

        this.store.applyInitialFilters(parseElasticIndicesFiltersFromParams(this.route.snapshot.queryParamMap));

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildElasticIndicesQueryParams({
                    nameFilter: this.store.nameFilter(),
                    statusFilter: this.store.statusFilter(),
                    hideSystemIndices: this.store.hideSystemIndices(),
                }),
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.fetchAllIndices();
    }


    onFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onCreateIndex(dto: IndexStateResponseDto) {
        const dialogRef = this.dialog.open(CreateIndexDialog, {
            width: '400px',
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result && dto.managedIndexInfo) {
                this.elasticIndicesService.createIndex(dto.managedIndexInfo.indexBaseName, dto.managedIndexInfo.indexVersion).subscribe(() => {
                    this.store.fetchAllIndices();
                });
            }
        });
    }


    onSetIndexVersionActive(dto: IndexStateResponseDto) {
        const dialogRef = this.dialog.open(SetIndexVersionActiveDialog, {
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result && dto.managedIndexInfo) {
                this.elasticIndicesService.onSetIndexVersionActive(dto.managedIndexInfo.indexBaseName, dto.managedIndexInfo.indexVersion);
            }
        });
    }


}

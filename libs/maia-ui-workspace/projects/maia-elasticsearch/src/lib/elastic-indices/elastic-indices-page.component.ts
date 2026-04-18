import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {EsIndexStateDto} from './models/EsIndexStateDto';
import {ElasticIndicesApiService} from './services/elastic-indices-api.service';
import {ElasticIndicesPageStore} from './state/elastic-indices-page.store';
import {ElasticIndexComponent} from './components/elastic-index/elastic-index.component';
import {CreateIndexDialogComponent} from './dialogs/create-index-dialog/create-index-dialog.component';
import {SetIndexVersionActiveDialogComponent} from './dialogs/set-index-version-active-dialog/set-index-version-active-dialog.component';

@Component({
    imports: [ElasticIndexComponent, MatSlideToggle],
    providers: [ElasticIndicesApiService, ElasticIndicesPageStore],
    selector: 'maia-elastic-indices-page',
    templateUrl: './elastic-indices-page.component.html'
})
export class ElasticIndicesPageComponent implements OnInit {

    readonly store = inject(ElasticIndicesPageStore);

    constructor(
        private elasticIndicesService: ElasticIndicesApiService,
        private dialog: MatDialog
    ) {}

    ngOnInit() {
        this.store.fetchAllIndices();
    }

    onCreateIndex(dto: EsIndexStateDto) {
        const dialogRef = this.dialog.open(CreateIndexDialogComponent, {
            width: '400px',
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.elasticIndicesService.createIndex(dto.indexName);
            }
        });
    }

    onSetIndexVersionActive(dto: EsIndexStateDto) {
        const dialogRef = this.dialog.open(SetIndexVersionActiveDialogComponent, {
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.elasticIndicesService.onSetIndexVersionActive(dto.indexName);
            }
        });
    }

}

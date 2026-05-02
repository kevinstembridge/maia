import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {EsIndexStateDto} from './models/EsIndexStateDto';
import {ElasticIndicesApiService} from './services/elastic-indices-api-service';
import {ElasticIndicesPageStore} from './state/elastic-indices-page-store';
import {ElasticIndex} from './components/elastic-index/elastic-index';
import {CreateIndexDialog} from './dialogs/create-index-dialog/create-index-dialog';
import {SetIndexVersionActiveDialog} from './dialogs/set-index-version-active-dialog/set-index-version-active-dialog';


@Component({
    imports: [ElasticIndex, MatSlideToggle],
    providers: [ElasticIndicesApiService, ElasticIndicesPageStore],
    selector: 'maia-elastic-indices-page',
    templateUrl: './elastic-indices-page.html'
})
export class ElasticIndicesPage implements OnInit {


    readonly store = inject(ElasticIndicesPageStore);


    constructor(
        private elasticIndicesService: ElasticIndicesApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {
        this.store.fetchAllIndices();
    }


    onCreateIndex(dto: EsIndexStateDto) {
        const dialogRef = this.dialog.open(CreateIndexDialog, {
            width: '400px',
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.elasticIndicesService.createIndex(dto.indexName).subscribe(() => {
                    this.store.fetchAllIndices();
                });
            }
        });
    }


    onSetIndexVersionActive(dto: EsIndexStateDto) {
        const dialogRef = this.dialog.open(SetIndexVersionActiveDialog, {
            data: dto
        });
        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.elasticIndicesService.onSetIndexVersionActive(dto.indexName);
            }
        });
    }


}

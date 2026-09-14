import {Component, computed, input, output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';
import {deriveDisplayStatus, STATUS_COLORS} from '../../state/elastic-indices-filtering';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html',
    styleUrl: './elastic-index.scss'
})
export class ElasticIndex {

    index = input.required<EsIndexStateDto>();

    createIndex = output<EsIndexStateDto>();
    setIndexVersionActive = output<EsIndexStateDto>();

    statusColor = computed<string | undefined>(() => {
        const status = deriveDisplayStatus(this.index());
        return status && status !== 'not-created' ? STATUS_COLORS[status] : undefined;
    });

    onCreateIndex() {
        this.createIndex.emit(this.index());
    }

    onSetIndexVersionActive() {
        this.setIndexVersionActive.emit(this.index());
    }

}

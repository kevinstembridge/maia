import {Component, computed, input, output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';

const STATUS_COLORS: Record<string, string> = {
    green: '#4caf50',
    yellow: '#fbc02d',
    red: '#d32f2f',
};
const UNKNOWN_STATUS_COLOR = '#9e9e9e';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html'
})
export class ElasticIndex {

    index = input.required<EsIndexStateDto>();

    createIndex = output<EsIndexStateDto>();
    setIndexVersionActive = output<EsIndexStateDto>();

    statusColor = computed<string | undefined>(() => {
        if (!this.index().indexExists) {
            return undefined;
        }
        const status = this.index().health?.status?.toLowerCase();
        return status ? (STATUS_COLORS[status] ?? UNKNOWN_STATUS_COLOR) : undefined;
    });

    onCreateIndex() {
        this.createIndex.emit(this.index());
    }

    onSetIndexVersionActive() {
        this.setIndexVersionActive.emit(this.index());
    }

}

import {Component, computed, input, output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {deriveDisplayStatus, STATUS_COLORS} from '../../state/elastic-indices-filtering';
import {
  EsIndexStateResponseDto
} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/EsIndexStateResponseDto';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html',
    styleUrl: './elastic-index.scss'
})
export class ElasticIndex {

    index = input.required<EsIndexStateResponseDto>();

    createIndex = output<EsIndexStateResponseDto>();
    setIndexVersionActive = output<EsIndexStateResponseDto>();

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

import {Component, computed, input, output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {deriveDisplayStatus, STATUS_COLORS} from '../../state/elastic-indices-filtering';
import {
  IndexStateResponseDto
} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html',
    styleUrl: './elastic-index.scss'
})
export class ElasticIndex {

    index = input.required<IndexStateResponseDto>();

    createIndex = output<IndexStateResponseDto>();
    setIndexVersionActive = output<IndexStateResponseDto>();

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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.component.html'
})
export class ElasticIndexComponent {

    @Input() index!: EsIndexStateDto;

    @Output() createIndex = new EventEmitter<EsIndexStateDto>();
    @Output() setIndexVersionActive = new EventEmitter<EsIndexStateDto>();

    onCreateIndex() {
        this.createIndex.emit(this.index);
    }

    onSetIndexVersionActive() {
        this.setIndexVersionActive.emit(this.index);
    }

}

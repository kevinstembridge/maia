import {Component, Input, output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.component.html'
})
export class ElasticIndexComponent {

    @Input() index!: EsIndexStateDto;

    createIndex = output<EsIndexStateDto>();
    setIndexVersionActive = output<EsIndexStateDto>();

    onCreateIndex() {
        this.createIndex.emit(this.index);
    }

    onSetIndexVersionActive() {
        this.setIndexVersionActive.emit(this.index);
    }

}

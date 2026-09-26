import {Component, computed, input, output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {PropertyResponseDto} from '../../models/PropertyResponseDto';
import {isOverdue} from '../../state/props-dashboard-filtering';


@Component({
    selector: 'maia-props-card',
    imports: [DatePipe, MatButtonModule, MatIconModule],
    templateUrl: './props-card.html',
    styleUrl: './props-card.scss'
})
export class PropsCard {


    property = input.required<PropertyResponseDto>();

    edit = output<PropertyResponseDto>();
    remove = output<PropertyResponseDto>();
    history = output<PropertyResponseDto>();

    overdue = computed<boolean>(() => isOverdue(this.property().reviewDate));

    accentStatus = computed<'overdue' | 'redundant' | 'overridden' | 'default'>(() => {
        if (this.overdue()) {
            return 'overdue';
        }
        if (this.property().isRedundant) {
            return 'redundant';
        }
        if (this.property().isOverridden) {
            return 'overridden';
        }
        return 'default';
    });


    onEdit(): void {
        this.edit.emit(this.property());
    }


    onRemove(): void {
        this.remove.emit(this.property());
    }


    onHistory(): void {
        this.history.emit(this.property());
    }


}

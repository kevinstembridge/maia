import {Component, computed, input, output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {FeatureToggleResponseDto} from '../../models/FeatureToggleResponseDto';
import {isOverdue} from '../../state/toggles-dashboard-filtering';
import {describeStrategy} from '../../state/toggles-strategy-editing';


@Component({
    selector: 'maia-toggle-card',
    imports: [DatePipe, MatButtonModule, MatIconModule],
    templateUrl: './toggle-card.html',
    styleUrl: './toggle-card.scss'
})
export class ToggleCard {


    toggle = input.required<FeatureToggleResponseDto>();

    changeState = output<FeatureToggleResponseDto>();
    editStrategies = output<FeatureToggleResponseDto>();
    history = output<FeatureToggleResponseDto>();

    overdue = computed<boolean>(() => isOverdue(this.toggle().reviewDate));

    accentStatus = computed<'overdue' | 'enabled' | 'disabled'>(() => {
        if (this.overdue()) {
            return 'overdue';
        }
        return this.toggle().enabled ? 'enabled' : 'disabled';
    });

    strategySummaries = computed<string[]>(() => this.toggle().activationStrategies.map(describeStrategy));


    onChangeState(): void {
        this.changeState.emit(this.toggle());
    }


    onEditStrategies(): void {
        this.editStrategies.emit(this.toggle());
    }


    onHistory(): void {
        this.history.emit(this.toggle());
    }


}

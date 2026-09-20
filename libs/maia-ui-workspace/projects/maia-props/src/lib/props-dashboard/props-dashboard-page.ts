import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatTableModule} from '@angular/material/table';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSlideToggleModule, MatSlideToggleChange} from '@angular/material/slide-toggle';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {PropsApiService} from './services/props-api.service';
import {PropsDashboardStore} from './state/props-dashboard-store';
import {PropertyResponseDto} from './models/PropertyResponseDto';
import {EditPropertyDialog, EditPropertyDialogData, EditPropertyDialogResult} from './dialogs/edit-property-dialog/edit-property-dialog';
import {RemoveOverrideDialog, RemoveOverrideDialogData, RemoveOverrideDialogResult} from './dialogs/remove-override-dialog/remove-override-dialog';
import {PropertyHistoryDialog, PropertyHistoryDialogData} from './dialogs/property-history-dialog/property-history-dialog';


@Component({
    selector: 'maia-props-dashboard-page',
    templateUrl: './props-dashboard-page.html',
    styleUrl: './props-dashboard-page.scss',
    imports: [MatTableModule, MatFormFieldModule, MatInputModule, MatSlideToggleModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
    providers: [PropsApiService, PropsDashboardStore]
})
export class PropsDashboardPage implements OnInit {


    readonly store = inject(PropsDashboardStore);

    readonly displayedColumns = ['propertyName', 'effectiveValue', 'isOverridden', 'sourceName', 'lastModifiedByUsername', 'lastModifiedTimestamp', 'actions'];


    constructor(
        private propsService: PropsApiService,
        private dialog: MatDialog
    ) {}


    ngOnInit() {
        this.store.fetchAllProperties();
    }


    onNameFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onOverriddenOnlyToggled(change: MatSlideToggleChange) {
        this.store.onOverriddenOnlyToggled(change.checked);
    }


    onAddOverride() {

        const data: EditPropertyDialogData = {propertyName: null, currentValue: null};
        const dialogRef = this.dialog.open(EditPropertyDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: EditPropertyDialogResult | undefined) => {
            if (result) {
                this.propsService.setProperty(result.propertyName, result.propertyValue, result.comment).subscribe(updated => {
                    this.store.applyPropertyUpdate(updated);
                });
            }
        });

    }


    onEdit(row: PropertyResponseDto) {

        const data: EditPropertyDialogData = {propertyName: row.propertyName, currentValue: row.effectiveValue};
        const dialogRef = this.dialog.open(EditPropertyDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: EditPropertyDialogResult | undefined) => {
            if (result) {
                this.propsService.setProperty(result.propertyName, result.propertyValue, result.comment).subscribe(updated => {
                    this.store.applyPropertyUpdate(updated);
                });
            }
        });

    }


    onRemove(row: PropertyResponseDto) {

        const data: RemoveOverrideDialogData = {propertyName: row.propertyName};
        const dialogRef = this.dialog.open(RemoveOverrideDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: RemoveOverrideDialogResult | undefined) => {
            if (result) {
                this.propsService.removeProperty(row.propertyName, result.comment).subscribe(() => {
                    this.store.retryFetch();
                });
            }
        });

    }


    onHistory(row: PropertyResponseDto) {

        this.propsService.getPropertyHistory(row.propertyName).subscribe(historyItems => {
            const data: PropertyHistoryDialogData = {propertyName: row.propertyName, historyItems};
            this.dialog.open(PropertyHistoryDialog, {width: '600px', data});
        });

    }


}

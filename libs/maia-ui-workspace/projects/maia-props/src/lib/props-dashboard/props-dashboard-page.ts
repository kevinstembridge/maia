import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
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
import {buildPropsQueryParams, parsePropsFiltersFromParams} from './state/props-dashboard-filtering';
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

    readonly displayedColumns = ['propertyName', 'effectiveValue', 'isOverridden', 'isRedundant', 'sourceName', 'lastModifiedByUsername', 'lastModifiedTimestamp', 'reviewDate', 'actions'];

    private route = inject(ActivatedRoute);
    private router = inject(Router);


    constructor(
        private propsService: PropsApiService,
        private dialog: MatDialog
    ) {

        const initialFilters = parsePropsFiltersFromParams(this.route.snapshot.queryParamMap);
        this.store.onNameFilterChanged(initialFilters.nameFilter);
        this.store.onOverriddenOnlyToggled(initialFilters.overriddenOnly);
        this.store.onRedundantOnlyToggled(initialFilters.redundantOnly);
        this.store.onOverdueOnlyToggled(initialFilters.overdueOnly);

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildPropsQueryParams({
                    nameFilter: this.store.nameFilter(),
                    overriddenOnly: this.store.overriddenOnly(),
                    redundantOnly: this.store.redundantOnly(),
                    overdueOnly: this.store.overdueOnly(),
                }),
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.fetchAllProperties();
    }


    onNameFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onOverriddenOnlyToggled(change: MatSlideToggleChange) {
        this.store.onOverriddenOnlyToggled(change.checked);
    }


    onRedundantOnlyToggled(change: MatSlideToggleChange) {
        this.store.onRedundantOnlyToggled(change.checked);
    }


    onOverdueOnlyToggled(change: MatSlideToggleChange) {
        this.store.onOverdueOnlyToggled(change.checked);
    }


    onAddOverride() {

        this.openEditDialog({propertyName: null, currentValue: null, currentReviewDate: null});

    }


    onEdit(row: PropertyResponseDto) {

        this.openEditDialog({propertyName: row.propertyName, currentValue: row.effectiveValue, currentReviewDate: row.reviewDate});

    }


    private openEditDialog(data: EditPropertyDialogData) {

        const dialogRef = this.dialog.open(EditPropertyDialog, {width: '600px', data});

        dialogRef.afterClosed().subscribe((result: EditPropertyDialogResult | undefined) => {
            if (result) {
                this.propsService.setProperty(result.propertyName, result.propertyValue, result.comment, result.reviewDate).subscribe(updated => {
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
                // Deliberately refetches rather than using store.applyPropertyRemoval(): removing an
                // override doesn't necessarily remove the row — if the property also has a real
                // Environment value (the common case), the row must revert to showing that value,
                // not disappear. Only a refetch can know which outcome applies.
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

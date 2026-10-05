import {Component, effect, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {TogglesApiService} from './services/toggles-api.service';
import {TOGGLES_HISTORY_ROUTE} from './services/toggles-history-route.token';
import {TogglesDashboardStore} from './state/toggles-dashboard-store';
import {buildTogglesQueryParams, parseTogglesFiltersFromParams, ToggleStateFilter, TOGGLES_TILE_COLORS} from './state/toggles-dashboard-filtering';
import {FeatureToggleResponseDto} from './models/FeatureToggleResponseDto';
import {ToggleStateDialog, ToggleStateDialogData, ToggleStateDialogResult} from './dialogs/toggle-state-dialog/toggle-state-dialog';
import {EditStrategiesDialog, EditStrategiesDialogData, EditStrategiesDialogResult} from './dialogs/edit-strategies-dialog/edit-strategies-dialog';
import {ToggleCard} from './components/toggle-card/toggle-card';


@Component({
    selector: 'maia-toggles-dashboard-page',
    templateUrl: './toggles-dashboard-page.html',
    styleUrl: './toggles-dashboard-page.scss',
    imports: [MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule, ToggleCard],
    providers: [TogglesApiService, TogglesDashboardStore]
})
export class TogglesDashboardPage implements OnInit {


    readonly store = inject(TogglesDashboardStore);

    readonly tileColors = TOGGLES_TILE_COLORS;

    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly dialog = inject(MatDialog);
    private readonly togglesService = inject(TogglesApiService);
    private readonly historyRoute = inject(TOGGLES_HISTORY_ROUTE);


    constructor() {

        const initialFilters = parseTogglesFiltersFromParams(this.route.snapshot.queryParamMap);
        this.store.onNameFilterChanged(initialFilters.nameFilter);
        this.store.onStateFilterChanged(initialFilters.stateFilter);
        this.store.onOverdueOnlyToggled(initialFilters.overdueOnly);

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildTogglesQueryParams({
                    nameFilter: this.store.nameFilter(),
                    stateFilter: this.store.stateFilter(),
                    overdueOnly: this.store.overdueOnly(),
                }),
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.fetchAllToggles();
        this.store.fetchStrategies();
    }


    onNameFilterInput(event: Event) {
        this.store.onNameFilterChanged((event.target as HTMLInputElement).value);
    }


    onStateFilterToggled(state: Exclude<ToggleStateFilter, 'all'>): void {
        this.store.onStateFilterChanged(this.store.stateFilter() === state ? 'all' : state);
    }


    onOverdueOnlyToggled(): void {
        this.store.onOverdueOnlyToggled(!this.store.overdueOnly());
    }


    onChangeState(toggle: FeatureToggleResponseDto) {

        const data: ToggleStateDialogData = {featureName: toggle.featureName, enable: !toggle.enabled};
        const dialogRef = this.dialog.open(ToggleStateDialog, {width: '480px', data});

        dialogRef.afterClosed().subscribe((result: ToggleStateDialogResult | undefined) => {
            if (result) {
                this.togglesService.setToggle(toggle.featureName, data.enable, result.comment, toggle.version).subscribe({
                    next: () => this.store.retryFetch(),
                    error: (err) => this.onUpdateFailed(`Failed to ${data.enable ? 'enable' : 'disable'} ${toggle.featureName}.`, err),
                });
            }
        });

    }


    onEditStrategies(toggle: FeatureToggleResponseDto) {

        const data: EditStrategiesDialogData = {
            featureName: toggle.featureName,
            strategies: toggle.activationStrategies,
            definitions: this.store.strategies(),
        };
        const dialogRef = this.dialog.open(EditStrategiesDialog, {width: '640px', data});

        dialogRef.afterClosed().subscribe((result: EditStrategiesDialogResult | undefined) => {
            if (result) {
                this.togglesService.updateActivationStrategies(toggle.id, toggle.version, result.strategies).subscribe({
                    next: () => this.store.retryFetch(),
                    error: (err) => this.onUpdateFailed(`Failed to update the strategies of ${toggle.featureName}.`, err),
                });
            }
        });

    }


    onHistory(toggle: FeatureToggleResponseDto) {

        this.router.navigate([this.historyRoute, toggle.id]);

    }


    // Refetches because a failed update usually means the toggle changed since we loaded it
    // (its version is stale) and the card is showing out-of-date state.
    private onUpdateFailed(message: string, err: unknown): void {

        console.error(err);
        this.store.onActionFailed(`${message} It may have been changed by someone else; the list has been refreshed.`);
        this.store.retryFetch();

    }


}

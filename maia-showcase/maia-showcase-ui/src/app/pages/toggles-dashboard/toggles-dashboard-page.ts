import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {TogglesDashboardPage as MaiaTogglesDashboardPageComponent} from '@maia/maia-toggles';

@Component({
    imports: [PageLayout, MaiaTogglesDashboardPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Feature Toggles" dataPageId="toggles_dashboard">
            <maia-toggles-dashboard-page />
        </maia-page-layout>
    `
})
export class TogglesDashboardPage {}

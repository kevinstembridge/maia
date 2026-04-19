import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {JobsDashboardPageComponent as MaiaJobsDashboardPageComponent} from '@maia/maia-jobs';

@Component({
    imports: [PageLayoutComponent, MaiaJobsDashboardPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <app-page-layout pageTitle="Jobs">
            <maia-jobs-dashboard-page />
        </app-page-layout>
    `
})
export class JobsDashboardPageComponent {}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {PropsDashboardPage as MaiaPropsDashboardPageComponent} from '@maia/maia-props';

@Component({
    imports: [PageLayout, MaiaPropsDashboardPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Property Overrides" dataPageId="props_dashboard">
            <maia-props-dashboard-page />
        </maia-page-layout>
    `
})
export class PropsDashboardPage {}

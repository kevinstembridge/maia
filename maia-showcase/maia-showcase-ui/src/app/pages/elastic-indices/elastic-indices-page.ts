import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {ElasticIndicesPage as MaiaElasticIndicesPageComponent} from '@maia/maia-elasticsearch';

@Component({
    imports: [PageLayout, MaiaElasticIndicesPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Elastic Indices">
            <maia-elastic-indices-page />
        </maia-page-layout>
    `
})
export class ElasticIndicesPage {}

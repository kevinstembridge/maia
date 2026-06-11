import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {ElasticIndicesPage as MaiaElasticIndicesPageComponent} from '@maia/maia-elasticsearch';

@Component({
    imports: [PageLayout, MaiaElasticIndicesPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <app-page-layout pageTitle="Elastic Indices">
            <maia-elastic-indices-page />
        </app-page-layout>
    `
})
export class ElasticIndicesPage {}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {ElasticIndicesPageComponent as MaiaElasticIndicesPageComponent} from '@maia/maia-elasticsearch';

@Component({
    imports: [PageLayoutComponent, MaiaElasticIndicesPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <app-page-layout pageTitle="Elastic Indices">
            <maia-elastic-indices-page />
        </app-page-layout>
    `
})
export class ElasticIndicesPageComponent {}

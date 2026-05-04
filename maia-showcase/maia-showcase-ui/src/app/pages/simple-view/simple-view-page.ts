import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {SimpleEntityDetailView} from '@app/gen-components/org/maiaframework/showcase/simple/simple-entity-detail-view';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout, SimpleEntityDetailView],
    selector: 'app-simple-view',
    templateUrl: './simple-view-page.html',
})
export class SimpleViewPage {


    private readonly route = inject(ActivatedRoute);


    protected readonly entityId = toSignal(
        this.route.paramMap.pipe(
            map(p => p.get('id'))
        )
    );


}

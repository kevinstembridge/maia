import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {
    CompositePrimaryKeyEntityDetailView
} from '@app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-entity-detail-view';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout, CompositePrimaryKeyEntityDetailView],
    selector: 'app-composite-primary-key-view',
    templateUrl: './composite-primary-key-view-page.html',
})
export class CompositePrimaryKeyViewPage {


    private readonly route = inject(ActivatedRoute);


    protected readonly entityId = toSignal(
        this.route.paramMap.pipe(
            map(p => p.get('id'))
        )
    );


}

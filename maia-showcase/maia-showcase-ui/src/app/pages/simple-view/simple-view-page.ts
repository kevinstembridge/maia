import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {filter, map, switchMap} from 'rxjs';
import {SimpleEntityDetailViewService} from '@app/gen-components/org/maiaframework/showcase/simple/simple-entity-detail-view-service';
import {PageLayout} from '@app/components/page-layout/page-layout';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout],
    selector: 'app-simple-view',
    templateUrl: './simple-view-page.html',
    styles: [`
        .simple-view-fields { display: flex; flex-direction: column; gap: 1rem; padding: 1rem 0; }
        .simple-view-field dt { font-weight: 600; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: #6b7280; margin-bottom: 0.25rem; }
        .simple-view-field dd { margin: 0; }
    `],
})
export class SimpleViewPage {


    private readonly entityDetailViewService = inject(SimpleEntityDetailViewService);


    private readonly route = inject(ActivatedRoute);


    protected readonly simpleEntityDetailDto = toSignal(
        this.route.paramMap.pipe(
            map(p => p.get('id')),
            filter((id): id is string => id !== null),
            switchMap(id => this.entityDetailViewService.fetch(id))
        )
    );


}

import {Component, computed, effect, inject, OnInit} from '@angular/core';
import {DateTime} from 'luxon';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {JobExecutionHistoryCardComponent} from './components/job-execution-history-card/job-execution-history-card.component';
import {JobsApiService} from '../jobs-dashboard/services/jobs-api.service';
import {StacktraceDialogComponent} from '../jobs-dashboard/dialogs/stacktrace-dialog/stacktrace-dialog.component';
import {JobMetricsDialogComponent} from '../jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {JobHistoryStore} from './state/job-history-store';
import {
    buildHistoryFilterQueryParams,
    HistoryStatusFilter,
    parseHistoryFiltersFromParams,
    toEndOfDayIso,
    toStartOfDayIso
} from './state/job-history-filtering';

@Component({
    imports: [
        JobExecutionHistoryCardComponent, MatFormFieldModule, MatInputModule, MatSelectModule, MatDatepickerModule,
        MatButtonModule, MatIconModule, MatPaginatorModule, MatProgressSpinnerModule, RouterLink
    ],
    providers: [JobsApiService, JobHistoryStore],
    selector: 'maia-job-history-page',
    templateUrl: './job-history-page.component.html',
    styleUrl: './job-history-page.component.scss'
})
export class JobHistoryPageComponent implements OnInit {


    readonly store = inject(JobHistoryStore);

    private dialog = inject(MatDialog);

    private jobsService = inject(JobsApiService);

    private route = inject(ActivatedRoute);

    private router = inject(Router);

    readonly fromDateValue = computed<DateTime | null>(() => {
        const fromDate = this.store.fromDate();
        return fromDate ? DateTime.fromISO(fromDate) : null;
    });

    readonly toDateValue = computed<DateTime | null>(() => {
        const toDate = this.store.toDate();
        return toDate ? DateTime.fromISO(toDate) : null;
    });


    constructor() {

        this.store.applyInitialFilters(parseHistoryFiltersFromParams(this.route.snapshot.queryParamMap));

        effect(() => {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: buildHistoryFilterQueryParams({
                    jobNameFilter: this.store.jobNameFilter(),
                    statusFilter: this.store.statusFilter(),
                    fromDate: this.store.fromDate(),
                    toDate: this.store.toDate(),
                }),
                replaceUrl: true,
            });
        });

    }


    ngOnInit() {
        this.store.init();
    }


    onJobNameFilterChanged(value: string | null) {
        this.store.onJobNameFilterChanged(value);
    }


    onStatusFilterChanged(value: HistoryStatusFilter) {
        this.store.onStatusFilterChanged(value);
    }


    onFromDateChanged(date: DateTime | null) {
        this.store.onDateRangeChanged(toStartOfDayIso(date), this.store.toDate());
    }


    onToDateChanged(date: DateTime | null) {
        this.store.onDateRangeChanged(this.store.fromDate(), toEndOfDayIso(date));
    }


    onPageChanged(event: PageEvent) {
        this.store.onPageChanged(event.pageIndex, event.pageSize);
    }


    onDisplayStackTrace(jobExecutionId: string) {

        this.jobsService.getStacktrace(jobExecutionId).subscribe(res => {
            this.dialog.open(StacktraceDialogComponent, {data: res.stacktrace, width: '90vw', maxWidth: '90vw'});
        });

    }


    onDisplayJobMetrics(metrics: any) {

        this.dialog.open(JobMetricsDialogComponent, {data: metrics, width: '90vw', maxWidth: '90vw'});

    }


}

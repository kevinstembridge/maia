import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatButtonModule} from '@angular/material/button';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {JobExecutionHistoryCardComponent} from './components/job-execution-history-card/job-execution-history-card.component';
import {JobsApiService} from '../jobs-dashboard/services/jobs-api.service';
import {StacktraceDialogComponent} from '../jobs-dashboard/dialogs/stacktrace-dialog/stacktrace-dialog.component';
import {JobMetricsDialogComponent} from '../jobs-dashboard/dialogs/job-metrics-dialog/job-metrics-dialog.component';
import {JobHistoryStore} from './state/job-history-store';
import {HistoryStatusFilter} from './state/job-history-filtering';

@Component({
    imports: [
        JobExecutionHistoryCardComponent, MatFormFieldModule, MatSelectModule, MatDatepickerModule,
        MatButtonModule, MatPaginatorModule, MatProgressSpinnerModule
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


    ngOnInit() {
        this.store.init();
    }


    onJobNameFilterChanged(value: string | null) {
        this.store.onJobNameFilterChanged(value);
    }


    onStatusFilterChanged(value: HistoryStatusFilter) {
        this.store.onStatusFilterChanged(value);
    }


    onFromDateChanged(date: Date | null) {
        this.store.onDateRangeChanged(this.toStartOfDayIso(date), this.store.toDate());
    }


    onToDateChanged(date: Date | null) {
        this.store.onDateRangeChanged(this.store.fromDate(), this.toEndOfDayIso(date));
    }


    onPageChanged(event: PageEvent) {
        this.store.onPageChanged(event.pageIndex, event.pageSize);
    }


    onDisplayStackTrace(jobExecutionId: string) {

        this.jobsService.getStacktrace(jobExecutionId).subscribe(res => {
            this.dialog.open(StacktraceDialogComponent, {data: res.stacktrace});
        });

    }


    onDisplayJobMetrics(metrics: any) {

        this.dialog.open(JobMetricsDialogComponent, {data: metrics});

    }


    private toStartOfDayIso(date: Date | null): string | null {

        if (!date) {
            return null;
        }

        const startOfDay = new Date(date);
        startOfDay.setHours(0, 0, 0, 0);
        return startOfDay.toISOString();

    }


    private toEndOfDayIso(date: Date | null): string | null {

        if (!date) {
            return null;
        }

        const endOfDay = new Date(date);
        endOfDay.setHours(23, 59, 59, 999);
        return endOfDay.toISOString();

    }


}

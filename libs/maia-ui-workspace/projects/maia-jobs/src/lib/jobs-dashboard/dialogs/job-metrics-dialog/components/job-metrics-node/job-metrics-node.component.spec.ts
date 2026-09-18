import {describe, expect, it} from 'vitest';
import {TestBed} from '@angular/core/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {JobMetricsNodeComponent} from './job-metrics-node.component';
import {JobMetricsReport} from '../../../../models/JobMetricsReport';

function makeReport(overrides: Partial<JobMetricsReport> = {}): JobMetricsReport {
    return {
        jobName: 'root-job',
        jobCount: 1,
        context: {},
        totalElapsedTime: {seconds: 1.5, formatted: '1.5s'},
        ...overrides,
    };
}

async function renderNode(report: JobMetricsReport) {
    await TestBed.configureTestingModule({
        imports: [JobMetricsNodeComponent],
        providers: [provideZonelessChangeDetection()],
    }).compileComponents();

    const fixture = TestBed.createComponent(JobMetricsNodeComponent);
    fixture.componentRef.setInput('report', report);
    await fixture.whenStable();

    return fixture;
}

function textOf(fixture: {nativeElement: unknown}): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
}

describe('JobMetricsNodeComponent', () => {

    it('renders the job name and formatted elapsed time for a leaf node', async () => {
        const fixture = await renderNode(makeReport({
            jobName: 'leaf-job',
            totalElapsedTime: {seconds: 2, formatted: '2s'},
        }));

        expect(textOf(fixture)).toContain('leaf-job');
        expect(textOf(fixture)).toContain('2s');
    });

    it('omits the timing stats section when min is absent', async () => {
        const fixture = await renderNode(makeReport());

        expect(textOf(fixture)).not.toContain('Median');
    });

    it('renders timing stats when min is present', async () => {
        const fixture = await renderNode(makeReport({
            min: 0.1, median: 0.2, '95th': 0.3, max: 0.4, mean: 0.25,
        }));
        const text = textOf(fixture);

        expect(text).toContain('0.100s');
        expect(text).toContain('0.200s');
        expect(text).toContain('0.300s');
        expect(text).toContain('0.400s');
        expect(text).toContain('0.250s');
    });

    it('omits context, counters and ratios sections when absent', async () => {
        const fixture = await renderNode(makeReport());

        expect(textOf(fixture)).not.toContain('Counters');
    });

    it('renders context, counters and ratios entries when present', async () => {
        const fixture = await renderNode(makeReport({
            context: {environment: 'test'},
            counters: {itemCount: 5},
            ratios: {successRatio: {toString: '80%', value: 0.8}},
        }));
        const text = textOf(fixture);

        expect(text).toContain('environment: test');
        expect(text).toContain('itemCount: 5');
        expect(text).toContain('successRatio: 80% (0.8)');
    });

    it('renders nested child jobs recursively', async () => {
        const fixture = await renderNode(makeReport({
            jobName: 'root-job',
            childJobs: [
                makeReport({
                    jobName: 'child-job',
                    childJobs: [makeReport({jobName: 'grandchild-job'})],
                }),
            ],
        }));
        const text = textOf(fixture);

        expect(text).toContain('child-job');
        expect(text).toContain('grandchild-job');
    });

    it('hides child jobs when the toggle is collapsed', async () => {
        const fixture = await renderNode(makeReport({
            childJobs: [makeReport({jobName: 'child-job'})],
        }));

        const toggle = (fixture.nativeElement as HTMLElement).querySelector('button.toggle') as HTMLButtonElement;
        toggle.click();
        await fixture.whenStable();

        expect(textOf(fixture)).not.toContain('child-job');
    });

});

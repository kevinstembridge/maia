import {describe, expect, it} from 'vitest';
import {TestBed} from '@angular/core/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {JobExecutionHistoryCardComponent} from './job-execution-history-card.component';
import {JobExecutionHistoryItemResponseDto} from '@app/gen-components/org/maiaframework/job/JobExecutionHistoryItemResponseDto';
import {JobExecutionStatus} from '@app/gen-components/org/maiaframework/job/JobExecutionStatus';

function makeItem(overrides: Partial<JobExecutionHistoryItemResponseDto>): JobExecutionHistoryItemResponseDto {
    return {
        jobExecutionId: 'exec-1',
        jobName: 'some-job',
        invokedBy: 'someone',
        startTimestamp: '2026-01-01T00:00:00Z',
        status: JobExecutionStatus.RUNNING,
        metrics: {},
        ...overrides,
    };
}

async function render(item: JobExecutionHistoryItemResponseDto, canAbandon?: boolean) {
    await TestBed.configureTestingModule({
        imports: [JobExecutionHistoryCardComponent],
        providers: [provideZonelessChangeDetection()],
    }).compileComponents();

    const fixture = TestBed.createComponent(JobExecutionHistoryCardComponent);
    fixture.componentRef.setInput('historyItem', item);
    if (canAbandon !== undefined) {
        fixture.componentRef.setInput('canAbandon', canAbandon);
    }
    await fixture.whenStable();
    return fixture;
}

function abandonButton(fixture: {nativeElement: HTMLElement}): HTMLButtonElement | undefined {
    const buttons = Array.from(fixture.nativeElement.querySelectorAll('button'));
    return buttons.find(b => b.textContent?.trim() === 'Abandon');
}

describe('JobExecutionHistoryCardComponent abandon button', () => {

    it('is shown for a RUNNING execution when canAbandon is true', async () => {
        const fixture = await render(makeItem({status: JobExecutionStatus.RUNNING}), true);
        expect(abandonButton(fixture)).toBeDefined();
    });

    it('is hidden when canAbandon is false', async () => {
        const fixture = await render(makeItem({status: JobExecutionStatus.RUNNING}), false);
        expect(abandonButton(fixture)).toBeUndefined();
    });

    it('is hidden by default', async () => {
        const fixture = await render(makeItem({status: JobExecutionStatus.RUNNING}));
        expect(abandonButton(fixture)).toBeUndefined();
    });

    it.each([JobExecutionStatus.SUCCESS, JobExecutionStatus.FAILED, JobExecutionStatus.ABANDONED])('is hidden for a %s execution', async (status) => {
        const fixture = await render(makeItem({status, endTimestamp: '2026-01-01T00:00:10Z'}), true);
        expect(abandonButton(fixture)).toBeUndefined();
    });

    it('emits the execution id when clicked', async () => {
        const fixture = await render(makeItem({status: JobExecutionStatus.RUNNING}), true);
        const emitted: string[] = [];
        fixture.componentInstance.abandonExecution.subscribe((id: string) => emitted.push(id));
        abandonButton(fixture)!.click();
        expect(emitted).toEqual(['exec-1']);
    });

});

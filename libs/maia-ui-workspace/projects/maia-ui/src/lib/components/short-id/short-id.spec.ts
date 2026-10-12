import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ShortId} from './short-id';

describe('ShortId', () => {
  const uuid = '123e4567-e89b-12d3-a456-426614174000';
  let fixture: ComponentFixture<ShortId>;

  beforeEach(async () => {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({imports: [ShortId]}).compileComponents();
    fixture = TestBed.createComponent(ShortId);
    fixture.componentRef.setInput('id', uuid);
    fixture.detectChanges();
  });

  afterEach(() => vi.useRealTimers());

  const el = () => fixture.nativeElement as HTMLElement;

  it('shows the last 12 characters', () => {
    expect(el().querySelector('.short-id-text')?.textContent?.trim()).toBe('426614174000');
  });

  it('shows the copy icon only after 700ms of hover', () => {
    const host = el().querySelector('.short-id') as HTMLElement;
    host.dispatchEvent(new Event('mouseenter'));
    vi.advanceTimersByTime(699);
    fixture.detectChanges();
    expect(el().querySelector('button')).toBeNull();
    vi.advanceTimersByTime(1);
    fixture.detectChanges();
    expect(el().querySelector('button')).not.toBeNull();
    host.dispatchEvent(new Event('mouseleave'));
    fixture.detectChanges();
    expect(el().querySelector('button')).toBeNull();
  });

  it('copies the full id', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined);
    Object.defineProperty(navigator, 'clipboard', {value: {writeText}, configurable: true});
    (el().querySelector('.short-id') as HTMLElement).dispatchEvent(new Event('mouseenter'));
    vi.advanceTimersByTime(700);
    fixture.detectChanges();
    (el().querySelector('button') as HTMLElement).click();
    expect(writeText).toHaveBeenCalledWith(uuid);
  });
});

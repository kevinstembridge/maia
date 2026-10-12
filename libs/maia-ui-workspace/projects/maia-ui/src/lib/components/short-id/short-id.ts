import {ChangeDetectionStrategy, Component, computed, input, OnDestroy, signal} from '@angular/core';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';

const TOOLTIP_DELAY_MS = 800;
const COPY_ICON_DELAY_MS = 700;
const COPIED_FEEDBACK_MS = 1500;
const VISIBLE_CHARS = 12;

/**
 * Displays the last 12 characters of an identifier (typically a UUID). After 800ms of hover the full
 * value is shown in a tooltip; after 700ms of hover a copy-to-clipboard icon appears.
 */
@Component({
  selector: 'maia-short-id',
  standalone: true,
  imports: [MatTooltip, MatIcon, MatIconButton],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './short-id.scss',
  templateUrl: './short-id.html'
})
export class ShortId implements OnDestroy {

  id = input.required<string | null | undefined>();

  protected readonly shortId = computed(() => (this.id() ?? '').slice(-VISIBLE_CHARS));
  protected readonly tooltipDelay = TOOLTIP_DELAY_MS;
  protected readonly showCopy = signal(false);
  protected readonly copied = signal(false);

  private showCopyTimer?: ReturnType<typeof setTimeout>;
  private copiedTimer?: ReturnType<typeof setTimeout>;

  protected onMouseEnter(): void {
    clearTimeout(this.showCopyTimer);
    this.showCopyTimer = setTimeout(() => this.showCopy.set(true), COPY_ICON_DELAY_MS);
  }

  protected onMouseLeave(): void {
    clearTimeout(this.showCopyTimer);
    this.showCopy.set(false);
  }

  protected async copy(event: Event): Promise<void> {
    event.stopPropagation();
    const id = this.id();
    if (!id) {
      return;
    }
    try {
      await navigator.clipboard.writeText(id);
    } catch {
      return;
    }
    this.copied.set(true);
    clearTimeout(this.copiedTimer);
    this.copiedTimer = setTimeout(() => this.copied.set(false), COPIED_FEEDBACK_MS);
  }

  ngOnDestroy(): void {
    clearTimeout(this.showCopyTimer);
    clearTimeout(this.copiedTimer);
  }
}

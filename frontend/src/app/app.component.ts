import { Component, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DenominationService, DenominationResultDto } from './denomination.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Euro Stückelung';

  private service = inject(DenominationService);

  amount = signal<string>('');
  useBackend = signal<boolean>(true);
  calculateDifference = signal<boolean>(true);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  result = signal<DenominationResultDto | null>(null);

  // Sorted denominations (descending by value)
  sortedDenominations = computed(() => {
    const res = this.result();
    if (!res) return [] as Array<[number, number]>;
    return Object.entries(res.denominations)
      .map(([k, v]) => [Number(k), v] as [number, number])
      .sort((a, b) => b[0] - a[0]);
  });

  // Sorted difference rows
  sortedDifferences = computed(() => {
    const res = this.result();
    if (!res?.differenceMap) return [];

    return Object.entries(res.differenceMap)
      .map(([denom, diff]) => [Number(denom), diff] as [number, number])
      .sort((a, b) => b[0] - a[0]);
  });

  toEuro(amountInCents: string | number): string {
    if (amountInCents === '-' || amountInCents === null || amountInCents === undefined) return '-';
    const n = typeof amountInCents === 'string' ? Number(amountInCents) : amountInCents;
    if (!Number.isFinite(n)) return '-';
    return (n / 100).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  onCalculate(): void {
    this.error.set(null);
    this.loading.set(true);
    const obs = this.useBackend()
      ? this.service.calculateViaBackend(this.amount(), this.calculateDifference())
      : this.service.calculateInFrontend(this.amount(), this.calculateDifference());
    obs.subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = typeof err?.message === 'string' ? err.message : 'Fehler bei der Berechnung';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }
}

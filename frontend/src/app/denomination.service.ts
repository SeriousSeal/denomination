import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';

export interface DenominationResultDto {
    amountInCents: number;
    denominations: Record<string, number>;
    previousCalculation: string;
    differenceMap: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class DenominationService {
    // Euro-Denominationen in Cent (von größter zu kleinster)
    private readonly DENOMINATIONS: number[] = [
        20000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1
    ];

    private lastCalculation: { amountInCents: number; denominations: Record<string, number> } | null = null;

    constructor(private http: HttpClient) { }

    calculateViaBackend(amount: string, calculateDifference: boolean): Observable<DenominationResultDto> {
        const params = new HttpParams().set('calculateDifference', String(calculateDifference));
        return this.http.post<DenominationResultDto>(
            '/api/denomination/calculate',
            { amount },
            { params }
        );
    }

    calculateInFrontend(amount: string, calculateDifference: boolean): Observable<DenominationResultDto> {
        try {
            const cents = this.parseAmountToCents(amount);

            const denominations: Record<string, number> = {};
            let remaining = cents;
            for (const d of this.DENOMINATIONS) {
                const count = Math.floor(remaining / d);
                if (count > 0) {
                    denominations[String(d)] = count;
                    remaining -= count * d;
                }
            }

            const result: DenominationResultDto = {
                amountInCents: cents,
                denominations,
                previousCalculation: '-',
                differenceMap: {}
            };

            if (calculateDifference && this.lastCalculation) {
                result.previousCalculation = String(this.lastCalculation.amountInCents);
                result.differenceMap = this.calculateDifferenceMap(denominations, this.lastCalculation.denominations);
            }

            this.lastCalculation = { amountInCents: cents, denominations: { ...denominations } };
            return of(result);
        } catch (e: any) {
            return throwError(() => (e instanceof Error ? e : new Error('Invalid amount')));
        }
    }

    private calculateDifferenceMap(current: Record<string, number>, previous: Record<string, number>): Record<string, number> {
        if (!previous || Object.keys(previous).length === 0) {
            return {};
        }

        const differenceMap: Record<string, number> = {};

        for (const d of this.DENOMINATIONS) {
            const key = String(d);
            const cur = current[key] ?? 0;
            const prev = previous[key] ?? 0;

            // Nur hinzufügen wenn mindestens einer der Werte nicht 0 ist
            if (cur !== 0 || prev !== 0) {
                differenceMap[key] = cur - prev;
            }
        }

        return differenceMap;
    }

    parseAmountToCents(amount: string): number {
        if (!amount || amount.trim().length === 0) {
            throw new Error('Amount cannot be empty');
        }
        let clean = amount.trim();
        if (!/^\d+([.,]\d{1,2})?$/.test(clean)) {
            throw new Error("Invalid amount format. Use format like '123,45' or '123.45'");
        }
        clean = clean.replace(',', '.');
        const parts = clean.split('.');
        if (!parts[0]) {
            throw new Error('Euro part cannot be empty');
        }
        const euros = Number(parts[0]);
        if (!Number.isInteger(euros)) {
            throw new Error('Invalid number format');
        }
        let cents = 0;
        if (parts.length > 1) {
            let centsStr = parts[1];
            if (centsStr.length > 2) {
                throw new Error('Maximum 2 decimal places allowed');
            }
            if (centsStr.length === 1) {
                centsStr = centsStr + '0';
            }
            cents = Number(centsStr);
            if (!Number.isInteger(cents)) {
                throw new Error('Invalid number format');
            }
        }
        const total = euros * 100 + cents;
        if (total < 0) {
            throw new Error('Amount cannot be negative');
        }
        if (total > 99999999) {
            throw new Error('Maximum amount is 999999,99€');
        }
        return total;
    }
}




import type { GradeSummary } from '../types';

export const TOP_GRADE = 6;
export const FAIL_GRADE = 2;
const EXCELLENT_THRESHOLD = 5.5;
const GOOD_THRESHOLD = 4.5;

export function average(values: number[]): number {
  return values.reduce((sum, v) => sum + v, 0) / values.length;
}

export function tierColor(avg: number): string {
  if (avg >= EXCELLENT_THRESHOLD) return 'var(--success)';
  if (avg >= GOOD_THRESHOLD) return 'var(--primary)';
  return 'var(--error)';
}

export type SessionType = 'regular' | 'retake';

// No real "session type" field in the data yet: only one grade per
// semester+subject can be the regular session — whichever was recorded
// first — and every later grade for that same semester+subject is a retake
// ("поправителна сесия"), regardless of its value.
export function classifySessionTypes(grades: GradeSummary[]): Map<number, SessionType> {
  const byGroup = new Map<string, GradeSummary[]>();
  for (const g of grades) {
    const key = `${g.semester}::${g.subject}`;
    const bucket = byGroup.get(key);
    if (bucket) bucket.push(g);
    else byGroup.set(key, [g]);
  }
  const result = new Map<number, SessionType>();
  for (const bucket of byGroup.values()) {
    [...bucket]
      .sort((a, b) => a.createdAt.localeCompare(b.createdAt))
      .forEach((g, index) => result.set(g.id, index === 0 ? 'regular' : 'retake'));
  }
  return result;
}

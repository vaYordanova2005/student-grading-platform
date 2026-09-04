import type { CalendarEventSummary } from '../types';

export function toDateKey(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function parseDateKey(key: string): Date {
  const [y, m, d] = key.split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function formatDate(key: string): string {
  return parseDateKey(key).toLocaleDateString('bg-BG', { day: 'numeric', month: 'long', year: 'numeric' });
}

export function formatDateShort(key: string): string {
  return parseDateKey(key).toLocaleDateString('bg-BG');
}

// Always 6 weeks so the grid height stays constant across months; leading/
// trailing days from adjacent months are included and rendered dimmed.
export function buildMonthGrid(year: number, month: number): Date[][] {
  const firstOfMonth = new Date(year, month, 1);
  const firstWeekday = (firstOfMonth.getDay() + 6) % 7; // Monday = 0
  const gridStart = new Date(year, month, 1 - firstWeekday);

  const weeks: Date[][] = [];
  const cursor = new Date(gridStart);
  for (let w = 0; w < 6; w++) {
    const week: Date[] = [];
    for (let d = 0; d < 7; d++) {
      week.push(new Date(cursor));
      cursor.setDate(cursor.getDate() + 1);
    }
    weeks.push(week);
  }
  return weeks;
}

export function eventsOnDate(events: CalendarEventSummary[], dateKey: string): CalendarEventSummary[] {
  return events.filter((e) => dateKey >= e.startDate && dateKey <= (e.endDate ?? e.startDate));
}

export const TYPE_LABELS: Record<CalendarEventSummary['type'], string> = {
  TEST: 'Тест',
  HOLIDAY: 'Ваканция',
  EVENT: 'Събитие',
};

export const MONTH_NAMES = [
  'Януари', 'Февруари', 'Март', 'Април', 'Май', 'Юни',
  'Юли', 'Август', 'Септември', 'Октомври', 'Ноември', 'Декември',
];

export const WEEKDAY_LABELS = ['Пон', 'Вт', 'Ср', 'Чет', 'Пет', 'Съб', 'Нед'];

import { useApiResource } from './useApiResource';
import type { CalendarEventSummary } from '../types';

const NO_EVENTS: CalendarEventSummary[] = [];

// Not cached: the calendar page writes as well as reads, so it always wants
// the current server state rather than whatever was fetched earlier.
export function useCalendarEvents() {
  const { data, error, loading, reload } = useApiResource<CalendarEventSummary[]>('/calendar/events');
  return { events: data ?? NO_EVENTS, error, loading, refresh: reload };
}

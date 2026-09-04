import { useCallback, useEffect, useState } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import type { CalendarEventSummary } from '../types';

export function useCalendarEvents() {
  const [events, setEvents] = useState<CalendarEventSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(() => {
    setLoading(true);
    setError(null);
    return apiClient
      .get<CalendarEventSummary[]>('/calendar/events')
      .then((response) => setEvents(response.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { events, error, loading, refresh };
}

import { useEffect, useState } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import type { GradeSummary } from '../types';

export function useStudentGrades(enabled = true) {
  const [grades, setGrades] = useState<GradeSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(enabled);

  useEffect(() => {
    if (!enabled) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    apiClient
      .get<GradeSummary[]>('/student/grades')
      .then((response) => setGrades(response.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [enabled]);

  return { grades, error, loading };
}

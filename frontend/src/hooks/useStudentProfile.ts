import { useEffect, useState } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import type { StudentProfileSummary } from '../types';

export function useStudentProfile(enabled = true) {
  const [profile, setProfile] = useState<StudentProfileSummary | null>(null);
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
      .get<StudentProfileSummary>('/student/profile')
      .then((response) => setProfile(response.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [enabled]);

  return { profile, error, loading };
}

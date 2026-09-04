import { createResourceCache } from '../api/resourceCache';
import { useApiResource } from './useApiResource';
import type { StudentProfileSummary } from '../types';

// Read by both Профил and Дневник (for the "(текущ)" semester marker).
const profileCache = createResourceCache<StudentProfileSummary>();

export function useStudentProfile(enabled = true) {
  const { data, error, loading, reload } = useApiResource<StudentProfileSummary>(
    '/student/profile',
    enabled,
    profileCache
  );
  return { profile: data, error, loading, reload };
}

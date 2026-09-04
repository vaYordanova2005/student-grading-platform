import { createResourceCache } from '../api/resourceCache';
import { useApiResource } from './useApiResource';
import type { GradeSummary } from '../types';

// Начало, Дневник and Статистики all render this same list, so without a
// shared cache every navigation between them refetches identical data.
const gradesCache = createResourceCache<GradeSummary[]>();

const NO_GRADES: GradeSummary[] = [];

export function useStudentGrades(enabled = true) {
  const { data, error, loading, reload } = useApiResource<GradeSummary[]>('/student/grades', enabled, gradesCache);
  return { grades: data ?? NO_GRADES, error, loading, reload };
}

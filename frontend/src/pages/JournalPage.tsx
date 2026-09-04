import { Fragment, useMemo, useState } from 'react';
import { Layout } from '../routes/Layout';
import { useAuth } from '../auth/useAuth';
import { useStudentGrades } from '../hooks/useStudentGrades';
import { useStudentProfile } from '../hooks/useStudentProfile';
import { byCreatedAt, classifySessionTypes, FAIL_GRADE, groupBy } from '../utils/grades';

const SEMESTERS = [1, 2, 3, 4, 5, 6, 7, 8];

export function JournalPage() {
  const { user } = useAuth();
  const { grades, error, loading } = useStudentGrades(user?.role === 'STUDENT');
  const { profile } = useStudentProfile(user?.role === 'STUDENT');
  const currentSemester = profile?.enrolledSemester ?? null;
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const sessionTypeById = useMemo(() => classifySessionTypes(grades), [grades]);

  // Grades are grouped by subject per semester so a retake sits next to its
  // regular-session grade on the same row instead of a separate row.
  const bySemester = useMemo(() => {
    const result = new Map<number, { subject: string; entries: typeof grades }[]>();
    for (const [semester, entries] of groupBy(grades, (g) => g.semester)) {
      const subjectRows = [...groupBy(entries, (g) => g.subject).entries()]
        .map(([subject, subjectEntries]) => ({
          subject,
          entries: [...subjectEntries].sort(byCreatedAt),
        }))
        .sort((a, b) => a.subject.localeCompare(b.subject));
      result.set(semester, subjectRows);
    }
    return result;
  }, [grades]);

  /**
   * The eight regular semesters, plus any semester the data actually contains
   * outside that range. The backend validates 1..8, so an out-of-range value
   * can only come from a legacy row or a direct database write — rendering the
   * fixed list alone would drop those grades from the page without a word.
   */
  const semesters = useMemo(() => {
    const unexpected = [...bySemester.keys()]
      .filter((semester) => !SEMESTERS.includes(semester))
      .sort((a, b) => a - b);
    return [...SEMESTERS, ...unexpected];
  }, [bySemester]);

  if (user?.role !== 'STUDENT') {
    return (
      <Layout title="Дневник">
        <section className="card">
          <p>Тази секция е в процес на разработка.</p>
        </section>
      </Layout>
    );
  }

  return (
    <Layout>
      {loading && (
        <section className="card">
          <p>Зареждане...</p>
        </section>
      )}
      {error && (
        <section className="card">
          <p className="error">{error}</p>
        </section>
      )}
      {!loading && !error && grades.length === 0 && (
        <section className="card">
          <p>Все още няма вписани оценки.</p>
        </section>
      )}
      {!loading && !error && grades.length > 0 && (
        <>
          {semesters.map((semester) => {
            const subjectRows = bySemester.get(semester) ?? [];
            return (
              <details className="card" key={semester}>
                <summary>Семестър {semester}{semester === currentSemester ? ' (текущ)' : ''}</summary>
                {subjectRows.length ? (
                  <table>
                    <thead>
                      <tr>
                        <th>Предмет</th>
                        <th>Оценки</th>
                      </tr>
                    </thead>
                    <tbody>
                      {subjectRows.map(({ subject, entries }) => (
                        <Fragment key={subject}>
                          <tr>
                            <td>{subject}</td>
                            <td className="grade-btn-group">
                              {entries.map((g) => (
                                <button
                                  key={g.id}
                                  type="button"
                                  className={g.grade === FAIL_GRADE ? 'grade-btn grade-btn-fail' : 'grade-btn'}
                                  aria-expanded={expandedId === g.id}
                                  aria-controls={`grade-detail-${g.id}`}
                                  onClick={() => setExpandedId(expandedId === g.id ? null : g.id)}
                                >
                                  {g.grade}
                                </button>
                              ))}
                            </td>
                          </tr>
                          {entries.map(
                            (g) =>
                              expandedId === g.id && (
                                <tr className="grade-detail-row" key={g.id}>
                                  <td colSpan={2}>
                                    <div className="grade-detail" id={`grade-detail-${g.id}`}>
                                      <div>Дата: {new Date(g.createdAt).toLocaleDateString('bg-BG')}</div>
                                      <div>
                                        Тип:{' '}
                                        {sessionTypeById.get(g.id) === 'retake'
                                          ? 'поправителна сесия'
                                          : 'редовна сесия'}
                                      </div>
                                      <div>Преподавател: {g.teacherUsername ?? '—'}</div>
                                    </div>
                                  </td>
                                </tr>
                              )
                          )}
                        </Fragment>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p>Няма оценки за този семестър.</p>
                )}
              </details>
            );
          })}
        </>
      )}
    </Layout>
  );
}

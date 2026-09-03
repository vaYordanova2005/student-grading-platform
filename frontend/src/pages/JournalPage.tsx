import { Fragment, useMemo, useState } from 'react';
import { Layout } from '../routes/Layout';
import { useAuth } from '../auth/AuthContext';
import { useStudentGrades } from '../hooks/useStudentGrades';
import { classifySessionTypes, FAIL_GRADE } from '../utils/grades';

const SEMESTERS = [1, 2, 3, 4];

export function JournalPage() {
  const { user } = useAuth();
  const { grades, error, loading } = useStudentGrades(user?.role === 'STUDENT');
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const sessionTypeById = useMemo(() => classifySessionTypes(grades), [grades]);

  // Grades are grouped by subject per semester so a retake sits next to its
  // regular-session grade on the same row instead of a separate row.
  const bySemester = useMemo(() => {
    const semesterGrades = new Map<number, typeof grades>();
    for (const semester of SEMESTERS) semesterGrades.set(semester, []);
    for (const g of grades) {
      const bucket = semesterGrades.get(g.semester);
      if (bucket) bucket.push(g);
      else semesterGrades.set(g.semester, [g]);
    }

    const result = new Map<number, { subject: string; entries: typeof grades }[]>();
    for (const [semester, entries] of semesterGrades) {
      const bySubject = new Map<string, typeof grades>();
      for (const g of entries) {
        const bucket = bySubject.get(g.subject);
        if (bucket) bucket.push(g);
        else bySubject.set(g.subject, [g]);
      }
      const subjectRows = [...bySubject.entries()]
        .map(([subject, subjectEntries]) => ({
          subject,
          entries: [...subjectEntries].sort((a, b) => a.createdAt.localeCompare(b.createdAt)),
        }))
        .sort((a, b) => a.subject.localeCompare(b.subject));
      result.set(semester, subjectRows);
    }
    return result;
  }, [grades]);

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
          {SEMESTERS.map((semester) => {
            const subjectRows = bySemester.get(semester) ?? [];
            return (
              <details className="card" key={semester}>
                <summary>Семестър {semester}</summary>
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
                                    <div className="grade-detail">
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

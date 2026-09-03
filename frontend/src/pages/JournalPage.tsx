import { Fragment, useMemo, useState } from 'react';
import { Layout } from '../routes/Layout';
import { useAuth } from '../auth/AuthContext';
import { useStudentGrades } from '../hooks/useStudentGrades';

const SEMESTERS = [1, 2, 3, 4];

export function JournalPage() {
  const { user } = useAuth();
  const { grades, error, loading } = useStudentGrades(user?.role === 'STUDENT');
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const bySemester = useMemo(() => {
    const map = new Map<number, typeof grades>();
    for (const semester of SEMESTERS) map.set(semester, []);
    for (const g of grades) {
      const bucket = map.get(g.semester);
      if (bucket) bucket.push(g);
      else map.set(g.semester, [g]);
    }
    for (const bucket of map.values()) bucket.sort((a, b) => a.subject.localeCompare(b.subject));
    return map;
  }, [grades]);

  // No real "session type" field in the data yet: within the same
  // semester+subject, the earliest grade is treated as the regular-session
  // grade and any later one(s) as a retake ("поправителна сесия").
  const sessionTypeById = useMemo(() => {
    const byGroup = new Map<string, typeof grades>();
    for (const g of grades) {
      const key = `${g.semester}::${g.subject}`;
      const bucket = byGroup.get(key);
      if (bucket) bucket.push(g);
      else byGroup.set(key, [g]);
    }
    const result = new Map<number, 'regular' | 'retake'>();
    for (const bucket of byGroup.values()) {
      [...bucket]
        .sort((a, b) => a.createdAt.localeCompare(b.createdAt))
        .forEach((g, index) => result.set(g.id, index === 0 ? 'regular' : 'retake'));
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
          {SEMESTERS.map((semester) => (
            <details className="card" key={semester}>
              <summary>Семестър {semester}</summary>
              {bySemester.get(semester)?.length ? (
                <table>
                  <thead>
                    <tr>
                      <th>Предмет</th>
                      <th>Оценка</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bySemester.get(semester)!.map((g) => (
                      <Fragment key={g.id}>
                        <tr>
                          <td>{g.subject}</td>
                          <td>
                            <button
                              type="button"
                              className="grade-btn"
                              onClick={() => setExpandedId(expandedId === g.id ? null : g.id)}
                            >
                              {g.grade}
                            </button>
                          </td>
                        </tr>
                        {expandedId === g.id && (
                          <tr className="grade-detail-row">
                            <td colSpan={2}>
                              <div className="grade-detail">
                                <div>Дата: {new Date(g.createdAt).toLocaleDateString('bg-BG')}</div>
                                <div>
                                  Тип:{' '}
                                  {sessionTypeById.get(g.id) === 'retake' ? 'поправителна сесия' : 'редовна сесия'}
                                </div>
                                <div>Преподавател: {g.teacherUsername ?? '—'}</div>
                              </div>
                            </td>
                          </tr>
                        )}
                      </Fragment>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p>Няма оценки за този семестър.</p>
              )}
            </details>
          ))}
        </>
      )}
    </Layout>
  );
}

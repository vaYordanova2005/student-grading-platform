import { useMemo } from 'react';
import { Layout } from '../routes/Layout';
import { useAuth } from '../auth/AuthContext';
import { useStudentGrades } from '../hooks/useStudentGrades';

export function JournalPage() {
  const { user } = useAuth();
  const { grades, error, loading } = useStudentGrades(user?.role === 'STUDENT');

  const chronological = useMemo(
    () => [...grades].sort((a, b) => a.createdAt.localeCompare(b.createdAt)),
    [grades]
  );

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
    <Layout title="Дневник">
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
        <section className="card">
          <h2>Всички оценки</h2>
          <table>
            <thead>
              <tr>
                <th>Дата</th>
                <th>Семестър</th>
                <th>Предмет</th>
                <th>Оценка</th>
              </tr>
            </thead>
            <tbody>
              {chronological.map((g) => (
                <tr key={g.id}>
                  <td>{new Date(g.createdAt).toLocaleDateString('bg-BG')}</td>
                  <td>{g.semester}</td>
                  <td>{g.subject}</td>
                  <td>{g.grade}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </Layout>
  );
}

import { useEffect, useState } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import { Layout } from '../routes/Layout';
import { useAuth } from '../auth/AuthContext';
import type { GradeSummary } from '../types';

export function JournalPage() {
  const { user } = useAuth();
  const [grades, setGrades] = useState<GradeSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.role !== 'STUDENT') {
      setLoading(false);
      return;
    }
    apiClient
      .get<GradeSummary[]>('/student/grades')
      .then((response) => setGrades(response.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [user?.role]);

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
                <th>Семестър</th>
                <th>Предмет</th>
                <th>Оценка</th>
              </tr>
            </thead>
            <tbody>
              {grades.map((g) => (
                <tr key={g.id}>
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

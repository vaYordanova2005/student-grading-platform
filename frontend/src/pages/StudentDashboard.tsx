import { useEffect, useState } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import { Layout } from '../routes/Layout';
import type { GradeSummary } from '../types';

export function StudentDashboard() {
  const [grades, setGrades] = useState<GradeSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient
      .get<GradeSummary[]>('/student/grades')
      .then((response) => setGrades(response.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout title="Моите оценки">
      <section className="card">
        {loading && <p>Зареждане...</p>}
        {error && <p className="error">{error}</p>}
        {!loading && !error && grades.length === 0 && <p>Все още няма вписани оценки.</p>}
        {!loading && grades.length > 0 && (
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
        )}
      </section>
    </Layout>
  );
}

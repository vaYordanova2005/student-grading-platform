import { useEffect, useMemo, useState, type CSSProperties } from 'react';
import { Link } from 'react-router-dom';
import apiClient, { extractErrorMessage } from '../api/client';
import { Layout } from '../routes/Layout';
import type { GradeSummary } from '../types';
import { ChartIcon, JournalIcon, TrophyIcon, BooksIcon } from '../components/icons';
import { NetworkField } from '../components/NetworkField';

const EXCELLENT_THRESHOLD = 5.5;
const GOOD_THRESHOLD = 4.5;

function tierColor(avg: number): string {
  if (avg >= EXCELLENT_THRESHOLD) return 'var(--success)';
  if (avg >= GOOD_THRESHOLD) return 'var(--primary)';
  return 'var(--error)';
}

function average(values: number[]): number {
  return values.reduce((sum, v) => sum + v, 0) / values.length;
}

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

  const stats = useMemo(() => {
    if (grades.length === 0) return null;
    const overallAvg = average(grades.map((g) => g.grade));
    const excellentCount = grades.filter((g) => g.grade >= EXCELLENT_THRESHOLD).length;
    const subjects = new Set(grades.map((g) => g.subject));

    const bySubject = new Map<string, number[]>();
    for (const g of grades) {
      bySubject.set(g.subject, [...(bySubject.get(g.subject) ?? []), g.grade]);
    }
    const subjectAverages = [...bySubject.entries()]
      .map(([subject, values]) => ({ subject, avg: average(values), count: values.length }))
      .sort((a, b) => b.avg - a.avg);

    const bySemester = new Map<number, number[]>();
    for (const g of grades) {
      bySemester.set(g.semester, [...(bySemester.get(g.semester) ?? []), g.grade]);
    }
    const semesterAverages = [...bySemester.entries()]
      .map(([semester, values]) => ({ semester, avg: average(values) }))
      .sort((a, b) => a.semester - b.semester);

    return { overallAvg, excellentCount, subjectCount: subjects.size, subjectAverages, semesterAverages };
  }, [grades]);

  return (
    <Layout>
      <NetworkField
        className="home-network-bg"
        intensity={1.9}
        minNodes={90}
        maxNodes={220}
        areaPerNode={3200}
        linkDist={85}
        maxPulses={40}
      />
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

      {!loading && stats && (
        <>
          <div className="stat-strip">
            <Link
              to="/journal"
              className="stat-tile"
              style={{ '--tile-accent': tierColor(stats.overallAvg) } as CSSProperties}
            >
              <ChartIcon />
              <div>
                <strong>{stats.overallAvg.toFixed(2)}</strong>
                <span>Успех</span>
              </div>
            </Link>
            <Link to="/journal" className="stat-tile">
              <JournalIcon />
              <div>
                <strong>{grades.length}</strong>
                <span>Оценки</span>
              </div>
            </Link>
            <div className="stat-tile" style={{ '--tile-accent': 'var(--success)' } as CSSProperties}>
              <TrophyIcon />
              <div>
                <strong>{stats.excellentCount}</strong>
                <span>Отлични</span>
              </div>
            </div>
            <div className="stat-tile">
              <BooksIcon />
              <div>
                <strong>{stats.subjectCount}</strong>
                <span>Предмети</span>
              </div>
            </div>
          </div>

          <section className="card">
            <h2>Успех по предмети</h2>
            <div className="subject-bars">
              {stats.subjectAverages.map(({ subject, avg, count }) => (
                <div className="subject-bar-row" key={subject}>
                  <span className="subject-bar-label">{subject}</span>
                  <div className="bar-track">
                    <div
                      className="bar-fill"
                      style={{ width: `${(avg / 6) * 100}%`, background: tierColor(avg) }}
                    />
                  </div>
                  <span className="subject-bar-value">
                    {avg.toFixed(2)} <small>({count})</small>
                  </span>
                </div>
              ))}
            </div>
          </section>

          {stats.semesterAverages.length > 1 && (
            <section className="card">
              <h2>Развитие по семестри</h2>
              <div className="semester-trend">
                {stats.semesterAverages.map(({ semester, avg }) => (
                  <div className="semester-pill" key={semester}>
                    <span className="semester-pill-label">Сем. {semester}</span>
                    <span className="semester-pill-value" style={{ color: tierColor(avg) }}>
                      {avg.toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
            </section>
          )}
        </>
      )}
    </Layout>
  );
}

import { useState, type FormEvent } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import { Layout } from '../routes/Layout';

export function TeacherDashboard() {
  const [studentUsername, setStudentUsername] = useState('');
  const [subject, setSubject] = useState('');
  const [semester, setSemester] = useState(1);
  const [grade, setGrade] = useState(6);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      await apiClient.post('/teacher/grades', { studentUsername, subject, semester, grade });
      setSuccess(`Оценка ${grade} по ${subject} записана за ${studentUsername}`);
      setSubject('');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout title="Учител">
      <section className="card">
        <h2>Добави оценка</h2>
        <form onSubmit={handleSubmit} className="inline-form">
          <label>
            Факултетен номер
            <input
              value={studentUsername}
              onChange={(e) => setStudentUsername(e.target.value)}
              required
            />
          </label>
          <label>
            Предмет
            <input value={subject} onChange={(e) => setSubject(e.target.value)} required />
          </label>
          <label>
            Семестър
            <input
              type="number"
              min={1}
              value={semester}
              onChange={(e) => setSemester(Number(e.target.value))}
              required
            />
          </label>
          <label>
            Оценка
            <input
              type="number"
              min={2}
              max={6}
              value={grade}
              onChange={(e) => setGrade(Number(e.target.value))}
              required
            />
          </label>
          <button type="submit" disabled={submitting}>
            {submitting ? 'Записване...' : 'Запиши'}
          </button>
        </form>
        {error && <p className="error">{error}</p>}
        {success && <p className="success">{success}</p>}
      </section>
    </Layout>
  );
}

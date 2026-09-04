import { useAuth } from '../auth/AuthContext';
import { Layout } from '../routes/Layout';
import { useStudentProfile } from '../hooks/useStudentProfile';
import type { StudentProfileSummary } from '../types';

const PROFILE_FIELDS: { key: keyof StudentProfileSummary; label: string }[] = [
  { key: 'degreeLevel', label: 'ОКС' },
  { key: 'facultyNumber', label: 'Фак. номер' },
  { key: 'faculty', label: 'Факултет' },
  { key: 'specialty', label: 'Специалност' },
  { key: 'studyMode', label: 'Вид обучение' },
  { key: 'specialization', label: 'Специализация' },
  { key: 'groupNumber', label: 'Група' },
  { key: 'admissionType', label: 'Вид прием' },
  { key: 'status', label: 'Състояние' },
  { key: 'enrolledSemester', label: 'Записан семестър' },
  { key: 'completedSemester', label: 'Заверен семестър' },
  { key: 'stream', label: 'Поток' },
  { key: 'studentUsername', label: 'Имейл' },
];

export function ProfilePage() {
  const { user } = useAuth();
  const { profile, error, loading } = useStudentProfile(user?.role === 'STUDENT');

  return (
    <Layout>
      {user?.role === 'STUDENT' && (
        <section className="card">
          <h2>Информация за студента</h2>
          {loading && <p>Зареждане...</p>}
          {error && <p className="error">{error}</p>}
          {!loading && !error && profile && (
            <div className="profile-info-grid">
              {PROFILE_FIELDS.map(({ key, label }) => (
                <div className="profile-info-row" key={key}>
                  <span className="profile-info-label">{label}</span>
                  <span className="profile-info-value">{profile[key] ?? '—'}</span>
                </div>
              ))}
            </div>
          )}
        </section>
      )}

      {user?.role !== 'STUDENT' && (
        <section className="card">
          <p>{user?.username}</p>
        </section>
      )}
    </Layout>
  );
}

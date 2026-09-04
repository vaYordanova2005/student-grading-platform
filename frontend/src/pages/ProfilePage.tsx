import { useAuth } from '../auth/AuthContext';
import { Layout } from '../routes/Layout';
import { useStudentProfile } from '../hooks/useStudentProfile';
import type { StudentProfileSummary } from '../types';

const roleLabels: Record<string, string> = {
  ADMIN: 'Администратор',
  TEACHER: 'Учител',
  STUDENT: 'Ученик',
};

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
  { key: 'personalEmail', label: 'Личен e-mail' },
];

export function ProfilePage() {
  const { user } = useAuth();
  const { profile, error, loading } = useStudentProfile(user?.role === 'STUDENT');

  return (
    <Layout>
      <section className="card">
        <p>
          <strong>Потребителско име:</strong> {user?.username}
        </p>
        <p>
          <strong>Роля:</strong> {user?.role ? (roleLabels[user.role] ?? user.role) : ''}
        </p>
      </section>

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
    </Layout>
  );
}

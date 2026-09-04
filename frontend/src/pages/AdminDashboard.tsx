import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import { Layout } from '../routes/Layout';
import type { Role, StudentProfileSummary, UserSummary } from '../types';

type ProfileFormState = {
  degreeLevel: string;
  facultyNumber: string;
  faculty: string;
  specialty: string;
  studyMode: string;
  specialization: string;
  groupNumber: string;
  admissionType: string;
  status: string;
  enrolledSemester: string;
  completedSemester: string;
  stream: string;
  personalEmail: string;
};

const PROFILE_FIELD_LABELS: { key: keyof ProfileFormState; label: string; type?: string }[] = [
  { key: 'degreeLevel', label: 'ОКС' },
  { key: 'facultyNumber', label: 'Фак. номер' },
  { key: 'faculty', label: 'Факултет' },
  { key: 'specialty', label: 'Специалност' },
  { key: 'studyMode', label: 'Вид обучение' },
  { key: 'specialization', label: 'Специализация' },
  { key: 'groupNumber', label: 'Група' },
  { key: 'admissionType', label: 'Вид прием' },
  { key: 'status', label: 'Състояние' },
  { key: 'enrolledSemester', label: 'Записан семестър', type: 'number' },
  { key: 'completedSemester', label: 'Заверен семестър', type: 'number' },
  { key: 'stream', label: 'Поток' },
  { key: 'personalEmail', label: 'Личен e-mail' },
];

function toFormState(profile: StudentProfileSummary): ProfileFormState {
  return {
    degreeLevel: profile.degreeLevel ?? '',
    facultyNumber: profile.facultyNumber ?? '',
    faculty: profile.faculty ?? '',
    specialty: profile.specialty ?? '',
    studyMode: profile.studyMode ?? '',
    specialization: profile.specialization ?? '',
    groupNumber: profile.groupNumber ?? '',
    admissionType: profile.admissionType ?? '',
    status: profile.status ?? '',
    enrolledSemester: profile.enrolledSemester != null ? String(profile.enrolledSemester) : '',
    completedSemester: profile.completedSemester != null ? String(profile.completedSemester) : '',
    stream: profile.stream ?? '',
    personalEmail: profile.personalEmail ?? '',
  };
}

export function AdminDashboard() {
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [role, setRole] = useState<Role>('STUDENT');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [profileUsername, setProfileUsername] = useState('');
  const [profileForm, setProfileForm] = useState<ProfileFormState | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSuccess, setProfileSuccess] = useState<string | null>(null);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get<UserSummary[]>('/admin/users');
      setUsers(response.data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await apiClient.post('/admin/users', { role, username, password });
      setUsername('');
      setPassword('');
      await loadUsers();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleLoadProfile = async (event: FormEvent) => {
    event.preventDefault();
    setProfileError(null);
    setProfileSuccess(null);
    setProfileForm(null);
    setProfileLoading(true);
    try {
      const response = await apiClient.get<StudentProfileSummary>('/admin/students/profile', {
        params: { username: profileUsername },
      });
      setProfileForm(toFormState(response.data));
    } catch (err) {
      setProfileError(extractErrorMessage(err));
    } finally {
      setProfileLoading(false);
    }
  };

  const updateProfileField = (field: keyof ProfileFormState) => (event: ChangeEvent<HTMLInputElement>) => {
    setProfileForm((prev) => (prev ? { ...prev, [field]: event.target.value } : prev));
  };

  const handleSaveProfile = async (event: FormEvent) => {
    event.preventDefault();
    if (!profileForm) return;
    setProfileError(null);
    setProfileSuccess(null);
    setProfileSaving(true);
    try {
      await apiClient.put('/admin/students/profile', {
        studentUsername: profileUsername,
        degreeLevel: profileForm.degreeLevel || null,
        facultyNumber: profileForm.facultyNumber || null,
        faculty: profileForm.faculty || null,
        specialty: profileForm.specialty || null,
        studyMode: profileForm.studyMode || null,
        specialization: profileForm.specialization || null,
        groupNumber: profileForm.groupNumber || null,
        admissionType: profileForm.admissionType || null,
        status: profileForm.status || null,
        enrolledSemester: profileForm.enrolledSemester ? Number(profileForm.enrolledSemester) : null,
        completedSemester: profileForm.completedSemester ? Number(profileForm.completedSemester) : null,
        stream: profileForm.stream || null,
        personalEmail: profileForm.personalEmail || null,
      });
      setProfileSuccess('Профилът е записан');
    } catch (err) {
      setProfileError(extractErrorMessage(err));
    } finally {
      setProfileSaving(false);
    }
  };

  return (
    <Layout title="Admin">
      <section className="card">
        <h2>Нов потребител</h2>
        <form onSubmit={handleSubmit} className="inline-form">
          <label>
            Роля
            <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
              <option value="STUDENT">Ученик</option>
              <option value="TEACHER">Учител</option>
            </select>
          </label>
          <label>
            {role === 'TEACHER' ? 'Имейл (@uni-sofia.bg)' : 'Имейл'}
            <input
              type="email"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </label>
          <label>
            Парола (мин. 5 символа)
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={submitting}>
            {submitting ? 'Създаване...' : 'Създай'}
          </button>
        </form>
        {error && <p className="error">{error}</p>}
      </section>

      <section className="card">
        <h2>Профил на ученик</h2>
        <form onSubmit={handleLoadProfile} className="inline-form">
          <label>
            Имейл на ученика
            <input
              type="email"
              value={profileUsername}
              onChange={(e) => setProfileUsername(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={profileLoading}>
            {profileLoading ? 'Зареждане...' : 'Зареди'}
          </button>
        </form>
        {profileError && <p className="error">{profileError}</p>}

        {profileForm && (
          <form onSubmit={handleSaveProfile} className="profile-edit-grid">
            {PROFILE_FIELD_LABELS.map(({ key, label, type }) => (
              <label key={key}>
                {label}
                <input type={type ?? 'text'} value={profileForm[key]} onChange={updateProfileField(key)} />
              </label>
            ))}
            <button type="submit" disabled={profileSaving}>
              {profileSaving ? 'Записване...' : 'Запази'}
            </button>
          </form>
        )}
        {profileSuccess && <p className="success">{profileSuccess}</p>}
      </section>

      <section className="card">
        <h2>Потребители</h2>
        {loading ? (
          <p>Зареждане...</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Потребителско име</th>
                <th>Роля</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.username}</td>
                  <td>{u.role}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </Layout>
  );
}

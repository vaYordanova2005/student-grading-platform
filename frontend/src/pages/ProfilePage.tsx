import { useState, type FormEvent } from 'react';
import { extractErrorMessage } from '../api/client';
import { useAuth } from '../auth/useAuth';
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

/**
 * A field the admin saved as an empty string is just as absent as a `null`
 * one — `?? '—'` only catches the latter and would leave a blank cell.
 */
function displayValue(value: string | number | null | undefined): string | number {
  return value === null || value === undefined || value === '' ? '—' : value;
}

export function ProfilePage() {
  const { user, changePassword } = useAuth();
  const { profile, error, loading } = useStudentProfile(user?.role === 'STUDENT');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [repeatedPassword, setRepeatedPassword] = useState('');
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);
  const [savingPassword, setSavingPassword] = useState(false);

  const handleChangePassword = async (event: FormEvent) => {
    event.preventDefault();
    setPasswordError(null);
    setPasswordSuccess(null);
    // Checked here as well as on the server: the repeat field only exists to
    // catch a typo, so there is no reason to spend a request on it.
    if (newPassword !== repeatedPassword) {
      setPasswordError('Двете нови пароли не съвпадат');
      return;
    }
    setSavingPassword(true);
    try {
      await changePassword(currentPassword, newPassword);
      setCurrentPassword('');
      setNewPassword('');
      setRepeatedPassword('');
      setPasswordSuccess('Паролата е сменена. Останалите ви сесии са прекратени.');
    } catch (err) {
      setPasswordError(extractErrorMessage(err));
    } finally {
      setSavingPassword(false);
    }
  };

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
                  <span className="profile-info-value">{displayValue(profile[key])}</span>
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

      <section className="card">
        <h2>Смяна на парола</h2>
        <form className="password-form" onSubmit={handleChangePassword}>
          <label>
            Текуща парола
            <input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
          <label>
            Нова парола
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </label>
          <label>
            Повторете новата парола
            <input
              type="password"
              value={repeatedPassword}
              onChange={(e) => setRepeatedPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </label>
          <p className="password-hint">
            Поне 10 символа, с главна буква, малка буква и цифра, и без потребителското ви име.
          </p>
          {passwordError && <p className="error">{passwordError}</p>}
          {passwordSuccess && <p className="success">{passwordSuccess}</p>}
          <button type="submit" disabled={savingPassword}>
            {savingPassword ? 'Записване...' : 'Смени паролата'}
          </button>
        </form>
      </section>
    </Layout>
  );
}

import { useEffect, useState, type FormEvent } from 'react';
import apiClient, { extractErrorMessage } from '../api/client';
import { Layout } from '../routes/Layout';
import type { Role, UserSummary } from '../types';

export function AdminDashboard() {
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [role, setRole] = useState<Role>('STUDENT');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const loadUsers = async () => {
    setLoading(true);
    const response = await apiClient.get<UserSummary[]>('/admin/users');
    setUsers(response.data);
    setLoading(false);
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
            {role === 'TEACHER' ? 'Имейл (@tu-sofia.bg)' : 'Факултетен номер (9 цифри)'}
            <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          </label>
          <label>
            {role === 'TEACHER' ? 'Парола (мин. 5 символа)' : 'ЕГН (10 цифри)'}
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

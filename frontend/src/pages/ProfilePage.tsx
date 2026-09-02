import { useAuth } from '../auth/AuthContext';
import { Layout } from '../routes/Layout';

const roleLabels: Record<string, string> = {
  ADMIN: 'Администратор',
  TEACHER: 'Учител',
  STUDENT: 'Ученик',
};

export function ProfilePage() {
  const { user } = useAuth();

  return (
    <Layout title="Профил">
      <section className="card">
        <p>
          <strong>Потребителско име:</strong> {user?.username}
        </p>
        <p>
          <strong>Роля:</strong> {user?.role ? (roleLabels[user.role] ?? user.role) : ''}
        </p>
      </section>
    </Layout>
  );
}

import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout({ title, children }: { title: string; children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="page">
      <header className="topbar">
        <h1>{title}</h1>
        <div className="topbar-user">
          <span>
            {user?.username} ({user?.role})
          </span>
          <button onClick={handleLogout}>Изход</button>
        </div>
      </header>
      <main>{children}</main>
    </div>
  );
}

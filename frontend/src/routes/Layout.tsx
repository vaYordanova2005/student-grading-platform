import type { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { HomeIcon, JournalIcon, ChartIcon, CalendarIcon, ProfileIcon } from '../components/icons';

const navItems = [
  { to: '/', label: 'Начало', icon: HomeIcon, end: true },
  { to: '/journal', label: 'Дневник', icon: JournalIcon },
  { to: '/statistics', label: 'Статистики', icon: ChartIcon },
  { to: '/calendar', label: 'Календар', icon: CalendarIcon },
  { to: '/profile', label: 'Профил', icon: ProfileIcon },
];

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
        <div className="topbar-top">
          <span className="brand">Markly</span>
          <div className="topbar-user">
            <span>
              {user?.username} ({user?.role})
            </span>
            <button onClick={handleLogout}>Изход</button>
          </div>
        </div>
        <nav className="topnav">
          {navItems.map(({ to, label, icon: ItemIcon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => (isActive ? 'active' : undefined)}
            >
              <ItemIcon />
              {label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main>
        <h2 className="page-title">{title}</h2>
        {children}
      </main>
    </div>
  );
}

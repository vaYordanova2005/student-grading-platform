import type { ReactNode } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { HomeIcon, JournalIcon, ChartIcon, CalendarIcon, ProfileIcon } from '../components/icons';

export function Layout({ title, children }: { title: string; children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const homePath = user ? `/${user.role.toLowerCase()}` : '/';
  const navItems = [
    { to: homePath, label: 'Начало', icon: HomeIcon, end: true },
    { to: '/journal', label: 'Дневник', icon: JournalIcon },
    { to: '/statistics', label: 'Статистики', icon: ChartIcon },
    { to: '/calendar', label: 'Календар', icon: CalendarIcon },
    { to: '/profile', label: 'Профил', icon: ProfileIcon },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="page">
      <header className="topbar">
        <Link to="/" className="brand">Markly</Link>
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
        <div className="topbar-user">
          <span>
            {user?.username} ({user?.role})
          </span>
          <button onClick={handleLogout}>Изход</button>
        </div>
      </header>
      <main>
        <h2 className="page-title">{title}</h2>
        {children}
      </main>
    </div>
  );
}

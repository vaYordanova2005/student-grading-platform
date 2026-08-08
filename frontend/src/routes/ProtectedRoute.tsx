import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import type { Role } from '../types';

export function ProtectedRoute({
  allowedRole,
  children,
}: {
  allowedRole: Role;
  children: ReactNode;
}) {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== allowedRole) return <Navigate to={`/${user.role.toLowerCase()}`} replace />;

  return <>{children}</>;
}

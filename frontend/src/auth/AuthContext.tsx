import { useMemo, useState, type ReactNode } from 'react';
import apiClient, { STORAGE_KEY } from '../api/client';
import { clearAllResourceCaches } from '../api/resourceCache';
import { AuthContext } from './useAuth';
import type { AuthUser } from '../types';

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(readStoredUser);

  const login = async (username: string, password: string) => {
    const response = await apiClient.post('/auth/login', { username, password });
    const authUser: AuthUser = {
      username: response.data.username,
      role: response.data.role,
      token: response.data.token,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(authUser));
    // Anything cached belongs to whoever was signed in before.
    clearAllResourceCaches();
    setUser(authUser);
    return authUser;
  };

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY);
    clearAllResourceCaches();
    setUser(null);
  };

  const value = useMemo(() => ({ user, login, logout }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

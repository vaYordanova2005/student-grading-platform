import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import apiClient, { setCsrfToken, setSessionExpiredHandler } from '../api/client';
import { clearAllResourceCaches } from '../api/resourceCache';
import { AuthContext } from './useAuth';
import type { AuthUser } from '../types';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  // The session lives in an httpOnly cookie this code cannot read, so on a
  // reload the only way to know whether one exists is to ask the backend.
  // Until that answers, routes must not decide the user is logged out.
  const [initializing, setInitializing] = useState(true);

  const clearSession = useCallback(() => {
    setCsrfToken(null);
    clearAllResourceCaches();
    setUser(null);
  }, []);

  useEffect(() => {
    setSessionExpiredHandler(clearSession);
    return () => setSessionExpiredHandler(null);
  }, [clearSession]);

  useEffect(() => {
    let ignore = false;
    apiClient
      .get('/auth/me')
      .then((response) => {
        if (ignore) return;
        setCsrfToken(response.data.csrfToken);
        setUser({ username: response.data.username, role: response.data.role });
      })
      .catch(() => {
        if (!ignore) setCsrfToken(null);
      })
      .finally(() => {
        if (!ignore) setInitializing(false);
      });
    return () => {
      ignore = true;
    };
  }, []);

  const login = async (username: string, password: string) => {
    const response = await apiClient.post('/auth/login', { username, password });
    const authUser: AuthUser = {
      username: response.data.username,
      role: response.data.role,
    };
    setCsrfToken(response.data.csrfToken);
    // Anything cached belongs to whoever was signed in before.
    clearAllResourceCaches();
    setUser(authUser);
    return authUser;
  };

  const logout = useCallback(async () => {
    try {
      // Bumps the account's token version server-side, so the token that was
      // just in flight cannot be replayed by anyone who captured it.
      await apiClient.post('/auth/logout');
    } catch {
      // The session is being abandoned either way.
    }
    clearSession();
  }, [clearSession]);

  const value = useMemo(() => ({ user, initializing, login, logout }), [user, initializing, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

import { createContext, useContext } from 'react';
import type { AuthUser } from '../types';

export interface AuthContextValue {
  user: AuthUser | null;
  /** True until the initial session probe against the backend has answered. */
  initializing: boolean;
  login: (username: string, password: string) => Promise<AuthUser>;
  logout: () => Promise<void>;
}

/**
 * The context and its hook live apart from {@code AuthContext.tsx} because a
 * module that exports both a component and something else opts out of React
 * Fast Refresh — editing the provider would then reload the whole app instead
 * of just that component.
 */
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}

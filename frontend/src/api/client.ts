import axios from 'axios';

const STORAGE_KEY = 'markly_auth';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
});

apiClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) {
    const { token } = JSON.parse(raw);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error)) {
      // 401 = not authenticated (missing/expired/invalid token) -> session is
      // dead, log out. 403 = authenticated but not allowed -> a legitimate
      // authorization outcome, not a reason to end the session (the backend
      // now returns a real AuthenticationEntryPoint-driven 401 for the
      // unauthenticated case, so this distinction is reliable).
      if (error.response?.status === 401 && localStorage.getItem(STORAGE_KEY)) {
        localStorage.removeItem(STORAGE_KEY);
        if (window.location.pathname !== '/login') {
          window.location.assign('/login');
        }
      }
    }
    return Promise.reject(error);
  }
);

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    if (data?.message) return data.message;
    if (error.response?.status === 401) return 'Невалидно потребителско име или парола';
    if (error.response?.status === 403) return 'Нямате достъп за това действие';
  }
  return 'Възникна грешка. Опитайте отново.';
}

export default apiClient;
export { STORAGE_KEY };

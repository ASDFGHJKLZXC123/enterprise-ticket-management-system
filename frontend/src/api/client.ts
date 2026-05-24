import axios, { AxiosError } from 'axios';

export const TOKEN_KEY = 'etms.token';

const baseURL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '');

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
      // Token expired or invalid; clear it and bounce to login.
      localStorage.removeItem(TOKEN_KEY);
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  },
);

/** Pull a human-readable message out of an Axios error for display in the UI. */
export function extractErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; error?: string } | undefined;
    return data?.message ?? data?.error ?? error.message ?? fallback;
  }
  if (error instanceof Error) return error.message;
  return fallback;
}

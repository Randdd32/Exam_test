import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';
import { getBrowserFingerprint } from '../utils/fingerprint';
import { API_BASE_URL, API_ENDPOINTS } from './constants';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
  withCredentials: true,
  paramsSerializer: (params) => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') return;
      if (Array.isArray(value)) {
        value.forEach((v) => searchParams.append(key, String(v)));
      } else {
        searchParams.append(key, String(value));
      }
    });
    return searchParams.toString();
  }
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string | null) => void; reject: (reason?: unknown) => void }> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

const translateErrorMessage = (errorCode?: string, msg?: string): string => {
  if (msg?.includes('Username already exists')) return 'Пользователь с таким логином уже существует.';
  if (msg?.includes('UserEntity with username') && msg?.includes('not found')) {
    return 'Пользователь с таким логином не найден.';
  }
  if (msg?.includes('Email already exists')) return 'Этот Email уже используется.';
  if (msg?.includes('Invalid username or password')) return 'Неверный логин или пароль.';
  if (errorCode === 'DATA_INTEGRITY_VIOLATION') return 'Нарушение уникальности данных.';
  if (errorCode === 'DTO_VALIDATION_FAILED') return 'Ошибка валидации данных на сервере.';
  if (errorCode === 'ACCESS_DENIED') return 'Доступ запрещен. Недостаточно прав.';
  return msg || 'Произошла непредвиденная ошибка сервера';
};

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token && config.headers) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<{ error?: string; message?: string }>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;

    if (!error.response) {
      toast.error('Сервер недоступен. Проверьте подключение к сети.');
      return Promise.reject(error);
    }

    if (status === 401 && originalRequest && !originalRequest._retry) {
      if (originalRequest.url?.includes(API_ENDPOINTS.AUTH.REFRESH) || originalRequest.url?.includes(API_ENDPOINTS.AUTH.LOGIN)) {
        useAuthStore.getState().logout();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise<string | null>((resolve, reject) => failedQueue.push({ resolve, reject }))
          .then((token) => {
            if (token && originalRequest.headers) originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const fingerprint = getBrowserFingerprint();
        const { data } = await axios.post(`${API_BASE_URL}${API_ENDPOINTS.AUTH.REFRESH}`, { fingerprint }, { withCredentials: true });
        useAuthStore.getState().setAuth(data.accessToken, { username: data.username, role: data.role });
        processQueue(null, data.accessToken);
        if (originalRequest.headers) originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().logout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (status !== 401) {
      toast.error(translateErrorMessage(error.response?.data?.error, error.response?.data?.message));
    }
    return Promise.reject(error);
  }
);
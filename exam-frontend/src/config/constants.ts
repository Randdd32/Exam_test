export const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+-]).{8,60}$/;
export const API_BASE_URL = '/api/v1';

const AUTH_BASE = '/auth';
const USERS_BASE = '/users';
const LOGS_BASE = '/logs';

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: `${AUTH_BASE}/login`,
    REGISTER: `${AUTH_BASE}/register`,
    REFRESH: `${AUTH_BASE}/refresh`,
    LOGOUT: `${AUTH_BASE}/logout`,
  },
  USERS: {
    BASE: USERS_BASE,
    ME: `${USERS_BASE}/me`,
    PASSWORD: `${USERS_BASE}/me/password`,
    DETAILS: (id: number | string) => `${USERS_BASE}/${id}`,
    REVOKE_SESSIONS: (id: number | string) => `${USERS_BASE}/${id}/revoke-sessions`,
  },
  LOGS: {
    FILES: `${LOGS_BASE}/files`,
    TAIL: `${LOGS_BASE}/tail`,
    DOWNLOAD: `${LOGS_BASE}/download`
  }
} as const;
import { apiClient } from '../config/api';
import { getBrowserFingerprint } from '../utils/fingerprint';
import { API_ENDPOINTS } from '../config/constants';
import type { UserRole } from '../types/auth';

export interface AuthResponseDto {
  accessToken: string;
  username: string;
  email: string;
  role: UserRole;
}

export const authService = {
  async login(username: string, password: string): Promise<AuthResponseDto> {
    const fingerprint = getBrowserFingerprint();
    const { data } = await apiClient.post<AuthResponseDto>(API_ENDPOINTS.AUTH.LOGIN, { username, password, fingerprint });
    return data;
  },

  async register(username: string, email: string, password: string): Promise<AuthResponseDto> {
    const fingerprint = getBrowserFingerprint();
    const { data } = await apiClient.post<AuthResponseDto>(API_ENDPOINTS.AUTH.REGISTER, { username, email, password, fingerprint });
    return data;
  },

  async logout(): Promise<void> {
    await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
  }
};
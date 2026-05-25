export type UserRole = 'USER' | 'ADMIN' | 'SUPERADMIN';

export const ROLE_LABELS: Record<UserRole, string> = {
  SUPERADMIN: 'Суперадминистратор',
  ADMIN: 'Администратор',
  USER: 'Пользователь'
};

export interface AuthUser {
  username: string;
  role: UserRole;
}

export interface UserDto {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  createdAt: string;
  updatedAt: string | null;
}
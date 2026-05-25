import { z } from 'zod';
import { PASSWORD_REGEX } from '../config/constants';

export const loginSchema = z.object({
  username: z.string().min(2, 'Логин должен содержать минимум 2 символа').max(100, 'Максимум 100 символов'),
  password: z.string().min(1, 'Введите пароль'),
});
export type LoginFormData = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  username: z.string().min(2, 'Минимум 2 символа').max(100, 'Максимум 100 символов'),
  email: z.string().email('Некорректный формат email').max(254, 'Максимум 254 символа'),
  password: z.string()
    .min(8, 'Минимум 8 символов')
    .max(60, 'Максимум 60 символов')
    .regex(PASSWORD_REGEX, 'Пароль должен содержать заглавную и строчную лат. буквы, цифру и спецсимвол (!@#$%^&*_=+-).'),
  confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Пароли не совпадают',
  path: ['confirmPassword'],
});
export type RegisterFormData = z.infer<typeof registerSchema>;

export const updatePasswordSchema = z.object({
  newPassword: z.string()
    .min(8, 'Минимум 8 символов')
    .max(60, 'Максимум 60 символов')
    .regex(PASSWORD_REGEX, 'Пароль должен содержать заглавную и строчную лат. буквы, цифру и спецсимвол (!@#$%^&*_=+-).'),
  confirmPassword: z.string()
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: 'Пароли не совпадают',
  path: ['confirmPassword'],
});
export type UpdatePasswordFormData = z.infer<typeof updatePasswordSchema>;
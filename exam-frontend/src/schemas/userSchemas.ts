import { z } from 'zod';
import { PASSWORD_REGEX } from '../config/constants';

export const createUserSchema = z.object({
  username: z.string().min(2, 'Логин от 2 до 100 символов').max(100, 'Максимум 100 символов'),
  email: z.string().email('Некорректный формат email').max(254, 'Максимум 254 символа'),
  role: z.enum(['USER', 'ADMIN', 'SUPERADMIN'] as const),
  password: z.string()
    .min(8, 'Минимум 8 символов')
    .max(60, 'Максимум 60 символов')
    .regex(PASSWORD_REGEX, 'Пароль должен содержать заглавную и строчную лат. буквы, цифру и спецсимвол (!@#$%^&*_=+-).')
});
export type CreateUserFormData = z.infer<typeof createUserSchema>;

export const updateUserSchema = z.object({
  role: z.enum(['USER', 'ADMIN', 'SUPERADMIN'] as const),
  password: z.string().optional().refine(val => !val || PASSWORD_REGEX.test(val), {
    message: 'Пароль должен содержать от 8 до 60 символов, заглавную/строчную буквы, цифру и спецсимвол (!@#$%^&*_=+-).'
  })
});
export type UpdateUserFormData = z.infer<typeof updateUserSchema>;
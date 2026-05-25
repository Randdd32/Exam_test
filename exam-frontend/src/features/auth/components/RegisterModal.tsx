import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { registerSchema, type RegisterFormData } from '../../../schemas/authSchemas';
import { authService } from '../../../services/auth.service';
import { useAuthStore } from '../../../store/authStore';
import { Modal } from '../../../components/ui/Modal/Modal';
import { Input } from '../../../components/ui/Input/Input';
import { Button } from '../../../components/ui/Button/Button';
import styles from './AuthForms.module.css';

interface Props { isOpen: boolean; onClose: () => void; onSwitchToLogin: () => void; }

export const RegisterModal = ({ isOpen, onClose, onSwitchToLogin }: Props) => {
  const setAuth = useAuthStore((s) => s.setAuth);

  const { register, handleSubmit, formState: { errors }, reset } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    mode: 'onTouched',
  });

  const mutation = useMutation({
    mutationFn: (data: RegisterFormData) => authService.register(data.username, data.email, data.password),
    onSuccess: (data) => {
      setAuth(data.accessToken, { username: data.username, role: data.role });
      reset();
      onClose();
    }
  });

  const onSubmit = (data: RegisterFormData) => mutation.mutate(data);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Регистрация">
      <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
        <div className={styles.field}>
          <label>Логин <span className={styles.req}>*</span></label>
          <Input {...register('username')} placeholder="Придумайте логин" error={errors.username?.message} />
        </div>
        <div className={styles.field}>
          <label>Email <span className={styles.req}>*</span></label>
          <Input type="email" {...register('email')} placeholder="example@mail.com" error={errors.email?.message} />
        </div>
        <div className={styles.field}>
          <label>Пароль <span className={styles.req}>*</span></label>
          <Input type="password" {...register('password')} placeholder="Надежный пароль" error={errors.password?.message} />
        </div>
        <div className={styles.field}>
          <label>Подтвердите пароль <span className={styles.req}>*</span></label>
          <Input type="password" {...register('confirmPassword')} placeholder="Повторите пароль" error={errors.confirmPassword?.message} />
        </div>
        <Button type="submit" isLoading={mutation.isPending} className={styles.submitBtn}>Создать аккаунт</Button>
        <div className={styles.footerText}>
          Уже есть аккаунт? <button type="button" onClick={onSwitchToLogin} className={styles.linkBtn}>Войти</button>
        </div>
      </form>
    </Modal>
  );
};
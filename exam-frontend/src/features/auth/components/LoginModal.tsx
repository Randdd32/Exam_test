import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { loginSchema, type LoginFormData } from '../../../schemas/authSchemas';
import { authService } from '../../../services/auth.service';
import { useAuthStore } from '../../../store/authStore';
import { Modal } from '../../../components/ui/Modal/Modal';
import { Input } from '../../../components/ui/Input/Input';
import { Button } from '../../../components/ui/Button/Button';
import styles from './AuthForms.module.css';

interface Props { isOpen: boolean; onClose: () => void; onSwitchToRegister: () => void; }

export const LoginModal = ({ isOpen, onClose, onSwitchToRegister }: Props) => {
  const setAuth = useAuthStore((s) => s.setAuth);

  const { register, handleSubmit, formState: { errors }, reset } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: 'onTouched'
  });

  const mutation = useMutation({
    mutationFn: (data: LoginFormData) => authService.login(data.username, data.password),
    onSuccess: (data) => {
      setAuth(data.accessToken, { username: data.username, role: data.role });
      reset();
      onClose();
    }
  });

  const onSubmit = (data: LoginFormData) => mutation.mutate(data);

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Вход в систему">
      <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
        <div className={styles.field}>
          <label>Логин</label>
          <Input {...register('username')} placeholder="Введите логин" error={errors.username?.message} />
        </div>
        <div className={styles.field}>
          <label>Пароль</label>
          <Input type="password" {...register('password')} placeholder="••••••••" error={errors.password?.message} />
        </div>
        <Button type="submit" isLoading={mutation.isPending} className={styles.submitBtn}>Войти</Button>
        <div className={styles.footerText}>
          Нет аккаунта? <button type="button" onClick={onSwitchToRegister} className={styles.linkBtn}>Зарегистрироваться</button>
        </div>
      </form>
    </Modal>
  );
};
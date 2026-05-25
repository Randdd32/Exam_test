import { useParams, useNavigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, Save, AlertTriangle } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { isAxiosError } from 'axios';
import { useAuthStore } from '../../store/authStore';
import { usersService } from '../../services/users.service';
import { createUserSchema, updateUserSchema } from '../../schemas/userSchemas';
import { Input } from '../../components/ui/Input/Input';
import { Button } from '../../components/ui/Button/Button';
import { Select } from '../../components/ui/Select/Select';
import { Card } from '../../components/ui/Card/Card';
import { Spinner } from '../../components/ui/Spinner/Spinner';
import { ErrorState } from '../../components/ui/ErrorState/ErrorState';
import { formatDateTime } from '../../utils/formatters';
import { useDocumentTitle } from '../../hooks/useDocumentTitle';
import type { UserRole, UserDto } from '../../types/auth';
import styles from '../../styles/layouts/editPageLayout.module.css';

type FormValues = {
  username: string;
  email: string;
  role: UserRole;
  password?: string;
};

export const UserEditPage = () => {
  const { id } = useParams<{ id: string }>();
  const isNew = id === 'new';
  useDocumentTitle(isNew ? 'Новый пользователь' : 'Редактирование пользователя');

  const { data: originalData, isLoading, isError } = useQuery({
    queryKey: ['user', id],
    queryFn: () => usersService.getById(Number(id)),
    enabled: !isNew
  });

  if (isLoading) return <Spinner fullPage size={40} />;
  if (isError) return <ErrorState message="Не удалось получить данные пользователя." />;

  return <UserForm key={isNew ? 'new' : originalData?.id} isNew={isNew} id={id!} originalData={originalData} />;
};

interface UserFormProps {
  isNew: boolean;
  id: string;
  originalData?: UserDto;
}

const UserForm = ({ isNew, id, originalData }: UserFormProps) => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user: currentUser, logout } = useAuthStore();

  const isSelf = currentUser?.username === originalData?.username;
  const isSuperAdmin = currentUser?.role === 'SUPERADMIN';

  const { register, handleSubmit, control, watch, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(isNew ? createUserSchema : updateUserSchema),
    mode: 'onTouched',
    defaultValues: {
      username: originalData?.username || '',
      email: originalData?.email || '',
      role: originalData?.role || 'USER',
      password: ''
    }
  });

  const passwordValue = watch('password');

  const saveMutation = useMutation({
    mutationFn: async (data: FormValues) => {
      if (isNew) {
        return usersService.create({
          username: data.username,
          email: data.email,
          password: data.password as string,
          role: data.role
        });
      } else {
        return usersService.update(Number(id), { 
          role: data.role, 
          password: data.password || undefined 
        });
      }
    },
    onSuccess: () => {
      toast.success(isNew ? 'Пользователь создан' : 'Данные обновлены');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      
      if (isSelf && passwordValue) {
        logout(); 
        navigate('/?auth=login', { replace: true });
        toast('Вы были выведены из системы в связи со сменой пароля.', { icon: '🔒' });
      } else {
        navigate('/admin/users');
      }
    },
    onError: (e: Error | unknown) => {
      if (!isAxiosError(e)) {
        toast.error((e as Error).message || 'Ошибка валидации');
      }
    }
  });

  const onSubmit = (data: FormValues) => saveMutation.mutate(data);

  const roleOptions = isSuperAdmin ? [
    { value: 'USER', label: 'Пользователь (USER)' },
    { value: 'ADMIN', label: 'Администратор (ADMIN)' }
  ] : [];

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Button variant="ghost" onClick={() => navigate('/admin/users')} className={styles.backBtn}>
          <ArrowLeft size={16} /> Назад
        </Button>
        <div>
          <h1 className={styles.title}>{isNew ? 'Добавление пользователя' : 'Редактирование профиля'}</h1>
          <p className={styles.subtitle}>Управление доступом к системе</p>
        </div>
      </div>

      <div className={styles.content}>
        <Card className={styles.formCard}>
          <h3 className={styles.cardTitle}>Учетные данные</h3>
          
          <form id="user-form" onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div className={styles.field}>
              <label className={styles.label}>Логин {isNew && <span className={styles.req}>*</span>}</label>
              <Input 
                {...register('username')} 
                disabled={!isNew} 
                placeholder="Введите логин" 
                error={errors.username?.message} 
              />
              {!isNew && <p className={styles.hint}>Логин нельзя изменить после создания.</p>}
            </div>

            <div className={styles.field}>
              <label className={styles.label}>Email {isNew && <span className={styles.req}>*</span>}</label>
              <Input 
                type="email" 
                {...register('email')} 
                disabled={!isNew} 
                placeholder="example@mail.com" 
                error={errors.email?.message} 
              />
              {!isNew && <p className={styles.hint}>Email нельзя изменить после создания.</p>}
            </div>

            <div className={styles.field}>
              <label className={styles.label}>Пароль {isNew && <span className={styles.req}>*</span>}</label>
              <Input 
                type="password" 
                {...register('password')} 
                placeholder={isNew ? 'Задайте безопасный пароль' : 'Введите новый пароль, чтобы изменить текущий'} 
                error={errors.password?.message} 
              />
              {isSelf && passwordValue && (
                <div className={styles.warningBanner}>
                  <AlertTriangle size={18} />
                  Смена пароля приведет к завершению всех активных сессий.
                </div>
              )}
            </div>

            {(!isSelf && isSuperAdmin) && (
              <div className={styles.field}>
                <label className={styles.label}>Системная роль <span className={styles.req}>*</span></label>
                <Controller
                  name="role"
                  control={control}
                  render={({ field }) => (
                    <Select 
                      value={field.value} 
                      onChange={field.onChange} 
                      options={roleOptions} 
                      isSearchable={false} 
                    />
                  )}
                />
              </div>
            )}
          </form>

          <div className={styles.actions}>
            <Button variant="secondary" onClick={() => navigate('/admin/users')}>Отмена</Button>
            <Button type="submit" form="user-form" isLoading={saveMutation.isPending}>
              <Save size={16} /> {isNew ? 'Создать аккаунт' : 'Сохранить изменения'}
            </Button>
          </div>
        </Card>

        {!isNew && originalData && (
          <div className={styles.metaColumn}>
            <Card className={styles.metaCard}>
              <h3 className={styles.cardTitle}>Системная информация</h3>
              
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>ID</span>
                <span className={styles.metaValue}>{originalData.id}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Создан</span>
                <span className={styles.metaValue}>{formatDateTime(originalData.createdAt)}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Обновлен</span>
                <span className={styles.metaValue}>{formatDateTime(originalData.updatedAt)}</span>
              </div>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
};
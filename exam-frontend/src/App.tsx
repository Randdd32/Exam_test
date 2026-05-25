import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { useUiStore } from './store/uiStore';
import { useAuthStore } from './store/authStore';
import { Spinner } from './components/ui/Spinner/Spinner';
import { ProtectedRoute } from './components/ProtectedRoute/ProtectedRoute';
import { MainLayout } from './layouts/MainLayout/MainLayout';
import { AdminLayout } from './layouts/AdminLayout/AdminLayout';
import { UsersPage } from './pages/UsersPage/UsersPage';
import { UserEditPage } from './pages/UserEditPage/UserEditPage';
import { LogsPage } from './pages/LogsPage/LogsPage';
import './styles/globals.css';

const HomePage = () => <h1>Главная страница (доступна всем)</h1>;
const ProfilePage = () => <h1>Профиль (Только авторизованным)</h1>;

const queryClient = new QueryClient({
  defaultOptions: { queries: { refetchOnWindowFocus: false, retry: 1 } },
});

export const App = () => {
  const theme = useUiStore((state) => state.theme);
  const { isInitialized, checkAuth } = useAuthStore();

  useEffect(() => { document.documentElement.setAttribute('data-theme', theme); }, [theme]);
  useEffect(() => { checkAuth(); }, [checkAuth]);

  if (!isInitialized) return <Spinner fullPage size={48} />;

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<HomePage />} />
            
            <Route element={<ProtectedRoute />}>
              <Route path="profile" element={<ProfilePage />} />
              {/* Тут будут защищенные роуты билета (Мои заказы, Мои фото и т.д.) */}
            </Route>
          </Route>

          <Route path="/admin" element={<ProtectedRoute allowedRoles={['ADMIN', 'SUPERADMIN']} />}>
            <Route element={<AdminLayout />}>
              <Route index element={<Navigate to="users" replace />} />
              <Route path="users" element={<UsersPage />} />
              <Route path="users/:id" element={<UserEditPage />} />
              <Route path="logs" element={<LogsPage />} />
            </Route>
          </Route>
          
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      
      <Toaster 
        position="top-center" 
        toastOptions={{
          style: {
            background: 'var(--bg-surface)', color: 'var(--text-primary)',
            border: '1px solid var(--border-color)', maxWidth: '500px'
          }
        }}
      />
    </QueryClientProvider>
  );
};

export default App;
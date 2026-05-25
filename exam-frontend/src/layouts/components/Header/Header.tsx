import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Shield, User, LogOut, ChevronDown, LayoutDashboard } from 'lucide-react';
import { clsx } from 'clsx';
import { useAuthStore } from '../../../store/authStore';
import { authService } from '../../../services/auth.service';
import { ROLE_LABELS } from '../../../types/auth';
import { Button } from '../../../components/ui/Button/Button';
import { LoginModal } from '../../../features/auth/components/LoginModal';
import { RegisterModal } from '../../../features/auth/components/RegisterModal';
import styles from './Header.module.css';

export const Header = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user, isAuthenticated, isAdmin, logout } = useAuthStore();

  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);

  const authParam = searchParams.get('auth');
  const isLoginOpen = authParam === 'login';
  const isRegisterOpen = authParam === 'register';

  const setAuthModal = (type: 'login' | 'register' | null) => {
    setSearchParams(prev => {
      const next = new URLSearchParams(prev);
      if (type) next.set('auth', type);
      else next.delete('auth');
      return next;
    });
  };

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) setIsProfileOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    try { await authService.logout(); } 
    finally { logout(); navigate('/'); }
  };

  return (
    <>
      <header className={styles.header}>
        <Link to="/" className={styles.logo}>
          <Shield size={24} className={styles.logoIcon} />
          <span>ExamApp</span>
        </Link>

        <div className={styles.rightSide}>
          {!isAuthenticated ? (
            <div className={styles.authButtons}>
              <Button variant="ghost" onClick={() => setAuthModal('login')}>Вход</Button>
              <Button onClick={() => setAuthModal('register')}>Регистрация</Button>
            </div>
          ) : (
            <div className={styles.profileWrapper} ref={profileRef}>
              <div className={styles.userProfile} onClick={() => setIsProfileOpen(!isProfileOpen)}>
                <div className={styles.avatar}>{user?.username.charAt(0).toUpperCase()}</div>
                <div className={styles.userInfo}>
                  <span className={styles.userName}>{user?.username}</span>
                  <span className={styles.userRole}>{user ? ROLE_LABELS[user.role] : ''}</span>
                </div>
                <ChevronDown size={16} className={clsx(styles.chevron, { [styles.rotated]: isProfileOpen })} />
              </div>

              {isProfileOpen && (
                <div className={styles.dropdownMenu}>
                  <Link to="/profile" className={styles.dropdownItem} onClick={() => setIsProfileOpen(false)}>
                    <User size={16} /> Мой профиль
                  </Link>
                  <div className={styles.dropdownDivider} />
                  {isAdmin && (
                    <>
                      <Link to="/admin" className={styles.dropdownItem} onClick={() => setIsProfileOpen(false)}>
                        <LayoutDashboard size={16} /> Админ-панель
                      </Link>
                      
                    </>
                  )}
                  <div className={styles.dropdownDivider} />
                  <button className={clsx(styles.dropdownItem, styles.logoutBtn)} onClick={handleLogout}>
                    <LogOut size={16} /> Выйти
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </header>

      <LoginModal 
        isOpen={isLoginOpen} 
        onClose={() => setAuthModal(null)} 
        onSwitchToRegister={() => setAuthModal('register')} 
      />
      
      <RegisterModal 
        isOpen={isRegisterOpen} 
        onClose={() => setAuthModal(null)} 
        onSwitchToLogin={() => setAuthModal('login')} 
      />
    </>
  );
};
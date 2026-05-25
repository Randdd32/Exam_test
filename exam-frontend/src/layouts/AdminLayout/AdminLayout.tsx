import { useState, useRef, useEffect } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { clsx } from 'clsx';
import { Users, Terminal, LogOut, Sun, Moon, Menu, Shield, ChevronDown, User, Monitor } from 'lucide-react';
import { ROLE_LABELS } from '../../types/auth';
import { useUiStore } from '../../store/uiStore';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/auth.service';
import styles from './AdminLayout.module.css';

export const AdminLayout = () => {
  const { theme, toggleTheme, isSidebarOpen, toggleSidebar } = useUiStore();
  const { user, logout } = useAuthStore();
  const location = useLocation();
  const navigate = useNavigate();

  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) setIsProfileOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleClick = () => { if (window.innerWidth <= 630) toggleSidebar(); };

  const handleLogout = async () => {
    try { await authService.logout(); } 
    finally { logout(); navigate('/'); }
  };

  return (
    <div className={styles.layout}>
       <div className={clsx(styles.sidebarBackdrop, { [styles.open]: isSidebarOpen })} onClick={toggleSidebar} />
       
      <aside className={clsx(styles.sidebar, { [styles.closed]: !isSidebarOpen })}>
        <div className={styles.sidebarHeader}>
          <Link to="/" className={styles.logo}>
            <Shield size={24} className={styles.logoIcon} />
            {isSidebarOpen && <span>Панель администратора</span>}
          </Link>
        </div>
        
        <nav className={styles.nav}>
          {[
            { path: '/admin/users', label: 'Пользователи', icon: Users },
            { path: '/admin/logs', label: 'Системные логи', icon: Terminal }
          ].map((item) => {
            const Icon = item.icon;
            return (
              <Link 
                key={item.path} to={item.path} onClick={handleClick}
                className={clsx(styles.navItem, { [styles.active]: location.pathname.includes(item.path) })}
              >
                <Icon size={20} className={styles.navIcon} />
                {isSidebarOpen && <span>{item.label}</span>}
              </Link>
            );
          })}
        </nav>
      </aside>

      <div className={styles.mainWrapper}>
        <header className={styles.header}>
          <div className={styles.headerLeft}>
            <button onClick={toggleSidebar} className={styles.iconButton}><Menu size={20} /></button>
            <h2 className={styles.pageTitle}>Управление системой</h2>
          </div>
          
          <div className={styles.headerRight}>
            <Link to="/" className={styles.iconButton} title="На сайт"><Monitor size={20} /></Link>
            <button onClick={toggleTheme} className={styles.iconButton} title="Сменить тему">
              {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
            </button>
            
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
                  <button className={clsx(styles.dropdownItem, styles.logoutBtn)} onClick={handleLogout}>
                    <LogOut size={16} /> Выйти
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};
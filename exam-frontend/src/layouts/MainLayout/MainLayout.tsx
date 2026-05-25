import { Outlet } from 'react-router-dom';
import { Header } from '../components/Header/Header';
import styles from './MainLayout.module.css';

export const MainLayout = () => {
  return (
    <div className={styles.layout}>
      <Header />
      <main className={styles.main}>
        <div className={styles.container}>
          <Outlet />
        </div>
      </main>
    </div>
  );
};
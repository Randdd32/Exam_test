import { forwardRef, type InputHTMLAttributes, type ReactNode } from 'react';
import { clsx } from 'clsx';
import { X, AlertCircle } from 'lucide-react';
import styles from './Input.module.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  icon?: ReactNode;
  error?: string;
  onClear?: () => void;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, icon, error, onClear, value, placeholder, ...props }, ref) => {
    const showClearButton = onClear && value && String(value).length > 0;

    return (
      <div className={styles.wrapper}>
        <div className={styles.inputContainer}>
          {icon && <span className={styles.icon}>{icon}</span>}
          
          <input
            ref={ref}
            value={value}
            placeholder={placeholder}
            className={clsx(
              styles.input,
              { [styles.withIcon]: !!icon, [styles.hasError]: !!error, [styles.withClear]: showClearButton },
              className
            )}
            {...props}
          />

          {showClearButton && (
            <button type="button" onClick={onClear} className={styles.clearButton} title="Очистить">
              <X size={16} />
            </button>
          )}
          {error && <AlertCircle size={16} className={styles.errorIcon} />}
        </div>
        {error && <span className={styles.errorMessage}>{error}</span>}
      </div>
    );
  }
);
Input.displayName = 'Input';
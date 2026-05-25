import { SearchableSelect } from '../../../../components/ui/SearchableSelect/SearchableSelect';
import { Select } from '../../../../components/ui/Select/Select';
import { type SelectOption } from '../../../../types/common';
import type { FilterValue } from '../../../../hooks/useUrlFilters';
import styles from '../../styles/filterForms.module.css';

type UpdateFiltersFn = (updates: Record<string, FilterValue>) => void;

interface MultiSelectProps {
  label: string;
  value: (string | number)[];
  onChange: (val: (string | number)[]) => void;
  fetchOptions: (s?: string) => Promise<SelectOption[]>;
  fetchByIds: (ids: (string | number)[]) => Promise<SelectOption[]>;
  placeholder?: string;
}

export const MultiSelectFilter = ({ label, value, onChange, fetchOptions, fetchByIds, placeholder }: MultiSelectProps) => (
  <div className={styles.filterGroup}>
    <label className={styles.label}>{label}</label>
    <SearchableSelect
      isMulti
      value={value}
      onChange={(val) => onChange((val as (string | number)[]) ??[])} 
      fetchOptions={fetchOptions}
      fetchByIds={fetchByIds}
      placeholder={placeholder}
    />
  </div>
);

interface StaticSelectProps {
  label: string;
  value: string | number | boolean | null;
  onChange: (val: string | number | boolean | null) => void;
  options: { value: string | number | boolean; label: string }[];
}

export const StaticSelectFilter = ({ label, value, onChange, options }: StaticSelectProps) => (
  <div className={styles.filterGroup}>
    <label className={styles.label}>{label}</label>
    <Select value={value} onChange={onChange} options={options} isSearchable={false} />
  </div>
);

interface DateRangeProps {
  filters: Record<string, FilterValue>;
  updateFilters: UpdateFiltersFn;
  label: string;
  fromKey: string;
  toKey: string;
}

export const DateRangeFilters = ({ filters, updateFilters, label, fromKey, toKey }: DateRangeProps) => (
  <div className={styles.hierarchyGroup}>
    <h4 className={styles.hierarchyTitle}>{label}</h4>
    <div className={styles.filterGroup}>
      <label className={styles.label}>От</label>
      <input type="datetime-local" step="1" className={styles.nativeInput}
        value={(filters[fromKey] as string) || ''}
        onChange={(e) => updateFilters({ [fromKey]: e.target.value })}
      />
    </div>
    <div className={styles.filterGroup}>
      <label className={styles.label}>До</label>
      <input type="datetime-local" step="1" className={styles.nativeInput}
        value={(filters[toKey] as string) || ''}
        onChange={(e) => updateFilters({ [toKey]: e.target.value })}
      />
    </div>
  </div>
);

interface AuditDateProps {
  filters: Record<string, FilterValue>;
  updateFilters: UpdateFiltersFn;
}

export const AuditDateFilters = ({ filters, updateFilters }: AuditDateProps) => (
  <>
    <DateRangeFilters 
      label="Дата создания" 
      fromKey="createdAfter" 
      toKey="createdBefore" 
      filters={filters} 
      updateFilters={updateFilters} 
    />
    <DateRangeFilters 
      label="Дата обновления" 
      fromKey="updatedAfter" 
      toKey="updatedBefore" 
      filters={filters} 
      updateFilters={updateFilters} 
    />
  </>
);

interface NumberRangeProps {
  filters: Record<string, FilterValue>;
  updateFilters: UpdateFiltersFn;
  label: string;
  minKey: string;
  maxKey: string;
}

export const NumberRangeFilter = ({ filters, updateFilters, label, minKey, maxKey }: NumberRangeProps) => (
  <div className={styles.hierarchyGroup}>
    <h4 className={styles.hierarchyTitle}>{label}</h4>
    <div className={styles.filterGroup}>
      <label className={styles.label}>От</label>
      <input type="number" className={styles.nativeInput} placeholder="Минимум"
        value={(filters[minKey] as number) || ''}
        onChange={(e) => updateFilters({ [minKey]: e.target.value ? Number(e.target.value) : undefined })}
      />
    </div>
    <div className={styles.filterGroup}>
      <label className={styles.label}>До</label>
      <input type="number" className={styles.nativeInput} placeholder="Максимум"
        value={(filters[maxKey] as number) || ''}
        onChange={(e) => updateFilters({[maxKey]: e.target.value ? Number(e.target.value) : undefined })}
      />
    </div>
  </div>
);
export const formatDateTime = (dateString?: string | null): string => {
  if (!dateString) return 'Н/Д';
  const date = new Date(dateString);
  return date.toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).replace(',', '');
};

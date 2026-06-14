import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { fetchDrawHistory } from '../services/drawService';

export function useDrawHistory(page: number, size: number = 20) {
  return useQuery({
    queryKey: ['drawHistory', page, size],
    queryFn: () => fetchDrawHistory(page, size),
    placeholderData: keepPreviousData,
  });
}

import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { fetchDrawHistory } from '../services/drawService';

export function useDrawHistory(page: number, size: number = 20, equipeId?: number | null) {
  return useQuery({
    queryKey: ['drawHistory', equipeId ?? null, page, size],
    queryFn: () => fetchDrawHistory(page, size, equipeId),
    placeholderData: keepPreviousData,
  });
}

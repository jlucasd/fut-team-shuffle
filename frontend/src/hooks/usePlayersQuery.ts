import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchPlayers,
  createPlayer,
  updatePlayer,
  deletePlayer,
  togglePlayerStatus,
  type PlayerFilters,
} from '../services/playerService';
import type { PlayerCreateRequest, PlayerUpdateRequest } from '../types';

const PLAYERS_KEY = 'players';

export function usePlayersQuery(filters: PlayerFilters = {}) {
  return useQuery({
    queryKey: [PLAYERS_KEY, filters],
    queryFn: () => fetchPlayers(filters),
  });
}

export function useCreatePlayer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: PlayerCreateRequest) => createPlayer(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLAYERS_KEY] });
    },
  });
}

export function useUpdatePlayer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: PlayerUpdateRequest }) => updatePlayer(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLAYERS_KEY] });
    },
  });
}

export function useDeletePlayer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => deletePlayer(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLAYERS_KEY] });
    },
  });
}

export function useTogglePlayerStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => togglePlayerStatus(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLAYERS_KEY] });
    },
  });
}

import { useMutation } from '@tanstack/react-query';
import { executeDraw, updateTeams } from '../services/drawService';
import type { DrawRequest, TeamEditRequest } from '../types';

export function useDrawMutation() {
  return useMutation({
    mutationFn: (data: DrawRequest) => executeDraw(data),
  });
}

export function useUpdateTeamsMutation() {
  return useMutation({
    mutationFn: ({ drawId, data }: { drawId: number; data: TeamEditRequest }) =>
      updateTeams(drawId, data),
  });
}

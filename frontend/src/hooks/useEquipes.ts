import { useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createEquipe, deleteEquipe, fetchEquipes, updateEquipe } from '../services/equipeService';
import { useEquipeStore } from '../stores/equipeStore';

const EQUIPES_KEY = 'equipes';

export function useEquipesQuery() {
  return useQuery({
    queryKey: [EQUIPES_KEY],
    queryFn: fetchEquipes,
  });
}

/**
 * The currently selected team. Falls back to the first team when nothing is
 * selected yet or the stored selection no longer exists.
 */
export function useCurrentEquipe() {
  const { data: equipes, isLoading } = useEquipesQuery();
  const selectedEquipeId = useEquipeStore((s) => s.selectedEquipeId);
  const setSelectedEquipeId = useEquipeStore((s) => s.setSelectedEquipeId);

  const current = equipes?.find((e) => e.id === selectedEquipeId) ?? equipes?.[0] ?? null;

  useEffect(() => {
    if (current && current.id !== selectedEquipeId) {
      setSelectedEquipeId(current.id);
    }
  }, [current, selectedEquipeId, setSelectedEquipeId]);

  return { equipe: current, equipeId: current?.id ?? null, equipes: equipes ?? [], isLoading };
}

export function useCreateEquipe() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (nome: string) => createEquipe(nome),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [EQUIPES_KEY] }),
  });
}

export function useUpdateEquipe() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, nome }: { id: number; nome: string }) => updateEquipe(id, nome),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EQUIPES_KEY] });
      queryClient.invalidateQueries({ queryKey: ['players'] });
    },
  });
}

export function useDeleteEquipe() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteEquipe(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [EQUIPES_KEY] }),
  });
}

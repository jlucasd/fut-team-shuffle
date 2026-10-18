import api from '../lib/api';
import type { Equipe } from '../types';

export async function fetchEquipes(): Promise<Equipe[]> {
  const response = await api.get<Equipe[]>('/api/equipes');
  return response.data;
}

export async function createEquipe(nome: string): Promise<Equipe> {
  const response = await api.post<Equipe>('/api/equipes', { nome });
  return response.data;
}

export async function updateEquipe(id: number, nome: string): Promise<Equipe> {
  const response = await api.put<Equipe>(`/api/equipes/${id}`, { nome });
  return response.data;
}

export async function deleteEquipe(id: number): Promise<void> {
  await api.delete(`/api/equipes/${id}`);
}

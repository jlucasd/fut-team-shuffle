import api from '../lib/api';
import type { Player, PlayerCreateRequest, PlayerUpdateRequest, PageResponse, Position } from '../types';

export interface PlayerFilters {
  posicao?: Position | '';
  ativo?: boolean | '';
  page?: number;
  size?: number;
}

export async function fetchPlayers(filters: PlayerFilters = {}): Promise<PageResponse<Player>> {
  const params: Record<string, string | number | boolean> = {};

  if (filters.posicao) params.posicao = filters.posicao;
  if (filters.ativo !== undefined && filters.ativo !== '') params.ativo = filters.ativo;
  if (filters.page !== undefined) params.page = filters.page;
  if (filters.size !== undefined) params.size = filters.size;

  const response = await api.get<PageResponse<Player>>('/api/jogadores', { params });
  return response.data;
}

export async function createPlayer(data: PlayerCreateRequest): Promise<Player> {
  const response = await api.post<Player>('/api/jogadores', data);
  return response.data;
}

export async function updatePlayer(id: number, data: PlayerUpdateRequest): Promise<Player> {
  const response = await api.put<Player>(`/api/jogadores/${id}`, data);
  return response.data;
}

export async function deletePlayer(id: number): Promise<void> {
  await api.delete(`/api/jogadores/${id}`);
}

export async function togglePlayerStatus(id: number): Promise<Player> {
  const response = await api.patch<Player>(`/api/jogadores/${id}/status`);
  return response.data;
}

import api from '../lib/api';
import type { DrawResult, DrawRequest, TeamEditRequest, PageResponse } from '../types';

export async function executeDraw(data: DrawRequest): Promise<DrawResult> {
  const response = await api.post<DrawResult>('/api/sorteio', data);
  return response.data;
}

export async function updateTeams(drawId: number, data: TeamEditRequest): Promise<DrawResult> {
  const response = await api.put<DrawResult>(`/api/sorteio/${drawId}/times`, data);
  return response.data;
}

export async function getDrawById(id: number): Promise<DrawResult> {
  const response = await api.get<DrawResult>(`/api/sorteio/${id}`);
  return response.data;
}

export async function fetchDrawHistory(page: number, size: number): Promise<PageResponse<DrawResult>> {
  const response = await api.get<PageResponse<DrawResult>>('/api/sorteio/historico', {
    params: { page, size },
  });
  return response.data;
}

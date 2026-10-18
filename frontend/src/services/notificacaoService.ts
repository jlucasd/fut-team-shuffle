import api from '../lib/api';

export interface NotificacaoStatus {
  firebaseConfigurado: boolean;
  dispositivos: number;
}

export async function registerDevice(token: string, plataforma: string): Promise<void> {
  await api.post('/api/notificacoes/dispositivos', { token, plataforma });
}

export async function fetchNotificacaoStatus(): Promise<NotificacaoStatus> {
  const response = await api.get<NotificacaoStatus>('/api/notificacoes/status');
  return response.data;
}

export async function sendTestNotification(): Promise<{ enviados: number; falhas: number }> {
  const response = await api.post<{ enviados: number; falhas: number }>('/api/notificacoes/teste');
  return response.data;
}

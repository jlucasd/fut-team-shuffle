export type Position = 'GOLEIRO' | 'ZAGUEIRO' | 'MEIO' | 'ATACANTE';

export interface Player {
  id: number;
  nome: string;
  posicao: Position;
  nivel: number;
  ativo: boolean;
}

export interface PlayerCreateRequest {
  nome: string;
  posicao: Position;
  nivel: number;
  ativo: boolean;
}

export interface PlayerUpdateRequest {
  nome: string;
  posicao: Position;
  nivel: number;
  ativo: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface DrawResult {
  id: number;
  dataHora: string;
  timeAmarelo: Player[];
  timePreto: Player[];
  reservas: Player[];
  mediaAmarelo: number;
  mediaPreto: number;
  equilibrado: boolean;
}

export interface DrawRequest {
  jogadorIds: number[];
}

export interface TeamEditRequest {
  timeAmarelo: number[];
  timePreto: number[];
  reserva?: number;
}

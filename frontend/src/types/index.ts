export type Position = 'GOLEIRO' | 'ZAGUEIRO' | 'MEIO' | 'ATACANTE';

export interface Player {
  id: number;
  nome: string;
  posicao: Position;
  nivel: number;
  ativo: boolean;
  dataNascimento?: string | null;
  equipeId?: number | null;
  equipeNome?: string | null;
}

/** A club/team whose members are registered in the app (not the draw side). */
export interface Equipe {
  id: number;
  nome: string;
}

export interface PlayerCreateRequest {
  nome: string;
  posicao: Position;
  nivel: number;
  ativo: boolean;
  dataNascimento?: string | null;
  equipeId?: number | null;
}

export interface PlayerUpdateRequest {
  nome: string;
  posicao: Position;
  nivel: number;
  ativo: boolean;
  dataNascimento?: string | null;
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
  equipeId?: number | null;
}

export interface DrawRequest {
  jogadorIds: number[];
  equipeId?: number | null;
}

export interface TeamEditRequest {
  timeAmarelo: number[];
  timePreto: number[];
  reservas?: number[];
}

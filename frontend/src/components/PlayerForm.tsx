import { useState, useEffect } from 'react';
import type { Player, PlayerCreateRequest, PlayerUpdateRequest, Position } from '../types';

interface PlayerFormProps {
  player?: Player | null;
  onSubmit: (data: PlayerCreateRequest | PlayerUpdateRequest) => void;
  onCancel: () => void;
  isLoading?: boolean;
}

const POSITIONS: { value: Position; label: string }[] = [
  { value: 'GOLEIRO', label: 'Goleiro' },
  { value: 'ZAGUEIRO', label: 'Zagueiro' },
  { value: 'MEIO', label: 'Meio' },
  { value: 'ATACANTE', label: 'Atacante' },
];

interface FormErrors {
  nome?: string;
  posicao?: string;
  nivel?: string;
}

export function PlayerForm({ player, onSubmit, onCancel, isLoading }: PlayerFormProps) {
  const [nome, setNome] = useState('');
  const [posicao, setPosicao] = useState<Position | ''>('');
  const [nivel, setNivel] = useState<number>(3);
  const [ativo, setAtivo] = useState(true);
  const [errors, setErrors] = useState<FormErrors>({});

  useEffect(() => {
    if (player) {
      setNome(player.nome);
      setPosicao(player.posicao);
      setNivel(player.nivel);
      setAtivo(player.ativo);
    } else {
      setNome('');
      setPosicao('');
      setNivel(3);
      setAtivo(true);
    }
    setErrors({});
  }, [player]);

  function validate(): boolean {
    const newErrors: FormErrors = {};

    if (!nome.trim()) {
      newErrors.nome = 'Nome é obrigatório';
    }
    if (!posicao) {
      newErrors.posicao = 'Posição é obrigatória';
    }
    if (nivel < 1 || nivel > 5) {
      newErrors.nivel = 'Nível deve ser entre 1 e 5';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const data = {
      nome: nome.trim(),
      posicao: posicao as Position,
      nivel,
      ativo,
    };

    onSubmit(data);
  }

  const isEditing = !!player;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/60"
        onClick={onCancel}
        aria-hidden="true"
      />
      <div className="relative bg-gavioes-dark-gray border border-gavioes-white/10 rounded-lg p-6 w-full max-w-md mx-4">
        <h2 className="text-xl font-title text-gavioes-yellow mb-4">
          {isEditing ? 'Editar Jogador' : 'Novo Jogador'}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="nome" className="block text-sm font-body text-gavioes-white/80 mb-1">
              Nome *
            </label>
            <input
              id="nome"
              type="text"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              className="w-full px-3 py-2 rounded bg-gavioes-black border border-gavioes-white/20 text-gavioes-white font-body focus:border-gavioes-yellow focus:outline-none"
              placeholder="Nome do jogador"
            />
            {errors.nome && (
              <p className="text-red-400 text-sm mt-1 font-body">{errors.nome}</p>
            )}
          </div>

          <div>
            <label htmlFor="posicao" className="block text-sm font-body text-gavioes-white/80 mb-1">
              Posição *
            </label>
            <select
              id="posicao"
              value={posicao}
              onChange={(e) => setPosicao(e.target.value as Position | '')}
              className="w-full px-3 py-2 rounded bg-gavioes-black border border-gavioes-white/20 text-gavioes-white font-body focus:border-gavioes-yellow focus:outline-none"
            >
              <option value="">Selecione a posição</option>
              {POSITIONS.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>
            {errors.posicao && (
              <p className="text-red-400 text-sm mt-1 font-body">{errors.posicao}</p>
            )}
          </div>

          <div>
            <label htmlFor="nivel" className="block text-sm font-body text-gavioes-white/80 mb-1">
              Nível (1-5) *
            </label>
            <input
              id="nivel"
              type="number"
              min={1}
              max={5}
              value={nivel}
              onChange={(e) => setNivel(Number(e.target.value))}
              className="w-full px-3 py-2 rounded bg-gavioes-black border border-gavioes-white/20 text-gavioes-white font-body focus:border-gavioes-yellow focus:outline-none"
            />
            {errors.nivel && (
              <p className="text-red-400 text-sm mt-1 font-body">{errors.nivel}</p>
            )}
          </div>

          <div className="flex items-center gap-3">
            <label htmlFor="ativo" className="text-sm font-body text-gavioes-white/80">
              Ativo
            </label>
            <button
              id="ativo"
              type="button"
              role="switch"
              aria-checked={ativo}
              onClick={() => setAtivo(!ativo)}
              className={`relative w-11 h-6 rounded-full transition-colors ${
                ativo ? 'bg-gavioes-yellow' : 'bg-gavioes-white/20'
              }`}
            >
              <span
                className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white transition-transform ${
                  ativo ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 rounded bg-gavioes-white/10 text-gavioes-white hover:bg-gavioes-white/20 font-body transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 rounded bg-gavioes-yellow text-gavioes-black font-body font-semibold hover:bg-gavioes-yellow/90 transition-colors disabled:opacity-50"
            >
              {isLoading ? 'Salvando...' : isEditing ? 'Salvar' : 'Criar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

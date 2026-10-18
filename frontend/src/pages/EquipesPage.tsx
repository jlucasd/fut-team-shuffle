import { useState } from 'react';
import type { Equipe } from '../types';
import {
  useCreateEquipe,
  useCurrentEquipe,
  useDeleteEquipe,
  useUpdateEquipe,
} from '../hooks/useEquipes';
import { useEquipeStore } from '../stores/equipeStore';
import { ConfirmDialog } from '../components/ConfirmDialog';

function errorMessage(error: unknown, fallback: string): string {
  const data = (error as { response?: { data?: Record<string, string> } })?.response?.data;
  return data?.mensagem || data?.nome || fallback;
}

export function EquipesPage() {
  const { equipes, equipeId, isLoading } = useCurrentEquipe();
  const setSelectedEquipeId = useEquipeStore((s) => s.setSelectedEquipeId);
  const createMutation = useCreateEquipe();
  const updateMutation = useUpdateEquipe();
  const deleteMutation = useDeleteEquipe();

  const [novoNome, setNovoNome] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editNome, setEditNome] = useState('');
  const [deleting, setDeleting] = useState<Equipe | null>(null);
  const [error, setError] = useState<string | null>(null);

  function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!novoNome.trim()) return;
    setError(null);
    createMutation.mutate(novoNome.trim(), {
      onSuccess: (created) => {
        setNovoNome('');
        setSelectedEquipeId(created.id);
      },
      onError: (err) => setError(errorMessage(err, 'Erro ao criar o time.')),
    });
  }

  function handleSave(id: number) {
    if (!editNome.trim()) return;
    setError(null);
    updateMutation.mutate(
      { id, nome: editNome.trim() },
      {
        onSuccess: () => setEditingId(null),
        onError: (err) => setError(errorMessage(err, 'Erro ao renomear o time.')),
      }
    );
  }

  function handleDelete() {
    if (!deleting) return;
    setError(null);
    deleteMutation.mutate(deleting.id, {
      onSettled: () => setDeleting(null),
      onError: (err) => setError(errorMessage(err, 'Erro ao excluir o time.')),
    });
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-title text-gavioes-yellow">Times</h1>
        <p className="text-sm text-gavioes-white/60 mt-1">
          Cada time tem seus próprios jogadores, sorteios e aniversariantes.
        </p>
      </div>

      <form onSubmit={handleCreate} className="flex flex-wrap gap-2">
        <input
          type="text"
          value={novoNome}
          onChange={(e) => setNovoNome(e.target.value)}
          placeholder="Nome do novo time"
          maxLength={80}
          className="flex-1 min-w-[200px] px-3 py-2 rounded bg-gavioes-dark-gray border border-gavioes-white/20 text-gavioes-white focus:border-gavioes-yellow focus:outline-none"
        />
        <button
          type="submit"
          disabled={!novoNome.trim() || createMutation.isPending}
          className="px-4 py-2 rounded bg-gavioes-yellow text-gavioes-black font-semibold hover:bg-gavioes-yellow/90 disabled:opacity-50 transition-colors"
        >
          + Novo time
        </button>
      </form>

      {error && (
        <div className="px-4 py-2 rounded-lg bg-red-900/50 border border-red-700 text-red-400 text-sm">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="w-8 h-8 border-4 border-gavioes-yellow border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <ul className="divide-y divide-gavioes-white/10 rounded-lg border border-gavioes-white/10 bg-gavioes-dark-gray">
          {equipes.map((equipe) => (
            <li key={equipe.id} className="flex flex-wrap items-center gap-3 px-4 py-3">
              {editingId === equipe.id ? (
                <>
                  <input
                    type="text"
                    value={editNome}
                    onChange={(e) => setEditNome(e.target.value)}
                    maxLength={80}
                    autoFocus
                    className="flex-1 min-w-[160px] px-2 py-1 rounded bg-gavioes-black border border-gavioes-white/20 text-gavioes-white focus:border-gavioes-yellow focus:outline-none"
                  />
                  <button
                    onClick={() => handleSave(equipe.id)}
                    disabled={updateMutation.isPending}
                    className="px-3 py-1 rounded text-sm bg-gavioes-yellow text-gavioes-black font-medium"
                  >
                    Salvar
                  </button>
                  <button
                    onClick={() => setEditingId(null)}
                    className="px-3 py-1 rounded text-sm text-gavioes-white/70 hover:text-gavioes-white"
                  >
                    Cancelar
                  </button>
                </>
              ) : (
                <>
                  <span className="flex-1 font-medium">
                    {equipe.nome}
                    {equipe.id === equipeId && (
                      <span className="ml-2 text-xs px-2 py-0.5 rounded-full bg-gavioes-yellow/20 text-gavioes-yellow">
                        selecionado
                      </span>
                    )}
                  </span>
                  {equipe.id !== equipeId && (
                    <button
                      onClick={() => setSelectedEquipeId(equipe.id)}
                      className="px-3 py-1 rounded text-sm border border-gavioes-yellow/40 text-gavioes-yellow hover:bg-gavioes-yellow/10"
                    >
                      Selecionar
                    </button>
                  )}
                  <button
                    onClick={() => {
                      setEditingId(equipe.id);
                      setEditNome(equipe.nome);
                    }}
                    className="px-3 py-1 rounded text-sm text-gavioes-white/70 hover:text-gavioes-yellow"
                  >
                    Renomear
                  </button>
                  <button
                    onClick={() => setDeleting(equipe)}
                    disabled={equipes.length <= 1}
                    title={equipes.length <= 1 ? 'É preciso ter pelo menos um time' : undefined}
                    className="px-3 py-1 rounded text-sm text-red-400 hover:text-red-300 disabled:opacity-30"
                  >
                    Excluir
                  </button>
                </>
              )}
            </li>
          ))}
        </ul>
      )}

      <ConfirmDialog
        open={!!deleting}
        title="Excluir time"
        confirmLabel="Excluir"
        message={`Excluir o time "${deleting?.nome}"? Só é possível excluir times sem jogadores e sem sorteios.`}
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  );
}

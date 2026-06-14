import { useState, useEffect } from 'react';
import type { Player, Position, PlayerCreateRequest, PlayerUpdateRequest } from '../types';
import {
  usePlayersQuery,
  useCreatePlayer,
  useUpdatePlayer,
  useDeletePlayer,
  useTogglePlayerStatus,
} from '../hooks/usePlayersQuery';
import { PlayerForm } from '../components/PlayerForm';
import { ConfirmDialog } from '../components/ConfirmDialog';

const POSITION_LABELS: Record<Position, string> = {
  GOLEIRO: 'Goleiro',
  ZAGUEIRO: 'Zagueiro',
  MEIO: 'Meio',
  ATACANTE: 'Atacante',
};

export function PlayersPage() {
  const [page, setPage] = useState(0);
  const [filterPosicao, setFilterPosicao] = useState<Position | ''>('');
  const [filterAtivo, setFilterAtivo] = useState<boolean | ''>('');
  const [filterNome, setFilterNome] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingPlayer, setEditingPlayer] = useState<Player | null>(null);
  const [deletingPlayer, setDeletingPlayer] = useState<Player | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Auto-dismiss success message after 3s
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const { data, isLoading, isError } = usePlayersQuery({
    posicao: filterPosicao || undefined,
    ativo: filterAtivo !== '' ? filterAtivo : undefined,
    page,
    size: 20,
  });

  const createMutation = useCreatePlayer();
  const updateMutation = useUpdatePlayer();
  const deleteMutation = useDeletePlayer();
  const toggleMutation = useTogglePlayerStatus();

  function handleCreate(formData: PlayerCreateRequest | PlayerUpdateRequest) {
    createMutation.mutate(formData as PlayerCreateRequest, {
      onSuccess: () => {
        setShowForm(false);
        setSuccessMessage('Jogador criado com sucesso!');
      },
    });
  }

  function handleUpdate(formData: PlayerCreateRequest | PlayerUpdateRequest) {
    if (!editingPlayer) return;
    updateMutation.mutate(
      { id: editingPlayer.id, data: formData as PlayerUpdateRequest },
      { onSuccess: () => setEditingPlayer(null) },
    );
  }

  function handleDelete() {
    if (!deletingPlayer) return;
    deleteMutation.mutate(deletingPlayer.id, {
      onSuccess: () => {
        setDeletingPlayer(null);
        setSuccessMessage('Jogador excluído com sucesso!');
      },
      onError: (error: any) => {
        setDeletingPlayer(null);
        const message = error?.response?.data?.mensagem || 'Não é possível excluir este jogador pois ele está associado a sorteios anteriores.';
        setErrorMessage(message);
      },
    });
  }

  function handleToggleStatus(id: number) {
    toggleMutation.mutate(id);
  }

  // Client-side name filter
  const filteredContent = data?.content.filter((player) =>
    player.nome.toLowerCase().includes(filterNome.toLowerCase())
  ) ?? [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-title text-gavioes-yellow">Jogadores</h1>
        <button
          onClick={() => setShowForm(true)}
          className="px-4 py-2 rounded bg-gavioes-yellow text-gavioes-black font-body font-semibold hover:bg-gavioes-yellow/90 transition-colors"
        >
          + Novo Jogador
        </button>
      </div>

      {/* Success message */}
      {successMessage && (
        <div className="mb-4 px-4 py-2 rounded-lg bg-green-900/50 border border-green-700 text-green-400 text-sm font-medium">
          {successMessage}
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6 items-end">
        <div className="w-[720px]">
          <label htmlFor="filter-nome" className="block text-sm text-gavioes-white/60 font-body mb-1">
            Buscar por nome
          </label>
          <input
            id="filter-nome"
            type="text"
            value={filterNome}
            onChange={(e) => setFilterNome(e.target.value)}
            placeholder="Nome..."
            className="w-full px-3 py-2 rounded bg-gavioes-dark-gray border border-gavioes-white/20 text-gavioes-white font-body focus:border-gavioes-yellow focus:outline-none"
          />
        </div>
        <div className="w-44">
          <label htmlFor="filter-posicao" className="block text-sm text-gavioes-white/60 font-body mb-1">
            Posição
          </label>
          <select
            id="filter-posicao"
            value={filterPosicao}
            onChange={(e) => {
              setFilterPosicao(e.target.value as Position | '');
              setPage(0);
            }}
            className="w-full px-3 py-2 rounded bg-gavioes-dark-gray border border-gavioes-white/20 text-gavioes-white font-body focus:border-gavioes-yellow focus:outline-none"
          >
            <option value="">Todas</option>
            <option value="GOLEIRO">Goleiro</option>
            <option value="ZAGUEIRO">Zagueiro</option>
            <option value="MEIO">Meio</option>
            <option value="ATACANTE">Atacante</option>
          </select>
        </div>
        <div className="w-44">
          <label htmlFor="filter-ativo" className="block text-sm text-gavioes-white/60 font-body mb-1">
            Status
          </label>
          <select
            id="filter-ativo"
            value={filterAtivo === '' ? '' : String(filterAtivo)}
            onChange={(e) => {
              const val = e.target.value;
              setFilterAtivo(val === '' ? '' : val === 'true');
              setPage(0);
            }}
            className="w-full px-3 py-2 rounded bg-gavioes-dark-gray border border-gavioes-white/20 text-gavioes-white font-body focus:border-gavioes-yellow focus:outline-none"
          >
            <option value="">Todos</option>
            <option value="true">Ativo</option>
            <option value="false">Inativo</option>
          </select>
        </div>
      </div>

      {/* Table */}
      {isLoading && (
        <p className="text-gavioes-white/60 font-body">Carregando...</p>
      )}

      {isError && (
        <p className="text-red-400 font-body">Erro ao carregar jogadores.</p>
      )}

      {data && (
        <>
          <div className="overflow-x-auto">
            <table className="w-full text-left font-body">
              <thead>
                <tr className="border-b border-gavioes-white/10">
                  <th className="py-3 px-4 text-gavioes-white/60 text-sm font-medium w-[30%]">Nome</th>
                  <th className="py-3 px-4 text-gavioes-white/60 text-sm font-medium">Posição</th>
                  <th className="py-3 px-4 text-gavioes-white/60 text-sm font-medium">Nível</th>
                  <th className="py-3 px-4 text-gavioes-white/60 text-sm font-medium">Status</th>
                  <th className="py-3 px-4 text-gavioes-white/60 text-sm font-medium">Ações</th>
                </tr>
              </thead>
              <tbody>
                {filteredContent.length === 0 && (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-gavioes-white/40">
                      Nenhum jogador encontrado.
                    </td>
                  </tr>
                )}
                {filteredContent.map((player) => (
                  <tr
                    key={player.id}
                    className="border-b border-gavioes-white/5 hover:bg-gavioes-white/5 transition-colors"
                  >
                    <td className="py-3 px-4 text-gavioes-white">{player.nome}</td>
                    <td className="py-3 px-4 text-gavioes-white/70">
                      {POSITION_LABELS[player.posicao]}
                    </td>
                    <td className="py-3 px-4 text-gavioes-white/70">{player.nivel}</td>
                    <td className="py-3 px-4">
                      <button
                        type="button"
                        role="switch"
                        aria-checked={player.ativo}
                        aria-label={`Alternar status de ${player.nome}`}
                        onClick={() => handleToggleStatus(player.id)}
                        disabled={toggleMutation.isPending}
                        className={`relative w-11 h-6 rounded-full transition-colors ${
                          player.ativo ? 'bg-gavioes-yellow' : 'bg-gavioes-white/20'
                        }`}
                      >
                        <span
                          className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white transition-transform ${
                            player.ativo ? 'translate-x-5' : 'translate-x-0'
                          }`}
                        />
                      </button>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex gap-2">
                        <button
                          onClick={() => setEditingPlayer(player)}
                          className="px-3 py-1 text-sm rounded bg-gavioes-white/10 text-gavioes-white hover:bg-gavioes-white/20 transition-colors"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => setDeletingPlayer(player)}
                          className="px-3 py-1 text-sm rounded bg-red-600/20 text-red-400 hover:bg-red-600/30 transition-colors"
                        >
                          Excluir
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {data.totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <span className="text-sm text-gavioes-white/60 font-body">
                Página {data.page + 1} de {data.totalPages} ({data.totalElements} jogadores)
              </span>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-3 py-1 rounded bg-gavioes-white/10 text-gavioes-white hover:bg-gavioes-white/20 disabled:opacity-30 disabled:cursor-not-allowed transition-colors font-body text-sm"
                >
                  Anterior
                </button>
                <button
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= data.totalPages - 1}
                  className="px-3 py-1 rounded bg-gavioes-white/10 text-gavioes-white hover:bg-gavioes-white/20 disabled:opacity-30 disabled:cursor-not-allowed transition-colors font-body text-sm"
                >
                  Próxima
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Create Form Modal */}
      {showForm && (
        <PlayerForm
          onSubmit={handleCreate}
          onCancel={() => setShowForm(false)}
          isLoading={createMutation.isPending}
        />
      )}

      {/* Edit Form Modal */}
      {editingPlayer && (
        <PlayerForm
          player={editingPlayer}
          onSubmit={handleUpdate}
          onCancel={() => setEditingPlayer(null)}
          isLoading={updateMutation.isPending}
        />
      )}

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={!!deletingPlayer}
        title="Excluir Jogador"
        message={`Tem certeza que deseja excluir o jogador "${deletingPlayer?.nome}"? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        cancelLabel="Cancelar"
        onConfirm={handleDelete}
        onCancel={() => setDeletingPlayer(null)}
      />

      {/* Error Modal */}
      {errorMessage && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-gavioes-dark-gray border border-red-700 rounded-xl p-6 max-w-sm w-full mx-4 shadow-2xl">
            <h3 className="text-lg font-title text-red-400 mb-3">Ação não permitida</h3>
            <p className="text-sm text-gavioes-white/80 mb-5">{errorMessage}</p>
            <button
              onClick={() => setErrorMessage(null)}
              className="w-full px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors"
            >
              Entendi
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

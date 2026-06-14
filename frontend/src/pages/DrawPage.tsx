import { useState } from 'react';
import { usePlayersQuery } from '../hooks/usePlayersQuery';
import { useDrawMutation, useUpdateTeamsMutation } from '../hooks/useDrawMutation';
import { useDrawStore } from '../stores/drawStore';
import { PlayerCard } from '../components/PlayerCard';
import { FootballField } from '../components/FootballField';
import { ShareImagePanel } from '../components/ShareImagePanel';

export function DrawPage() {
  const { data: playersData, isLoading } = usePlayersQuery({ ativo: true, size: 100 });
  const drawMutation = useDrawMutation();
  const updateTeamsMutation = useUpdateTeamsMutation();

  const {
    selectedPlayerIds,
    selectPlayer,
    deselectPlayer,
    currentDraw,
    setDraw,
    isAnimating,
    setIsAnimating,
    isEditing,
    editedAmarelo,
    editedPreto,
    editedReservas,
    swapPlayers,
    swapWithReserve,
    resetEdits,
    swapFrom,
    setSwapFrom,
    setSwapTo,
    calculateAverages,
    clearSelection,
  } = useDrawStore();

  const [showResults, setShowResults] = useState(false);
  const [swapMode, setSwapMode] = useState(false);

  const activePlayers = playersData?.content ?? [];

  function handleTogglePlayer(id: number) {
    if (selectedPlayerIds.includes(id)) {
      deselectPlayer(id);
    } else {
      selectPlayer(id);
    }
  }

  function handleSelectAll() {
    activePlayers.forEach((p) => selectPlayer(p.id));
  }

  function handleDeselectAll() {
    clearSelection();
  }

  async function handleDraw() {
    if (selectedPlayerIds.length < 2) return;

    setIsAnimating(true);
    setShowResults(false);

    // 500ms shuffle animation
    await new Promise((resolve) => setTimeout(resolve, 500));

    try {
      const result = await drawMutation.mutateAsync({ jogadorIds: selectedPlayerIds });
      setDraw(result);
      setShowResults(true);
    } catch {
      // Error handled by React Query
    } finally {
      setIsAnimating(false);
    }
  }

  async function handleReDraw() {
    setIsAnimating(true);
    setShowResults(false);

    await new Promise((resolve) => setTimeout(resolve, 500));

    try {
      const result = await drawMutation.mutateAsync({ jogadorIds: selectedPlayerIds });
      setDraw(result);
      setShowResults(true);
    } catch {
      // Error handled by React Query
    } finally {
      setIsAnimating(false);
    }
  }

  function handlePlayerClickForSwap(player: { id: number }) {
    if (!swapMode) return;

    const inAmarelo = editedAmarelo.some((p) => p.id === player.id);
    const inPreto = editedPreto.some((p) => p.id === player.id);
    const inReservas = editedReservas.some((p) => p.id === player.id);

    if (!inAmarelo && !inPreto && !inReservas) return;

    const findPlayer = () =>
      editedAmarelo.find(p => p.id === player.id) ||
      editedPreto.find(p => p.id === player.id) ||
      editedReservas.find(p => p.id === player.id) ||
      null;

    if (!swapFrom) {
      setSwapFrom(findPlayer());
    } else {
      const fromInAmarelo = editedAmarelo.some((p) => p.id === swapFrom.id);
      const fromInPreto = editedPreto.some((p) => p.id === swapFrom.id);
      const fromInReservas = editedReservas.some((p) => p.id === swapFrom.id);

      const targetPlayer = findPlayer();
      if (!targetPlayer) return;

      // Case 1: Both in teams (different teams) → normal swap
      if ((fromInAmarelo && inPreto) || (fromInPreto && inAmarelo)) {
        swapPlayers(swapFrom, targetPlayer);
        setSwapMode(false);
      }
      // Case 2: One in team, other in reserves → swap with reserve
      else if ((fromInAmarelo || fromInPreto) && inReservas) {
        swapWithReserve(swapFrom, targetPlayer);
        setSwapMode(false);
      } else if (fromInReservas && (inAmarelo || inPreto)) {
        swapWithReserve(targetPlayer, swapFrom);
        setSwapMode(false);
      }
      // Case 3: Same team or both reserves → change selection
      else {
        setSwapFrom(targetPlayer);
      }
    }
  }

  async function handleSaveChanges() {
    if (!currentDraw) return;

    try {
      const result = await updateTeamsMutation.mutateAsync({
        drawId: currentDraw.id,
        data: {
          timeAmarelo: editedAmarelo.map((p) => p.id),
          timePreto: editedPreto.map((p) => p.id),
          reserva: editedReservas?.[0]?.id,
        },
      });
      setDraw(result);
    } catch {
      // Error handled by React Query
    }
  }

  function handleBackToSelection() {
    setShowResults(false);
  }

  // Calculate current averages
  const averages = showResults ? calculateAverages() : null;

  // Animation overlay
  if (isAnimating) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <div className="animate-shuffle">
          <svg
            className="w-24 h-24 text-gavioes-yellow animate-spin"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
        </div>
        <p className="mt-4 text-xl font-title text-gavioes-yellow animate-pulse">
          Sorteando...
        </p>
      </div>
    );
  }

  // Results view
  if (showResults && currentDraw && averages) {
    return (
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-title text-gavioes-yellow">Resultado do Sorteio</h1>
            <p className="text-sm text-gavioes-white/60 mt-1">
              {new Date(currentDraw.dataHora).toLocaleString('pt-BR')}
            </p>
          </div>
          <button
            onClick={handleBackToSelection}
            className="px-4 py-2 text-sm border border-gavioes-white/30 rounded-lg text-gavioes-white/70 hover:bg-gavioes-white/5 transition-colors"
          >
            ← Voltar
          </button>
        </div>

        {/* Football field with both teams */}
        <FootballField
          timeAmarelo={editedAmarelo}
          timePreto={editedPreto}
          reservas={editedReservas}
          mediaAmarelo={averages.mediaAmarelo}
          mediaPreto={averages.mediaPreto}
          equilibrado={averages.equilibrado}
          swapMode={swapMode}
          selectedPlayerId={swapFrom?.id ?? null}
          onPlayerClick={handlePlayerClickForSwap}
        />

        {/* All Action Buttons inline */}
        <div className="flex gap-2 items-center flex-wrap">
          <button
            onClick={() => {
              setSwapMode(!swapMode);
              setSwapFrom(null);
              setSwapTo(null);
            }}
            className={`shrink-0 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              swapMode
                ? 'bg-gavioes-yellow text-gavioes-black'
                : 'bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10'
            }`}
          >
            {swapMode
              ? swapFrom
                ? 'Selecione o 2º'
                : 'Selecione o 1º'
              : 'Trocar'}
          </button>

          <button
            onClick={handleReDraw}
            disabled={drawMutation.isPending}
            className="shrink-0 px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors disabled:opacity-50"
          >
            Re-sortear
          </button>

          <ShareImagePanel
            timeAmarelo={editedAmarelo}
            timePreto={editedPreto}
            mediaAmarelo={averages.mediaAmarelo}
            mediaPreto={averages.mediaPreto}
            dataHora={currentDraw.dataHora}
          />

          {isEditing && (
            <>
              <button
                onClick={resetEdits}
                className="shrink-0 px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors"
              >
                Desfazer
              </button>
              <button
                onClick={handleSaveChanges}
                disabled={updateTeamsMutation.isPending}
                className="shrink-0 px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-yellow text-gavioes-black hover:bg-gavioes-yellow/90 transition-colors disabled:opacity-50"
              >
                {updateTeamsMutation.isPending ? 'Salvando...' : 'Salvar'}
              </button>
            </>
          )}
        </div>

        {/* Error messages */}
        {drawMutation.isError && (
          <p className="text-red-400 text-sm">Erro ao realizar o sorteio. Tente novamente.</p>
        )}
        {updateTeamsMutation.isError && (
          <p className="text-red-400 text-sm">Erro ao salvar alterações. Tente novamente.</p>
        )}
      </div>
    );
  }

  // Selection view
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-title text-gavioes-yellow">Sorteio</h1>
          <p className="text-sm text-gavioes-white/60 mt-1">
            Selecione os jogadores para o sorteio
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm text-gavioes-white/60">
            {selectedPlayerIds.length} selecionado{selectedPlayerIds.length !== 1 ? 's' : ''}
          </span>
        </div>
      </div>

      {/* Quick actions */}
      <div className="flex gap-3">
        <button
          onClick={handleSelectAll}
          className="px-3 py-1.5 text-sm rounded border border-gavioes-white/30 text-gavioes-white/70 hover:bg-gavioes-white/5 transition-colors"
        >
          Selecionar todos
        </button>
        <button
          onClick={handleDeselectAll}
          className="px-3 py-1.5 text-sm rounded border border-gavioes-white/30 text-gavioes-white/70 hover:bg-gavioes-white/5 transition-colors"
        >
          Limpar seleção
        </button>
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gavioes-yellow" />
        </div>
      )}

      {/* Player Grid */}
      {!isLoading && (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
          {activePlayers.map((player) => (
            <PlayerCard
              key={player.id}
              player={player}
              selected={selectedPlayerIds.includes(player.id)}
              onToggle={handleTogglePlayer}
            />
          ))}
        </div>
      )}

      {/* Empty state */}
      {!isLoading && activePlayers.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gavioes-white/60">Nenhum jogador ativo encontrado.</p>
          <p className="text-sm text-gavioes-white/40 mt-1">
            Adicione jogadores na página de Jogadores.
          </p>
        </div>
      )}

      {/* Draw button */}
      <div className="flex justify-center pt-4">
        <button
          onClick={handleDraw}
          disabled={selectedPlayerIds.length < 2 || drawMutation.isPending}
          className="px-8 py-3 bg-gavioes-yellow text-gavioes-black font-title text-xl rounded-lg hover:bg-gavioes-yellow/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          ⚽ Sortear
        </button>
      </div>

      {selectedPlayerIds.length > 0 && selectedPlayerIds.length < 2 && (
        <p className="text-center text-sm text-gavioes-white/50">
          Selecione pelo menos 2 jogadores para sortear.
        </p>
      )}

      {drawMutation.isError && (
        <p className="text-center text-red-400 text-sm">
          Erro ao realizar o sorteio. Tente novamente.
        </p>
      )}
    </div>
  );
}

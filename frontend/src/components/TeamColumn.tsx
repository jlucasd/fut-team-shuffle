import { useState } from 'react';
import type { Player } from '../types';

const positionLabels: Record<string, string> = {
  GOLEIRO: 'GOL',
  ZAGUEIRO: 'ZAG',
  MEIO: 'MEI',
  ATACANTE: 'ATA',
};

interface TeamColumnProps {
  team: 'amarelo' | 'preto';
  players: Player[];
  average: number;
  onDropPlayer?: (player: Player, fromTeam: 'amarelo' | 'preto') => void;
  onPlayerClick?: (player: Player) => void;
  selectedPlayerId?: number | null;
}

export function TeamColumn({
  team,
  players,
  average,
  onDropPlayer,
  onPlayerClick,
  selectedPlayerId,
}: TeamColumnProps) {
  const [isDragOver, setIsDragOver] = useState(false);

  const isYellow = team === 'amarelo';
  const teamName = isYellow ? 'AMARELO' : 'PRETO';
  const badgeClasses = isYellow
    ? 'bg-gavioes-yellow text-gavioes-black'
    : 'bg-gavioes-black text-gavioes-white border border-gavioes-white';
  const borderColor = isYellow ? 'border-gavioes-yellow' : 'border-gavioes-white';

  function handleDragStart(e: React.DragEvent, player: Player) {
    e.dataTransfer.setData('application/json', JSON.stringify({ player, fromTeam: team }));
    e.dataTransfer.effectAllowed = 'move';
  }

  function handleDragOver(e: React.DragEvent) {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setIsDragOver(true);
  }

  function handleDragLeave() {
    setIsDragOver(false);
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setIsDragOver(false);

    try {
      const data = JSON.parse(e.dataTransfer.getData('application/json'));
      const { player, fromTeam } = data as { player: Player; fromTeam: 'amarelo' | 'preto' };
      if (fromTeam !== team && onDropPlayer) {
        onDropPlayer(player, fromTeam);
      }
    } catch {
      // ignore invalid drop data
    }
  }

  return (
    <div
      className={`flex-1 rounded-lg border-2 ${borderColor} ${
        isDragOver ? 'bg-gavioes-white/5' : 'bg-gavioes-dark-gray'
      } transition-colors duration-200 p-4`}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {/* Team Header */}
      <div className="flex items-center justify-between mb-4">
        <span className={`px-3 py-1 rounded font-title text-lg ${badgeClasses}`}>
          {teamName}
        </span>
        <span className="text-sm text-gavioes-white/60">
          Média: <span className="font-semibold text-gavioes-white">{average.toFixed(2)}</span>
        </span>
      </div>

      {/* Player List */}
      <ul className="space-y-2">
        {players.map((player) => (
          <li
            key={player.id}
            draggable
            onDragStart={(e) => handleDragStart(e, player)}
            onClick={() => onPlayerClick?.(player)}
            className={`
              flex items-center gap-3 p-3 rounded-md cursor-grab active:cursor-grabbing
              transition-all duration-150
              ${
                selectedPlayerId === player.id
                  ? 'bg-gavioes-yellow/20 ring-2 ring-gavioes-yellow'
                  : 'bg-gavioes-black/40 hover:bg-gavioes-white/5'
              }
            `}
          >
            <span className="text-xs font-semibold px-1.5 py-0.5 rounded bg-gavioes-white/10 text-gavioes-white/70 min-w-[36px] text-center">
              {positionLabels[player.posicao] || player.posicao}
            </span>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gavioes-white truncate">
                {player.nome}
              </p>
            </div>
            <span className="flex items-center gap-0.5">
              {Array.from({ length: 5 }, (_, i) => (
                <span
                  key={i}
                  className={`w-1.5 h-1.5 rounded-full ${
                    i < player.nivel
                      ? isYellow
                        ? 'bg-gavioes-yellow'
                        : 'bg-gavioes-white'
                      : 'bg-gavioes-white/20'
                  }`}
                />
              ))}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

import type { Player } from '../types';

const positionLabels: Record<string, string> = {
  GOLEIRO: 'GOL',
  ZAGUEIRO: 'ZAG',
  MEIO: 'MEI',
  ATACANTE: 'ATA',
};

interface PlayerCardProps {
  player: Player;
  selected: boolean;
  onToggle: (id: number) => void;
}

export function PlayerCard({ player, selected, onToggle }: PlayerCardProps) {
  return (
    <button
      type="button"
      onClick={() => onToggle(player.id)}
      className={`
        relative w-full p-4 rounded-lg border-2 transition-all duration-200 text-left
        ${
          selected
            ? 'border-gavioes-yellow bg-gavioes-yellow/10 shadow-lg shadow-gavioes-yellow/20'
            : 'border-gavioes-dark-gray bg-gavioes-dark-gray hover:border-gavioes-white/30'
        }
      `}
    >
      {selected && (
        <span className="absolute top-2 right-2 w-5 h-5 bg-gavioes-yellow rounded-full flex items-center justify-center">
          <svg className="w-3 h-3 text-gavioes-black" fill="currentColor" viewBox="0 0 20 20">
            <path
              fillRule="evenodd"
              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
              clipRule="evenodd"
            />
          </svg>
        </span>
      )}

      <div className="flex items-center gap-3">
        <span className="text-xs font-semibold px-2 py-0.5 rounded bg-gavioes-white/10 text-gavioes-white/70">
          {positionLabels[player.posicao] || player.posicao}
        </span>
        <span className="flex items-center gap-1">
          {Array.from({ length: 5 }, (_, i) => (
            <span
              key={i}
              className={`w-2 h-2 rounded-full ${
                i < player.nivel ? 'bg-gavioes-yellow' : 'bg-gavioes-white/20'
              }`}
            />
          ))}
        </span>
      </div>

      <p className="mt-2 font-semibold text-gavioes-white truncate">{player.nome}</p>
    </button>
  );
}

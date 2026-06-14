import type { Player } from '../types';

interface FootballFieldProps {
  timeAmarelo: Player[];
  timePreto: Player[];
  reservas: Player[];
  mediaAmarelo: number;
  mediaPreto: number;
  equilibrado: boolean;
  swapMode?: boolean;
  selectedPlayerId?: number | null;
  onPlayerClick?: (player: Player) => void;
}

export function FootballField({ timeAmarelo, timePreto, reservas, mediaAmarelo, mediaPreto, equilibrado, swapMode, selectedPlayerId, onPlayerClick }: FootballFieldProps) {
  // Ordered numbering: GK=1, ZAG=2..., MEI=..., ATA=...
  const amareloOrdered = [
    ...timeAmarelo.filter(p => p.posicao === 'GOLEIRO'),
    ...timeAmarelo.filter(p => p.posicao === 'ZAGUEIRO'),
    ...timeAmarelo.filter(p => p.posicao === 'MEIO'),
    ...timeAmarelo.filter(p => p.posicao === 'ATACANTE'),
  ];
  const pretoOrdered = [
    ...timePreto.filter(p => p.posicao === 'GOLEIRO'),
    ...timePreto.filter(p => p.posicao === 'ZAGUEIRO'),
    ...timePreto.filter(p => p.posicao === 'MEIO'),
    ...timePreto.filter(p => p.posicao === 'ATACANTE'),
  ];

  const amareloNumberMap = new Map<number, number>();
  amareloOrdered.forEach((p, i) => amareloNumberMap.set(p.id, i + 1));
  const pretoNumberMap = new Map<number, number>();
  pretoOrdered.forEach((p, i) => pretoNumberMap.set(p.id, i + 1));

  // Reserves split
  const reservasAmarelo = reservas.filter((_, i) => i % 2 === 0);
  const reservasPreto = reservas.filter((_, i) => i % 2 !== 0);

  // Position groups
  const amareloGK = timeAmarelo.filter(p => p.posicao === 'GOLEIRO');
  const amareloZAG = timeAmarelo.filter(p => p.posicao === 'ZAGUEIRO');
  const amareloMEI = timeAmarelo.filter(p => p.posicao === 'MEIO');
  const amareloATA = timeAmarelo.filter(p => p.posicao === 'ATACANTE');
  const pretoGK = timePreto.filter(p => p.posicao === 'GOLEIRO');
  const pretoZAG = timePreto.filter(p => p.posicao === 'ZAGUEIRO');
  const pretoMEI = timePreto.filter(p => p.posicao === 'MEIO');
  const pretoATA = timePreto.filter(p => p.posicao === 'ATACANTE');

  function JerseyIcon({ variant }: { variant: 'amarelo' | 'preto' }) {
    const fill = variant === 'amarelo' ? '#FACC15' : '#1a1a1a';
    const stroke = variant === 'amarelo' ? '#CA8A04' : '#ffffff';
    return (
      <svg viewBox="0 0 40 44" className="w-10 h-11 sm:w-12 sm:h-13">
        <path d="M8 6 L4 12 L8 14 L8 40 L32 40 L32 14 L36 12 L32 6 L26 2 L20 6 L14 2 L8 6 Z" fill={fill} stroke={stroke} strokeWidth="1.5" />
        <path d="M16 2 Q20 5 24 2" fill="none" stroke={stroke} strokeWidth="1" />
      </svg>
    );
  }

  function Shirt({ player, number, variant }: { player: Player; number: number; variant: 'amarelo' | 'preto' }) {
    const textColor = variant === 'amarelo' ? 'text-black' : 'text-white';
    const isSelected = swapMode && selectedPlayerId === player.id;
    const clickable = swapMode ? 'cursor-pointer hover:scale-110 transition-transform' : '';
    const ring = isSelected ? 'ring-2 ring-white ring-offset-2 ring-offset-green-700 rounded-lg' : '';
    return (
      <div className={`flex flex-col items-center gap-0 ${clickable} ${ring} p-0.5`} onClick={() => swapMode && onPlayerClick?.(player)}>
        <div className="relative">
          <JerseyIcon variant={variant} />
          <span className={`absolute inset-0 flex items-center justify-center font-bold text-xs sm:text-sm ${textColor} pt-2`}>{number}</span>
        </div>
        <span className="text-[9px] sm:text-[11px] text-white font-semibold text-center leading-tight max-w-[80px] truncate drop-shadow-md">{player.nome}</span>
      </div>
    );
  }

  // Row component positioned absolutely at a given vertical %
  function Row({ players, variant, numberMap, top }: { players: Player[]; variant: 'amarelo' | 'preto'; numberMap: Map<number, number>; top: string }) {
    if (players.length === 0) return null;
    return (
      <div className="absolute left-0 right-0 flex items-center justify-center gap-2 sm:gap-3 flex-wrap px-10 sm:px-14" style={{ top, transform: 'translateY(-50%)' }}>
        {players.map(p => <Shirt key={p.id} player={p} number={numberMap.get(p.id) || 0} variant={variant} />)}
      </div>
    );
  }

  return (
    <div className="w-full max-w-lg mx-auto space-y-3">
      {/* Top info bar */}
      <div className="flex items-center justify-between px-2">
        <div className="flex items-center gap-3">
          <span className={`px-2 py-0.5 rounded text-xs font-semibold ${equilibrado ? 'bg-green-900/60 text-green-400 border border-green-700' : 'bg-red-900/60 text-red-400 border border-red-700'}`}>
            {equilibrado ? 'Equilibrado' : 'Desequilibrado'}
          </span>
          <span className="text-xs text-gavioes-white/60">Δ {Math.abs(mediaAmarelo - mediaPreto).toFixed(2)}</span>
        </div>
        <div className="text-xs text-gavioes-white/80">
          <span className="text-yellow-400 font-bold">{mediaAmarelo.toFixed(2)}</span>
          <span className="mx-1 text-gavioes-white/40">vs</span>
          <span className="text-white font-bold">{mediaPreto.toFixed(2)}</span>
        </div>
      </div>

      {/* The field */}
      <div className="relative bg-green-700 rounded-xl border-4 border-green-900 overflow-hidden shadow-2xl aspect-[2/3]"
        style={{ backgroundImage: 'repeating-linear-gradient(0deg, transparent, transparent 28px, rgba(255,255,255,0.03) 28px, rgba(255,255,255,0.03) 30px)' }}>

        {/* Field markings */}
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute inset-2 sm:inset-3 border border-white/30 rounded" />
          {/* Center line */}
          <div className="absolute top-1/2 left-2 right-2 sm:left-3 sm:right-3 h-px bg-white/40" />
          {/* Center circle */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-20 h-20 sm:w-24 sm:h-24 rounded-full border border-white/30" />
          {/* Center dot */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-2 h-2 rounded-full bg-white/50" />
          {/* Top penalty area */}
          <div className="absolute top-[2%] left-1/2 -translate-x-1/2 w-[55%] h-[15%] border-b border-l border-r border-white/30" />
          {/* Bottom penalty area */}
          <div className="absolute bottom-[2%] left-1/2 -translate-x-1/2 w-[55%] h-[15%] border-t border-l border-r border-white/30" />
        </div>

        {/* Team labels on the left */}
        <div className="absolute top-[25%] left-1 sm:left-2 -translate-y-1/2 z-10">
          <span className="font-title text-lg sm:text-xl text-black tracking-widest font-bold drop-shadow-[0_1px_2px_rgba(255,255,255,0.6)]" style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)' }}>PRETO</span>
        </div>
        <div className="absolute top-[75%] left-1 sm:left-2 -translate-y-1/2 z-10">
          <span className="font-title text-lg sm:text-xl text-yellow-400 tracking-widest font-bold drop-shadow-lg" style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)' }}>AMARELO</span>
        </div>

        {/* Players positioned at fixed % from top — symmetric around 50% center */}
        {/* PRETO: GK at 8%, ZAG at 20%, MEI at 32%, ATA at 44% */}
        <Row players={pretoGK} variant="preto" numberMap={pretoNumberMap} top="8%" />
        <Row players={pretoZAG} variant="preto" numberMap={pretoNumberMap} top="22%" />
        <Row players={pretoMEI} variant="preto" numberMap={pretoNumberMap} top="35%" />
        <Row players={pretoATA} variant="preto" numberMap={pretoNumberMap} top="45%" />

        {/* AMARELO: ATA at 56%, MEI at 68%, ZAG at 80%, GK at 92% */}
        <Row players={amareloATA} variant="amarelo" numberMap={amareloNumberMap} top="55%" />
        <Row players={amareloMEI} variant="amarelo" numberMap={amareloNumberMap} top="65%" />
        <Row players={amareloZAG} variant="amarelo" numberMap={amareloNumberMap} top="78%" />
        <Row players={amareloGK} variant="amarelo" numberMap={amareloNumberMap} top="92%" />

        {/* Reserves — right side, positioned per team half */}
        {reservasPreto.length > 0 && (
          <div className="absolute top-[25%] right-1 sm:right-2 -translate-y-1/2 flex flex-col items-center gap-1 z-10">
            <span className="text-[7px] text-white/40 uppercase tracking-wider">Res.</span>
            {reservasPreto.map((p, i) => (
              <Shirt key={p.id} player={p} number={pretoOrdered.length + i + 1} variant="preto" />
            ))}
          </div>
        )}
        {reservasAmarelo.length > 0 && (
          <div className="absolute top-[75%] right-1 sm:right-2 -translate-y-1/2 flex flex-col items-center gap-1 z-10">
            <span className="text-[7px] text-white/40 uppercase tracking-wider">Res.</span>
            {reservasAmarelo.map((p, i) => (
              <Shirt key={p.id} player={p} number={amareloOrdered.length + i + 1} variant="amarelo" />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

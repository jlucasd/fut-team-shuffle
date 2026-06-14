import { useState } from 'react';
import { useDrawHistory } from '../hooks/useDrawHistory';
import type { DrawResult, Player } from '../types';

const positionLabels: Record<string, string> = {
  GOLEIRO: 'GOL',
  ZAGUEIRO: 'ZAG',
  MEIO: 'MEI',
  ATACANTE: 'ATA',
};

function DrawDetail({ draw, onBack }: { draw: DrawResult; onBack: () => void }) {
  // Split reserves into respective teams (even index → amarelo, odd → preto)
  const reservasAmarelo = draw.reservas?.filter((_, i) => i % 2 === 0) ?? [];
  const reservasPreto = draw.reservas?.filter((_, i) => i % 2 !== 0) ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-title text-gavioes-yellow">Detalhes do Sorteio</h2>
          <p className="text-sm text-gavioes-white/60 mt-1">
            {new Date(draw.dataHora).toLocaleString('pt-BR')}
          </p>
        </div>
        <button
          onClick={onBack}
          className="px-4 py-2 text-sm border border-gavioes-white/30 rounded-lg text-gavioes-white/70 hover:bg-gavioes-white/5 transition-colors"
        >
          ← Voltar
        </button>
      </div>

      {/* Balance info */}
      <div className="flex items-center gap-3">
        <span
          className={`px-3 py-1 rounded text-sm font-medium ${
            draw.equilibrado
              ? 'bg-green-900/50 text-green-400 border border-green-700'
              : 'bg-red-900/50 text-red-400 border border-red-700'
          }`}
        >
          {draw.equilibrado ? 'Equilibrado' : 'Desequilibrado'}
        </span>
        <span className="text-sm text-gavioes-white/60">
          Diferença: {Math.abs(draw.mediaAmarelo - draw.mediaPreto).toFixed(2)}
        </span>
      </div>

      {/* Teams */}
      <div className="flex gap-4 flex-col md:flex-row">
        {/* Yellow Team */}
        <div className="flex-1 rounded-lg border-2 border-gavioes-yellow bg-gavioes-dark-gray p-4">
          <div className="flex items-center justify-between mb-4">
            <span className="px-3 py-1 rounded font-title text-lg bg-gavioes-yellow text-gavioes-black">
              AMARELO
            </span>
            <span className="text-sm text-gavioes-white/60">
              Média: <span className="font-semibold text-gavioes-white">{draw.mediaAmarelo.toFixed(2)}</span>
            </span>
          </div>
          <ul className="space-y-2">
            {draw.timeAmarelo.map((player: Player) => (
              <li
                key={player.id}
                className="flex items-center gap-3 p-3 rounded-md bg-gavioes-black/40"
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
                        i < player.nivel ? 'bg-gavioes-yellow' : 'bg-gavioes-white/20'
                      }`}
                    />
                  ))}
                </span>
              </li>
            ))}
            {reservasAmarelo.length > 0 && (
              <>
                <li className="pt-2 pb-1">
                  <span className="text-xs text-gavioes-white/40 uppercase tracking-wider">Reservas</span>
                </li>
                {reservasAmarelo.map((player: Player) => (
                  <li
                    key={player.id}
                    className="flex items-center gap-3 p-3 rounded-md bg-gavioes-black/40 opacity-70"
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
                            i < player.nivel ? 'bg-gavioes-yellow' : 'bg-gavioes-white/20'
                          }`}
                        />
                      ))}
                    </span>
                  </li>
                ))}
              </>
            )}
          </ul>
        </div>

        {/* Black Team */}
        <div className="flex-1 rounded-lg border-2 border-gavioes-white bg-gavioes-dark-gray p-4">
          <div className="flex items-center justify-between mb-4">
            <span className="px-3 py-1 rounded font-title text-lg bg-gavioes-black text-gavioes-white border border-gavioes-white">
              PRETO
            </span>
            <span className="text-sm text-gavioes-white/60">
              Média: <span className="font-semibold text-gavioes-white">{draw.mediaPreto.toFixed(2)}</span>
            </span>
          </div>
          <ul className="space-y-2">
            {draw.timePreto.map((player: Player) => (
              <li
                key={player.id}
                className="flex items-center gap-3 p-3 rounded-md bg-gavioes-black/40"
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
                        i < player.nivel ? 'bg-gavioes-white' : 'bg-gavioes-white/20'
                      }`}
                    />
                  ))}
                </span>
              </li>
            ))}
            {reservasPreto.length > 0 && (
              <>
                <li className="pt-2 pb-1">
                  <span className="text-xs text-gavioes-white/40 uppercase tracking-wider">Reservas</span>
                </li>
                {reservasPreto.map((player: Player) => (
                  <li
                    key={player.id}
                    className="flex items-center gap-3 p-3 rounded-md bg-gavioes-black/40 opacity-70"
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
                            i < player.nivel ? 'bg-gavioes-white' : 'bg-gavioes-white/20'
                          }`}
                        />
                      ))}
                    </span>
                  </li>
                ))}
              </>
            )}
          </ul>
        </div>
      </div>
    </div>
  );
}

export function HistoryPage() {
  const [page, setPage] = useState(0);
  const [selectedDraw, setSelectedDraw] = useState<DrawResult | null>(null);
  const { data, isLoading, isError } = useDrawHistory(page, 20);

  if (selectedDraw) {
    return <DrawDetail draw={selectedDraw} onBack={() => setSelectedDraw(null)} />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-title text-gavioes-yellow">Histórico de Sorteios</h1>
        <p className="text-sm text-gavioes-white/60 mt-1">
          Consulte os sorteios anteriores
        </p>
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gavioes-yellow" />
        </div>
      )}

      {/* Error */}
      {isError && (
        <div className="text-center py-12">
          <p className="text-red-400">Erro ao carregar histórico. Tente novamente.</p>
        </div>
      )}

      {/* Draw List */}
      {data && data.content.length > 0 && (
        <div className="space-y-3">
          {data.content.map((draw) => {
            const total = draw.timeAmarelo.length + draw.timePreto.length + (draw.reservas?.length || 0);
            return (
              <button
                key={draw.id}
                onClick={() => setSelectedDraw(draw)}
                className="w-full text-left p-4 rounded-lg bg-gavioes-dark-gray border border-gavioes-white/10 hover:border-gavioes-yellow/50 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-gavioes-white font-medium">
                      {new Date(draw.dataHora).toLocaleString('pt-BR')}
                    </p>
                    <p className="text-sm text-gavioes-white/60 mt-1">
                      {total} jogadores
                      {' • '}
                      {Math.ceil(total / 2)} vs {Math.floor(total / 2)}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span
                      className={`px-2 py-0.5 rounded text-xs font-medium ${
                        draw.equilibrado
                          ? 'bg-green-900/50 text-green-400 border border-green-700'
                          : 'bg-red-900/50 text-red-400 border border-red-700'
                      }`}
                    >
                      {draw.equilibrado ? 'Equilibrado' : 'Desequilibrado'}
                    </span>
                    <span className="text-gavioes-white/40 text-sm">→</span>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      )}

      {/* Empty state */}
      {data && data.content.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gavioes-white/60">Nenhum sorteio realizado ainda.</p>
          <p className="text-sm text-gavioes-white/40 mt-1">
            Realize um sorteio na página de Sorteio.
          </p>
        </div>
      )}

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 pt-4">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-3 py-1.5 text-sm rounded border border-gavioes-white/30 text-gavioes-white/70 hover:bg-gavioes-white/5 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          >
            ← Anterior
          </button>
          <span className="text-sm text-gavioes-white/60">
            Página {page + 1} de {data.totalPages}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
            disabled={page >= data.totalPages - 1}
            className="px-3 py-1.5 text-sm rounded border border-gavioes-white/30 text-gavioes-white/70 hover:bg-gavioes-white/5 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          >
            Próxima →
          </button>
        </div>
      )}
    </div>
  );
}

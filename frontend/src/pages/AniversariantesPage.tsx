import { usePlayersQuery } from '../hooks/usePlayersQuery';
import { useBirthdayNotifications } from '../hooks/useBirthdayNotifications';
import {
  daysUntilBirthday,
  formatDayMonth,
  isBirthdayToday,
  isBirthdayTomorrow,
  sortByUpcomingBirthday,
} from '../lib/birthday';

function daysLabel(iso: string): string {
  if (isBirthdayToday(iso)) return 'Hoje! 🎉';
  if (isBirthdayTomorrow(iso)) return 'Amanhã 🎂';
  const days = daysUntilBirthday(iso);
  if (days == null) return '';
  return `em ${days} dias`;
}

export function AniversariantesPage() {
  const { data, isLoading, isError } = usePlayersQuery({ size: 100 });
  const { supported, permission, enabled, enable, disable } = useBirthdayNotifications();

  const players = data?.content ?? [];
  const withBirthday = sortByUpcomingBirthday(players);
  const withoutBirthday = players.filter((p) => !p.dataNascimento);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-title text-gavioes-yellow">Aniversariantes</h1>
          <p className="text-sm text-gavioes-white/60 mt-1">
            Datas de nascimento dos sócios do Gaviões FC
          </p>
        </div>

        {/* Notification control */}
        <div className="flex flex-col items-end gap-1">
          {!supported && (
            <span className="text-xs text-gavioes-white/50">
              Notificações não são suportadas neste navegador.
            </span>
          )}
          {supported && permission === 'denied' && (
            <span className="text-xs text-red-400 max-w-[240px] text-right">
              Notificações bloqueadas. Habilite nas configurações do navegador.
            </span>
          )}
          {supported && permission !== 'denied' && (
            enabled ? (
              <button
                onClick={disable}
                className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-yellow text-gavioes-black hover:bg-gavioes-yellow/90 transition-colors"
              >
                🔔 Notificações ativadas
              </button>
            ) : (
              <button
                onClick={enable}
                className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors"
              >
                🔕 Ativar notificações
              </button>
            )
          )}
          <span className="text-xs text-gavioes-white/40 max-w-[240px] text-right">
            Avisamos um dia antes de cada aniversário.
          </span>
        </div>
      </div>

      {isLoading && (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gavioes-yellow" />
        </div>
      )}

      {isError && (
        <p className="text-red-400 font-body">Erro ao carregar aniversariantes.</p>
      )}

      {data && (
        <>
          {withBirthday.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gavioes-white/60">Nenhuma data de nascimento cadastrada ainda.</p>
              <p className="text-sm text-gavioes-white/40 mt-1">
                Adicione a data de nascimento ao editar um jogador.
              </p>
            </div>
          )}

          {withBirthday.length > 0 && (
            <div className="space-y-2">
              {withBirthday.map((player) => {
                const iso = player.dataNascimento!;
                const soon = isBirthdayToday(iso) || isBirthdayTomorrow(iso);
                return (
                  <div
                    key={player.id}
                    className={`flex items-center justify-between p-4 rounded-lg border transition-colors ${
                      soon
                        ? 'bg-gavioes-yellow/10 border-gavioes-yellow/50'
                        : 'bg-gavioes-dark-gray border-gavioes-white/10'
                    }`}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <span className="text-2xl" aria-hidden="true">🎂</span>
                      <div className="min-w-0">
                        <p className="text-gavioes-white font-medium truncate">{player.nome}</p>
                        <p className="text-sm text-gavioes-white/60">{formatDayMonth(iso)}</p>
                      </div>
                    </div>
                    <span
                      className={`shrink-0 text-sm font-medium ${
                        soon ? 'text-gavioes-yellow' : 'text-gavioes-white/50'
                      }`}
                    >
                      {daysLabel(iso)}
                    </span>
                  </div>
                );
              })}
            </div>
          )}

          {withoutBirthday.length > 0 && (
            <p className="text-xs text-gavioes-white/40 pt-2">
              {withoutBirthday.length} jogador(es) sem data de nascimento cadastrada.
            </p>
          )}
        </>
      )}
    </div>
  );
}

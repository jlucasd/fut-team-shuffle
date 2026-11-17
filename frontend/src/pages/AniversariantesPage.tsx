import { useState } from 'react';
import { usePlayersQuery, useUpdatePlayer } from '../hooks/usePlayersQuery';
import { useBirthdayNotifications } from '../hooks/useBirthdayNotifications';
import { usePushNotifications, useWebPush } from '../hooks/usePushNotifications';
import { webPushConfigured } from '../lib/webPush';
import { useCurrentEquipe } from '../hooks/useEquipes';
import { sendTestNotification } from '../services/notificacaoService';
import type { Player } from '../types';
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

/** Browser notifications (web) — only fire while the site is open. */
function WebNotificationControl() {
  const { supported, permission, enabled, enable, disable } = useBirthdayNotifications();

  return (
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
  );
}

/** "Testar" button: asks the server to push to every registered device. */
function useTestNotification() {
  const [testMessage, setTestMessage] = useState<string | null>(null);

  async function handleTest() {
    setTestMessage(null);
    try {
      const { enviados } = await sendTestNotification();
      setTestMessage(enviados > 0 ? 'Notificação de teste enviada!' : 'Nenhum celular recebeu o teste.');
    } catch (error) {
      const data = (error as { response?: { data?: { mensagem?: string } } })?.response?.data;
      setTestMessage(data?.mensagem ?? 'Não foi possível enviar o teste.');
    }
  }

  return { testMessage, handleTest };
}

const DAILY_PUSH_HINT = 'Todo dia às 7h da manhã avisamos quem faz aniversário hoje e amanhã, mesmo com o app fechado.';

/** Push notifications (Android/iOS app) — sent by the server even with the app closed. */
function PushNotificationControl() {
  const { permission, enable } = usePushNotifications();
  const { testMessage, handleTest } = useTestNotification();

  return (
    <div className="flex flex-col items-end gap-1">
      {permission === 'granted' ? (
        <div className="flex gap-2">
          <span className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-yellow text-gavioes-black">
            🔔 Push ativado
          </span>
          <button
            onClick={handleTest}
            className="px-3 py-2 rounded-lg text-sm border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10"
          >
            Testar
          </button>
        </div>
      ) : permission === 'denied' ? (
        <span className="text-xs text-red-400 max-w-[240px] text-right">
          Notificações bloqueadas. Habilite nos Ajustes do celular para este app.
        </span>
      ) : (
        <button
          onClick={enable}
          className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors"
        >
          🔕 Ativar notificações
        </button>
      )}
      {testMessage && <span className="text-xs text-gavioes-white/70">{testMessage}</span>}
      <span className="text-xs text-gavioes-white/40 max-w-[240px] text-right">{DAILY_PUSH_HINT}</span>
    </div>
  );
}

/** Web push (browser / PWA): server-sent like the app, also on iPhone via the Home Screen. */
function WebPushControl() {
  const { state, enable } = useWebPush();
  const { disable: disableLocalReminders } = useBirthdayNotifications();
  const { testMessage, handleTest } = useTestNotification();
  const [error, setError] = useState<string | null>(null);

  async function handleEnable() {
    setError(null);
    try {
      if (await enable()) {
        // The server push replaces the old in-browser reminder (avoids duplicates)
        disableLocalReminders();
      }
    } catch {
      setError('Não foi possível ativar. Tente novamente.');
    }
  }

  if (state === 'ios-needs-home-screen') {
    return (
      <div className="max-w-[300px] rounded-lg border border-gavioes-yellow/40 bg-gavioes-yellow/10 p-3 text-sm">
        <p className="font-medium text-gavioes-yellow mb-1">📲 Receber avisos no iPhone</p>
        <ol className="list-decimal pl-4 space-y-0.5 text-gavioes-white/80">
          <li>No Safari, toque em <strong>Compartilhar</strong> (quadrado com seta ↑)</li>
          <li>Escolha <strong>Adicionar à Tela de Início</strong></li>
          <li>Abra o app pelo ícone e volte aqui para ativar</li>
        </ol>
        <p className="text-xs text-gavioes-white/50 mt-2">Requer iOS 16.4 ou superior.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-end gap-1">
      {state === 'checking' && <span className="text-xs text-gavioes-white/50">Verificando…</span>}
      {state === 'unsupported' && (
        <span className="text-xs text-gavioes-white/50 max-w-[240px] text-right">
          Este navegador não suporta notificações push.
        </span>
      )}
      {state === 'denied' && (
        <span className="text-xs text-red-400 max-w-[240px] text-right">
          Notificações bloqueadas. Habilite nas configurações do navegador/celular.
        </span>
      )}
      {state === 'prompt' && (
        <button
          onClick={handleEnable}
          className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors"
        >
          🔕 Ativar notificações
        </button>
      )}
      {state === 'enabled' && (
        <div className="flex gap-2">
          <span className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-yellow text-gavioes-black">
            🔔 Push ativado
          </span>
          <button
            onClick={handleTest}
            className="px-3 py-2 rounded-lg text-sm border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10"
          >
            Testar
          </button>
        </div>
      )}
      {(error || testMessage) && <span className="text-xs text-gavioes-white/70">{error ?? testMessage}</span>}
      {state !== 'unsupported' && (
        <span className="text-xs text-gavioes-white/40 max-w-[240px] text-right">{DAILY_PUSH_HINT}</span>
      )}
    </div>
  );
}

export function AniversariantesPage() {
  const { equipe, equipeId } = useCurrentEquipe();
  const { data, isLoading, isError } = usePlayersQuery({ equipeId, size: 500 });
  const { isNativeApp } = usePushNotifications();
  const updateMutation = useUpdatePlayer();

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editNome, setEditNome] = useState('');
  const [editData, setEditData] = useState('');
  const [editError, setEditError] = useState<string | null>(null);

  const players = data?.content ?? [];
  const withBirthday = sortByUpcomingBirthday(players);
  const withoutBirthday = players.filter((p) => !p.dataNascimento);
  const orderedPlayers: Player[] = [...withBirthday, ...withoutBirthday];

  function startEdit(player: Player) {
    setEditingId(player.id);
    setEditNome(player.nome);
    setEditData(player.dataNascimento ?? '');
    setEditError(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditError(null);
  }

  function saveEdit(player: Player) {
    if (!editNome.trim()) {
      setEditError('O nome é obrigatório.');
      return;
    }
    updateMutation.mutate(
      {
        id: player.id,
        data: {
          nome: editNome.trim(),
          posicao: player.posicao,
          nivel: player.nivel,
          ativo: player.ativo,
          dataNascimento: editData || null,
        },
      },
      {
        onSuccess: () => {
          setEditingId(null);
          setEditError(null);
        },
        onError: () => setEditError('Erro ao salvar. Tente novamente.'),
      }
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-title text-gavioes-yellow">Aniversariantes</h1>
          <p className="text-sm text-gavioes-white/60 mt-1">
            Datas de nascimento dos sócios{equipe ? ` — ${equipe.nome}` : ''}
          </p>
        </div>

        {isNativeApp ? (
          <PushNotificationControl />
        ) : webPushConfigured ? (
          <WebPushControl />
        ) : (
          <WebNotificationControl />
        )}
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
          {orderedPlayers.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gavioes-white/60">Nenhum jogador cadastrado ainda.</p>
            </div>
          )}

          <div className="space-y-2">
            {orderedPlayers.map((player) => {
              const iso = player.dataNascimento;
              const soon = !!iso && (isBirthdayToday(iso) || isBirthdayTomorrow(iso));
              const isRowEditing = editingId === player.id;

              if (isRowEditing) {
                return (
                  <div
                    key={player.id}
                    className="p-4 rounded-lg border border-gavioes-yellow/60 bg-gavioes-dark-gray"
                  >
                    <div className="flex flex-col sm:flex-row sm:items-end gap-3">
                      <div className="flex-1">
                        <label className="block text-xs text-gavioes-white/60 mb-1">Nome</label>
                        <input
                          type="text"
                          value={editNome}
                          onChange={(e) => setEditNome(e.target.value)}
                          className="w-full px-3 py-2 rounded bg-gavioes-black border border-gavioes-white/20 text-gavioes-white focus:border-gavioes-yellow focus:outline-none"
                          placeholder="Nome do jogador"
                        />
                      </div>
                      <div>
                        <label className="block text-xs text-gavioes-white/60 mb-1">Data de nascimento</label>
                        <input
                          type="date"
                          value={editData}
                          onChange={(e) => setEditData(e.target.value)}
                          className="w-full px-3 py-2 rounded bg-gavioes-black border border-gavioes-white/20 text-gavioes-white focus:border-gavioes-yellow focus:outline-none"
                        />
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => saveEdit(player)}
                          disabled={updateMutation.isPending}
                          className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-yellow text-gavioes-black hover:bg-gavioes-yellow/90 transition-colors disabled:opacity-50"
                        >
                          {updateMutation.isPending ? 'Salvando...' : 'Salvar'}
                        </button>
                        <button
                          onClick={cancelEdit}
                          disabled={updateMutation.isPending}
                          className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-white/30 text-gavioes-white hover:bg-gavioes-white/10 transition-colors disabled:opacity-50"
                        >
                          Cancelar
                        </button>
                      </div>
                    </div>
                    {editError && <p className="text-red-400 text-sm mt-2">{editError}</p>}
                  </div>
                );
              }

              return (
                <div
                  key={player.id}
                  className={`flex items-center justify-between gap-3 p-4 rounded-lg border transition-colors ${
                    soon
                      ? 'bg-gavioes-yellow/10 border-gavioes-yellow/50'
                      : 'bg-gavioes-dark-gray border-gavioes-white/10'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <span className="text-2xl" aria-hidden="true">🎂</span>
                    <div className="min-w-0">
                      <p className="text-gavioes-white font-medium truncate">{player.nome}</p>
                      <p className="text-sm text-gavioes-white/60">
                        {iso ? formatDayMonth(iso) : 'Sem data cadastrada'}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    {iso && (
                      <span
                        className={`text-sm font-medium ${
                          soon ? 'text-gavioes-yellow' : 'text-gavioes-white/50'
                        }`}
                      >
                        {daysLabel(iso)}
                      </span>
                    )}
                    <button
                      onClick={() => startEdit(player)}
                      className="px-3 py-1.5 text-sm rounded border border-gavioes-white/30 text-gavioes-white/80 hover:bg-gavioes-white/10 transition-colors"
                    >
                      Editar
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

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

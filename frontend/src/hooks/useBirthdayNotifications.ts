import { useCallback, useEffect, useState } from 'react';
import type { Player } from '../types';
import { formatDayMonth, isBirthdayTomorrow } from '../lib/birthday';

const ENABLED_KEY = 'gv_birthday_notifications_enabled';
const RECHECK_EVENT = 'gv-birthday-recheck';
const CHECK_INTERVAL_MS = 1000 * 60 * 60; // hourly

const notificationsSupported =
  typeof window !== 'undefined' && 'Notification' in window;

function todayKey(): string {
  const now = new Date();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${mm}-${dd}`;
}

/** Fire a notification for each player whose birthday is tomorrow (once per day). */
export function checkBirthdayNotifications(players: Player[]): void {
  if (!notificationsSupported || Notification.permission !== 'granted') return;
  if (localStorage.getItem(ENABLED_KEY) !== '1') return;

  const today = todayKey();
  players.forEach((player) => {
    if (!player.dataNascimento || !isBirthdayTomorrow(player.dataNascimento)) return;

    const dedupeKey = `gv_bday_notified_${today}_${player.id}`;
    if (localStorage.getItem(dedupeKey)) return;

    try {
      new Notification('🎉 Aniversário amanhã!', {
        body: `${player.nome} faz aniversário amanhã (${formatDayMonth(player.dataNascimento)}). 🎂`,
        tag: dedupeKey,
      });
      localStorage.setItem(dedupeKey, '1');
    } catch {
      // Ignore notification construction failures (e.g. unsupported context)
    }
  });
}

/**
 * Settings hook: exposes notification permission state and enable/disable
 * controls. Used by the birthdays page UI.
 */
export function useBirthdayNotifications() {
  const [permission, setPermission] = useState<NotificationPermission | 'unsupported'>(
    notificationsSupported ? Notification.permission : 'unsupported'
  );
  const [enabled, setEnabled] = useState(
    () => notificationsSupported && localStorage.getItem(ENABLED_KEY) === '1'
  );

  const enable = useCallback(async () => {
    if (!notificationsSupported) return false;
    let perm = Notification.permission;
    if (perm !== 'granted') {
      perm = await Notification.requestPermission();
      setPermission(perm);
    }
    if (perm === 'granted') {
      localStorage.setItem(ENABLED_KEY, '1');
      setEnabled(true);
      // Ask the app-wide scheduler to run an immediate check.
      window.dispatchEvent(new Event(RECHECK_EVENT));
      return true;
    }
    return false;
  }, []);

  const disable = useCallback(() => {
    localStorage.setItem(ENABLED_KEY, '0');
    setEnabled(false);
  }, []);

  return {
    supported: notificationsSupported,
    permission,
    enabled,
    enable,
    disable,
  };
}

/**
 * App-wide scheduler: while the app is open, periodically checks whether any
 * player has a birthday tomorrow and fires a notification one day before.
 */
export function useBirthdayScheduler(players: Player[]) {
  useEffect(() => {
    if (!notificationsSupported) return;

    checkBirthdayNotifications(players);

    const interval = setInterval(() => checkBirthdayNotifications(players), CHECK_INTERVAL_MS);
    const onRecheck = () => checkBirthdayNotifications(players);
    window.addEventListener(RECHECK_EVENT, onRecheck);

    return () => {
      clearInterval(interval);
      window.removeEventListener(RECHECK_EVENT, onRecheck);
    };
  }, [players]);
}

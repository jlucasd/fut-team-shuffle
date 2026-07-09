import type { Player } from '../types';

/** Parse a `yyyy-MM-dd` string into a local Date (avoids UTC off-by-one). */
export function parseISODate(iso: string): Date | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso);
  if (!match) return null;
  const [, y, m, d] = match;
  const date = new Date(Number(y), Number(m) - 1, Number(d));
  return Number.isNaN(date.getTime()) ? null : date;
}

/** Midnight of the local "today". */
function startOfToday(): Date {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

/** Next occurrence (today or in the future) of the birthday's month/day. */
export function nextBirthday(iso: string, from: Date = startOfToday()): Date | null {
  const birth = parseISODate(iso);
  if (!birth) return null;
  const base = new Date(from.getFullYear(), from.getMonth(), from.getDate());
  let next = new Date(base.getFullYear(), birth.getMonth(), birth.getDate());
  if (next < base) {
    next = new Date(base.getFullYear() + 1, birth.getMonth(), birth.getDate());
  }
  return next;
}

/** Whole days from today until the next birthday (0 = today, 1 = tomorrow). */
export function daysUntilBirthday(iso: string): number | null {
  const next = nextBirthday(iso);
  if (!next) return null;
  const diff = next.getTime() - startOfToday().getTime();
  return Math.round(diff / (1000 * 60 * 60 * 24));
}

/** True when the player's birthday is exactly tomorrow. */
export function isBirthdayTomorrow(iso: string): boolean {
  return daysUntilBirthday(iso) === 1;
}

/** True when the player's birthday is today. */
export function isBirthdayToday(iso: string): boolean {
  return daysUntilBirthday(iso) === 0;
}

/** Format an ISO date as `dd/MM`. */
export function formatDayMonth(iso: string): string {
  const date = parseISODate(iso);
  if (!date) return '';
  const dd = String(date.getDate()).padStart(2, '0');
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  return `${dd}/${mm}`;
}

/** Format an ISO date as `dd/MM/yyyy`. */
export function formatFullDate(iso: string): string {
  const date = parseISODate(iso);
  if (!date) return '';
  return date.toLocaleDateString('pt-BR');
}

/** Players with a birth date, sorted by how soon their next birthday is. */
export function sortByUpcomingBirthday(players: Player[]): Player[] {
  return players
    .filter((p) => !!p.dataNascimento)
    .slice()
    .sort((a, b) => {
      const da = daysUntilBirthday(a.dataNascimento!) ?? Infinity;
      const db = daysUntilBirthday(b.dataNascimento!) ?? Infinity;
      return da - db;
    });
}

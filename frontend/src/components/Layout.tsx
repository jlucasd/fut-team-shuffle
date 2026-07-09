import { Outlet } from 'react-router-dom';
import { NavBar } from './NavBar';
import { usePlayersQuery } from '../hooks/usePlayersQuery';
import { useBirthdayScheduler } from '../hooks/useBirthdayNotifications';
import type { Player } from '../types';

const NO_PLAYERS: Player[] = [];

function BirthdayNotifier() {
  const { data } = usePlayersQuery({ size: 100 });
  useBirthdayScheduler(data?.content ?? NO_PLAYERS);
  return null;
}

export function Layout() {
  return (
    <div className="min-h-screen bg-gavioes-black text-gavioes-white">
      <BirthdayNotifier />
      <NavBar />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
    </div>
  );
}

import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { NavBar } from './NavBar';
import { usePlayersQuery } from '../hooks/usePlayersQuery';
import { useBirthdayScheduler } from '../hooks/useBirthdayNotifications';
import { usePushRegistration } from '../hooks/usePushNotifications';
import type { Player } from '../types';

const NO_PLAYERS: Player[] = [];

function BirthdayNotifier() {
  // All teams: the browser reminder covers every registered birthday.
  const { data } = usePlayersQuery({ size: 500 });
  useBirthdayScheduler(data?.content ?? NO_PLAYERS);
  usePushRegistration();
  return null;
}

export function Layout() {
  return (
    <div className="min-h-screen bg-gavioes-black text-gavioes-white">
      <BirthdayNotifier />
      <NavBar />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Suspense
          fallback={
            <div className="flex justify-center py-12">
              <div className="w-8 h-8 border-4 border-gavioes-yellow border-t-transparent rounded-full animate-spin" />
            </div>
          }
        >
          <Outlet />
        </Suspense>
      </main>
    </div>
  );
}

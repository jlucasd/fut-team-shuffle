import { lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { loadHomePage } from './pages/lazyPages';
import { useAuthStore } from './stores/authStore';

// Pages load on demand: the first screen ships only what it needs
const HomePage = lazy(loadHomePage);
const PlayersPage = lazy(() => import('./pages/PlayersPage').then((m) => ({ default: m.PlayersPage })));
const DrawPage = lazy(() => import('./pages/DrawPage').then((m) => ({ default: m.DrawPage })));
const HistoryPage = lazy(() => import('./pages/HistoryPage').then((m) => ({ default: m.HistoryPage })));
const AniversariantesPage = lazy(() =>
  import('./pages/AniversariantesPage').then((m) => ({ default: m.AniversariantesPage }))
);
const EquipesPage = lazy(() => import('./pages/EquipesPage').then((m) => ({ default: m.EquipesPage })));

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<HomePage />} />
        <Route path="jogadores" element={<PlayersPage />} />
        <Route path="sorteio" element={<DrawPage />} />
        <Route path="historico" element={<HistoryPage />} />
        <Route path="aniversariantes" element={<AniversariantesPage />} />
        <Route path="times" element={<EquipesPage />} />
      </Route>
    </Routes>
  );
}

export default App;

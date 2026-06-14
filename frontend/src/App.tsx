import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { PlayersPage } from './pages/PlayersPage';
import { DrawPage } from './pages/DrawPage';
import { HistoryPage } from './pages/HistoryPage';
import { useAuthStore } from './stores/authStore';

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
      </Route>
    </Routes>
  );
}

export default App;

import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import api from '../lib/api';

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const login = useAuthStore((state) => state.login);
  const navigate = useNavigate();

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/api/auth/login', { username, password });
      login(response.data.token);
      navigate('/');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { status: number } };
        if (axiosError.response?.status === 401) {
          setError('Credenciais inválidas');
        } else {
          setError('Erro ao fazer login. Tente novamente.');
        }
      } else {
        setError('Erro de conexão. Verifique sua internet.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[80vh]">
      <div className="w-full max-w-sm bg-gavioes-dark-gray rounded-lg p-8 border border-gavioes-yellow/20">
        <h1 className="text-3xl font-title text-gavioes-yellow text-center mb-6">
          Login
        </h1>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label htmlFor="username" className="block text-sm font-body text-gavioes-white/80 mb-1">
              Usuário
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3 py-2 bg-gavioes-black border border-gavioes-yellow/30 rounded text-gavioes-white font-body focus:outline-none focus:border-gavioes-yellow"
              required
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-body text-gavioes-white/80 mb-1">
              Senha
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 bg-gavioes-black border border-gavioes-yellow/30 rounded text-gavioes-white font-body focus:outline-none focus:border-gavioes-yellow"
              required
            />
          </div>

          {error && (
            <p className="text-red-400 text-sm font-body" role="alert">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2 bg-gavioes-yellow text-gavioes-black font-title text-lg rounded hover:bg-gavioes-yellow/90 transition-colors disabled:opacity-50"
          >
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  );
}

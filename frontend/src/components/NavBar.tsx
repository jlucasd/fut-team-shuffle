import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { useEquipeStore } from '../stores/equipeStore';
import { useCurrentEquipe } from '../hooks/useEquipes';

const navItems = [
  { to: '/', label: 'Início' },
  { to: '/jogadores', label: 'Jogadores' },
  { to: '/sorteio', label: 'Sorteio' },
  { to: '/historico', label: 'Histórico' },
  { to: '/aniversariantes', label: 'Aniversariantes' },
  { to: '/times', label: 'Times' },
];

function EquipeSelector() {
  const { equipes, equipeId } = useCurrentEquipe();
  const setSelectedEquipeId = useEquipeStore((s) => s.setSelectedEquipeId);

  if (equipes.length === 0) return null;

  return (
    <select
      aria-label="Time selecionado"
      value={equipeId ?? ''}
      onChange={(e) => setSelectedEquipeId(Number(e.target.value))}
      className="max-w-[160px] sm:max-w-[220px] px-2 py-1.5 rounded bg-gavioes-black border border-gavioes-yellow/40 text-sm text-gavioes-yellow font-body focus:outline-none focus:border-gavioes-yellow"
    >
      {equipes.map((equipe) => (
        <option key={equipe.id} value={equipe.id}>
          {equipe.nome}
        </option>
      ))}
    </select>
  );
}

export function NavBar() {
  const { isAuthenticated, logout } = useAuthStore();

  return (
    <nav className="bg-gavioes-dark-gray border-b border-gavioes-yellow/20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-2 py-3 lg:h-16 lg:py-0">
          <span className="text-2xl font-title text-gavioes-yellow whitespace-nowrap">
            ⚽ Gaviões FC
          </span>

          {/* Scrolls sideways on narrow screens (phone/app) instead of overflowing */}
          <div className="order-last lg:order-none w-full lg:w-auto -mx-1 flex items-center gap-1 overflow-x-auto">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `shrink-0 px-4 py-2 rounded text-sm font-body font-medium transition-colors ${
                    isActive
                      ? 'bg-gavioes-yellow text-gavioes-black'
                      : 'text-gavioes-white hover:text-gavioes-yellow'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>

          <div className="flex items-center gap-2">
            {isAuthenticated && <EquipeSelector />}
            {isAuthenticated && (
              <button
                onClick={logout}
                className="px-3 py-1.5 text-sm font-body text-gavioes-white hover:text-gavioes-yellow transition-colors"
              >
                Sair
              </button>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

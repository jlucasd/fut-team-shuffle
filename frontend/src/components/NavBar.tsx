import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

const navItems = [
  { to: '/', label: 'Início' },
  { to: '/jogadores', label: 'Jogadores' },
  { to: '/sorteio', label: 'Sorteio' },
  { to: '/historico', label: 'Histórico' },
];

export function NavBar() {
  const { isAuthenticated, logout } = useAuthStore();

  return (
    <nav className="bg-gavioes-dark-gray border-b border-gavioes-yellow/20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-2">
            <span className="text-2xl font-title text-gavioes-yellow">
              ⚽ Gaviões FC
            </span>
          </div>

          <div className="flex items-center gap-1">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `px-4 py-2 rounded text-sm font-body font-medium transition-colors ${
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

          <div>
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

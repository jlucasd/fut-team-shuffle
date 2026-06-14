import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-8">
      <h1 className="text-6xl font-title text-gavioes-yellow text-center">
        ⚽ Gaviões FC Sorteio
      </h1>
      <p className="text-lg font-body text-gavioes-white/80 text-center max-w-md">
        Sistema de sorteio de times equilibrados para o futebol dos Gaviões FC.
      </p>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-8 w-full max-w-2xl">
        <Link
          to="/jogadores"
          className="bg-gavioes-dark-gray border border-gavioes-yellow/20 rounded-lg p-6 text-center hover:border-gavioes-yellow transition-colors"
        >
          <h2 className="text-xl font-title text-gavioes-yellow">Jogadores</h2>
          <p className="text-sm text-gavioes-white/60 mt-2">Gerenciar elenco</p>
        </Link>

        <Link
          to="/sorteio"
          className="bg-gavioes-dark-gray border border-gavioes-yellow/20 rounded-lg p-6 text-center hover:border-gavioes-yellow transition-colors"
        >
          <h2 className="text-xl font-title text-gavioes-yellow">Sorteio</h2>
          <p className="text-sm text-gavioes-white/60 mt-2">Sortear times</p>
        </Link>

        <Link
          to="/historico"
          className="bg-gavioes-dark-gray border border-gavioes-yellow/20 rounded-lg p-6 text-center hover:border-gavioes-yellow transition-colors"
        >
          <h2 className="text-xl font-title text-gavioes-yellow">Histórico</h2>
          <p className="text-sm text-gavioes-white/60 mt-2">Ver sorteios anteriores</p>
        </Link>
      </div>
    </div>
  );
}

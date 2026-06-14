import { useRef } from 'react';
import html2canvas from 'html2canvas';
import type { Player } from '../types';

const positionLabels: Record<string, string> = {
  GOLEIRO: 'GOL',
  ZAGUEIRO: 'ZAG',
  MEIO: 'MEI',
  ATACANTE: 'ATA',
};

interface ShareImagePanelProps {
  timeAmarelo: Player[];
  timePreto: Player[];
  mediaAmarelo: number;
  mediaPreto: number;
  dataHora: string;
}

export function ShareImagePanel({
  timeAmarelo,
  timePreto,
  mediaAmarelo,
  mediaPreto,
  dataHora,
}: ShareImagePanelProps) {
  const panelRef = useRef<HTMLDivElement>(null);

  const formattedDate = new Date(dataHora).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  async function handleDownload() {
    if (!panelRef.current) return;

    const canvas = await html2canvas(panelRef.current, {
      backgroundColor: '#0A0A0A',
      scale: 2,
    });

    const link = document.createElement('a');
    link.download = `gavioes-fc-times-${new Date().toISOString().slice(0, 10)}.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
  }

  function handleShareWhatsApp() {
    const message = encodeURIComponent('⚽ Confira os times de hoje do Gaviões FC!');
    window.open(`https://web.whatsapp.com/send?text=${message}`, '_blank');
  }

  return (
    <div className="contents">
      {/* Hidden panel for image capture — rendered off-screen */}
      <div
        ref={panelRef}
        style={{
          position: 'absolute',
          left: '-9999px',
          top: '-9999px',
          width: '800px',
          padding: '40px',
          backgroundColor: '#0A0A0A',
          fontFamily: 'Inter, sans-serif',
        }}
      >
        {/* Hawk Crest Placeholder */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            marginBottom: '16px',
          }}
        >
          <div
            style={{
              width: '80px',
              height: '80px',
              borderRadius: '50%',
              backgroundColor: '#F5C518',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '40px',
              border: '3px solid #0A0A0A',
              boxShadow: '0 0 0 3px #F5C518',
            }}
          >
            🦅
          </div>
        </div>

        {/* Title */}
        <h1
          style={{
            textAlign: 'center',
            color: '#F5C518',
            fontSize: '36px',
            fontFamily: '"Bebas Neue", sans-serif',
            marginBottom: '8px',
            letterSpacing: '2px',
          }}
        >
          ⚽ GAVIÕES FC — TIMES DE HOJE
        </h1>

        {/* Date/Time */}
        <p
          style={{
            textAlign: 'center',
            color: '#FFFFFF',
            opacity: 0.7,
            fontSize: '14px',
            marginBottom: '32px',
          }}
        >
          {formattedDate}
        </p>

        {/* Teams Columns */}
        <div
          style={{
            display: 'flex',
            gap: '24px',
          }}
        >
          {/* Yellow Team */}
          <div style={{ flex: 1 }}>
            <h2
              style={{
                color: '#F5C518',
                fontSize: '24px',
                fontFamily: '"Bebas Neue", sans-serif',
                textAlign: 'center',
                marginBottom: '16px',
                paddingBottom: '8px',
                borderBottom: '2px solid #F5C518',
              }}
            >
              AMARELO
            </h2>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
              {timeAmarelo.map((player) => (
                <li
                  key={player.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 12px',
                    marginBottom: '4px',
                    backgroundColor: '#1A1A1A',
                    borderRadius: '4px',
                  }}
                >
                  <span style={{ color: '#FFFFFF', fontSize: '14px' }}>
                    {player.nome}
                  </span>
                  <span style={{ color: '#F5C518', fontSize: '12px', opacity: 0.8 }}>
                    {positionLabels[player.posicao] || player.posicao}
                  </span>
                </li>
              ))}
            </ul>
            <p
              style={{
                textAlign: 'center',
                color: '#F5C518',
                fontSize: '14px',
                marginTop: '12px',
                fontWeight: 'bold',
              }}
            >
              Média: {mediaAmarelo.toFixed(2)}
            </p>
          </div>

          {/* Black Team */}
          <div style={{ flex: 1 }}>
            <h2
              style={{
                color: '#FFFFFF',
                fontSize: '24px',
                fontFamily: '"Bebas Neue", sans-serif',
                textAlign: 'center',
                marginBottom: '16px',
                paddingBottom: '8px',
                borderBottom: '2px solid #FFFFFF',
              }}
            >
              PRETO
            </h2>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
              {timePreto.map((player) => (
                <li
                  key={player.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px 12px',
                    marginBottom: '4px',
                    backgroundColor: '#1A1A1A',
                    borderRadius: '4px',
                  }}
                >
                  <span style={{ color: '#FFFFFF', fontSize: '14px' }}>
                    {player.nome}
                  </span>
                  <span style={{ color: '#FFFFFF', fontSize: '12px', opacity: 0.6 }}>
                    {positionLabels[player.posicao] || player.posicao}
                  </span>
                </li>
              ))}
            </ul>
            <p
              style={{
                textAlign: 'center',
                color: '#FFFFFF',
                fontSize: '14px',
                marginTop: '12px',
                fontWeight: 'bold',
              }}
            >
              Média: {mediaPreto.toFixed(2)}
            </p>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="contents">
        <button
          onClick={handleDownload}
          className="px-4 py-2 rounded-lg text-sm font-medium bg-gavioes-dark-gray border border-gavioes-yellow text-gavioes-yellow hover:bg-gavioes-yellow/10 transition-colors flex items-center gap-2"
        >
          📥 Baixar Imagem
        </button>
        <button
          onClick={handleShareWhatsApp}
          className="px-4 py-2 rounded-lg text-sm font-medium bg-green-700 text-white hover:bg-green-600 transition-colors flex items-center gap-2"
        >
          💬 Compartilhar via WhatsApp
        </button>
      </div>
    </div>
  );
}

// Feature: gavioes-fc-sorteio, Property 16: Image template content completeness
// **Validates: Requirements 12.4, 12.5**

import { describe, it, expect } from 'vitest';
import fc from 'fast-check';
import { render } from '@testing-library/react';
import { teamCompositionArbitrary } from './arbitraries';
import { ShareImagePanel } from '../../components/ShareImagePanel';
import type { Player } from '../../types';

const positionLabels: Record<string, string> = {
  GOLEIRO: 'GOL',
  ZAGUEIRO: 'ZAG',
  MEIO: 'MEI',
  ATACANTE: 'ATA',
};

function expectedAverage(players: Player[]): number {
  if (players.length === 0) return 0;
  return players.reduce((sum, p) => sum + p.nivel, 0) / players.length;
}

describe('Property 16: Image template content completeness', () => {
  it('rendered image template contains every player name, position, and team averages', () => {
    fc.assert(
      fc.property(teamCompositionArbitrary, ({ timeAmarelo, timePreto }) => {
        const mediaAmarelo = expectedAverage(timeAmarelo);
        const mediaPreto = expectedAverage(timePreto);
        const dataHora = '2024-06-15T14:30:00';

        const { container } = render(
          <ShareImagePanel
            timeAmarelo={timeAmarelo}
            timePreto={timePreto}
            mediaAmarelo={mediaAmarelo}
            mediaPreto={mediaPreto}
            dataHora={dataHora}
          />
        );

        const textContent = container.textContent || '';

        // Every player's name should be present
        for (const player of timeAmarelo) {
          expect(textContent).toContain(player.nome);
        }

        for (const player of timePreto) {
          expect(textContent).toContain(player.nome);
        }

        // Every player's position label should be present
        for (const player of [...timeAmarelo, ...timePreto]) {
          const posLabel = positionLabels[player.posicao] || player.posicao;
          expect(textContent).toContain(posLabel);
        }

        // Team averages should be displayed
        expect(textContent).toContain(mediaAmarelo.toFixed(2));
        expect(textContent).toContain(mediaPreto.toFixed(2));
      }),
      { numRuns: 100 }
    );
  });
});

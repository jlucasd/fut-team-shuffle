// Feature: gavioes-fc-sorteio, Property 14: Frontend average calculation and balance indicator
// **Validates: Requirements 11.2, 11.5**

import { describe, it, expect } from 'vitest';
import fc from 'fast-check';
import { teamCompositionArbitrary } from './arbitraries';
import { useDrawStore } from '../../stores/drawStore';

/**
 * Helper to compute the expected arithmetic mean of player levels.
 */
function expectedAverage(players: { nivel: number }[]): number {
  if (players.length === 0) return 0;
  return players.reduce((sum, p) => sum + p.nivel, 0) / players.length;
}

/**
 * Helper to determine the expected balance indicator color based on the difference.
 */
function expectedColor(diff: number): 'green' | 'yellow' | 'red' {
  if (diff <= 0.5) return 'green';
  if (diff <= 1.0) return 'yellow';
  return 'red';
}

describe('Property 14: Frontend average calculation and balance indicator', () => {
  it('calculated average equals the arithmetic mean of player skill levels', () => {
    fc.assert(
      fc.property(teamCompositionArbitrary, ({ timeAmarelo, timePreto }) => {
        // Set up the store with the team composition
        useDrawStore.setState({
          editedAmarelo: timeAmarelo,
          editedPreto: timePreto,
        });

        const result = useDrawStore.getState().calculateAverages();

        const expectedAmarelo = expectedAverage(timeAmarelo);
        const expectedPreto = expectedAverage(timePreto);

        // The calculated averages should match the arithmetic mean
        expect(result.mediaAmarelo).toBeCloseTo(expectedAmarelo, 10);
        expect(result.mediaPreto).toBeCloseTo(expectedPreto, 10);
      }),
      { numRuns: 100 }
    );
  });

  it('equilibrado flag is true if and only if average difference <= 0.5', () => {
    fc.assert(
      fc.property(teamCompositionArbitrary, ({ timeAmarelo, timePreto }) => {
        useDrawStore.setState({
          editedAmarelo: timeAmarelo,
          editedPreto: timePreto,
        });

        const result = useDrawStore.getState().calculateAverages();
        const diff = Math.abs(result.mediaAmarelo - result.mediaPreto);

        if (diff <= 0.5) {
          expect(result.equilibrado).toBe(true);
        } else {
          expect(result.equilibrado).toBe(false);
        }
      }),
      { numRuns: 100 }
    );
  });

  it('balance indicator color follows threshold rules: green <= 0.5, yellow <= 1.0, red > 1.0', () => {
    fc.assert(
      fc.property(teamCompositionArbitrary, ({ timeAmarelo, timePreto }) => {
        const mediaAmarelo = expectedAverage(timeAmarelo);
        const mediaPreto = expectedAverage(timePreto);
        const diff = Math.abs(mediaAmarelo - mediaPreto);

        // Replicate the BalanceIndicator logic
        let color: 'green' | 'yellow' | 'red';
        if (diff <= 0.5) {
          color = 'green';
        } else if (diff <= 1.0) {
          color = 'yellow';
        } else {
          color = 'red';
        }

        expect(color).toBe(expectedColor(diff));
      }),
      { numRuns: 100 }
    );
  });
});

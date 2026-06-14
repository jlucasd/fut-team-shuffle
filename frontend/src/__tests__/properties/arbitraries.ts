import fc from 'fast-check';
import type { Player, Position } from '../../types';

const positions: Position[] = ['GOLEIRO', 'ZAGUEIRO', 'MEIO', 'ATACANTE'];

/**
 * Arbitrary that generates a valid Player object.
 * - id: positive integer
 * - nome: alphabetic string (3-15 chars)
 * - posicao: one of the four valid positions
 * - nivel: integer between 1 and 5 (inclusive)
 * - ativo: boolean
 */
export const playerArbitrary: fc.Arbitrary<Player> = fc.record({
  id: fc.integer({ min: 1, max: 10000 }),
  nome: fc.stringMatching(/^[a-zA-Z]{3,15}$/),
  posicao: fc.constantFrom(...positions),
  nivel: fc.integer({ min: 1, max: 5 }),
  ativo: fc.constant(true),
});

/**
 * Generates a team composition: two non-empty arrays of players with unique IDs.
 * Each team has 1-8 players, simulating a draw result with Yellow and Black teams.
 */
export const teamCompositionArbitrary: fc.Arbitrary<{
  timeAmarelo: Player[];
  timePreto: Player[];
}> = fc
  .tuple(
    fc.integer({ min: 1, max: 8 }),
    fc.integer({ min: 1, max: 8 })
  )
  .chain(([sizeA, sizeB]) =>
    fc
      .array(playerArbitrary, { minLength: sizeA + sizeB, maxLength: sizeA + sizeB })
      .map((players) => {
        // Ensure unique IDs
        const uniquePlayers = players.map((p, i) => ({ ...p, id: i + 1 }));
        return {
          timeAmarelo: uniquePlayers.slice(0, sizeA),
          timePreto: uniquePlayers.slice(sizeA),
        };
      })
  );

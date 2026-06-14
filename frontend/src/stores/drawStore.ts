import { create } from 'zustand';
import type { Player, DrawResult } from '../types';

interface DrawState {
  // Selection
  selectedPlayerIds: number[];
  // Draw result
  currentDraw: DrawResult | null;
  // Editing state
  isEditing: boolean;
  editedAmarelo: Player[];
  editedPreto: Player[];
  editedReserva: Player | null;
  editedReservas: Player[];
  // Swap selection
  swapFrom: Player | null;
  swapTo: Player | null;
  // Animation
  isAnimating: boolean;

  // Actions
  selectPlayer: (id: number) => void;
  deselectPlayer: (id: number) => void;
  clearSelection: () => void;
  setDraw: (draw: DrawResult) => void;
  movePlayer: (player: Player, fromTeam: 'amarelo' | 'preto', toTeam: 'amarelo' | 'preto') => void;
  swapPlayers: (playerA: Player, playerB: Player) => void;
  swapWithReserve: (teamPlayer: Player, reservePlayer: Player) => void;
  resetEdits: () => void;
  setSwapFrom: (player: Player | null) => void;
  setSwapTo: (player: Player | null) => void;
  setIsEditing: (editing: boolean) => void;
  setIsAnimating: (animating: boolean) => void;
  calculateAverages: () => { mediaAmarelo: number; mediaPreto: number; equilibrado: boolean };
}

function calcAverage(players: Player[]): number {
  if (players.length === 0) return 0;
  return players.reduce((sum, p) => sum + p.nivel, 0) / players.length;
}

export const useDrawStore = create<DrawState>((set, get) => ({
  selectedPlayerIds: [],
  currentDraw: null,
  isEditing: false,
  editedAmarelo: [],
  editedPreto: [],
  editedReserva: null,
  editedReservas: [],
  swapFrom: null,
  swapTo: null,
  isAnimating: false,

  selectPlayer: (id) =>
    set((state) => ({
      selectedPlayerIds: state.selectedPlayerIds.includes(id)
        ? state.selectedPlayerIds
        : [...state.selectedPlayerIds, id],
    })),

  deselectPlayer: (id) =>
    set((state) => ({
      selectedPlayerIds: state.selectedPlayerIds.filter((pid) => pid !== id),
    })),

  clearSelection: () => set({ selectedPlayerIds: [] }),

  setDraw: (draw) =>
    set({
      currentDraw: draw,
      isEditing: false,
      editedAmarelo: [...draw.timeAmarelo],
      editedPreto: [...draw.timePreto],
      editedReserva: draw.reservas?.[0] || null,
      editedReservas: draw.reservas || [],
      swapFrom: null,
      swapTo: null,
    }),

  movePlayer: (player, fromTeam, toTeam) => {
    if (fromTeam === toTeam) return;
    set((state) => {
      const fromKey = fromTeam === 'amarelo' ? 'editedAmarelo' : 'editedPreto';
      const toKey = toTeam === 'amarelo' ? 'editedAmarelo' : 'editedPreto';
      return {
        isEditing: true,
        [fromKey]: state[fromKey].filter((p) => p.id !== player.id),
        [toKey]: [...state[toKey], player],
      };
    });
  },

  swapPlayers: (playerA, playerB) =>
    set((state) => {
      const aInAmarelo = state.editedAmarelo.some((p) => p.id === playerA.id);
      const bInAmarelo = state.editedAmarelo.some((p) => p.id === playerB.id);

      // Only swap if they are in different teams
      if (aInAmarelo === bInAmarelo) return state;

      const newAmarelo = state.editedAmarelo.map((p) => {
        if (p.id === playerA.id) return playerB;
        if (p.id === playerB.id) return playerA;
        return p;
      });
      const newPreto = state.editedPreto.map((p) => {
        if (p.id === playerA.id) return playerB;
        if (p.id === playerB.id) return playerA;
        return p;
      });

      return {
        isEditing: true,
        editedAmarelo: newAmarelo,
        editedPreto: newPreto,
        swapFrom: null,
        swapTo: null,
      };
    }),

  swapWithReserve: (teamPlayer, reservePlayer) =>
    set((state) => {
      const inAmarelo = state.editedAmarelo.some((p) => p.id === teamPlayer.id);
      const inPreto = state.editedPreto.some((p) => p.id === teamPlayer.id);

      if (!inAmarelo && !inPreto) return state;

      const newAmarelo = state.editedAmarelo.map((p) =>
        p.id === teamPlayer.id ? reservePlayer : p
      );
      const newPreto = state.editedPreto.map((p) =>
        p.id === teamPlayer.id ? reservePlayer : p
      );
      const newReservas = state.editedReservas.map((p) =>
        p.id === reservePlayer.id ? teamPlayer : p
      );

      return {
        isEditing: true,
        editedAmarelo: newAmarelo,
        editedPreto: newPreto,
        editedReservas: newReservas,
        swapFrom: null,
        swapTo: null,
      };
    }),

  resetEdits: () =>
    set((state) => ({
      isEditing: false,
      editedAmarelo: state.currentDraw ? [...state.currentDraw.timeAmarelo] : [],
      editedPreto: state.currentDraw ? [...state.currentDraw.timePreto] : [],
      editedReserva: state.currentDraw?.reservas?.[0] || null,
      editedReservas: state.currentDraw?.reservas || [],
      swapFrom: null,
      swapTo: null,
    })),

  setSwapFrom: (player) => set({ swapFrom: player }),
  setSwapTo: (player) => set({ swapTo: player }),
  setIsEditing: (editing) => set({ isEditing: editing }),
  setIsAnimating: (animating) => set({ isAnimating: animating }),

  calculateAverages: () => {
    const state = get();
    const mediaAmarelo = calcAverage(state.editedAmarelo);
    const mediaPreto = calcAverage(state.editedPreto);
    const diff = Math.abs(mediaAmarelo - mediaPreto);
    return {
      mediaAmarelo,
      mediaPreto,
      equilibrado: diff <= 0.5,
    };
  },
}));

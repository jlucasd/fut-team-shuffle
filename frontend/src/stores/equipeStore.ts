import { create } from 'zustand';
import { useDrawStore } from './drawStore';

const STORAGE_KEY = 'gv_equipe_id';

function readStoredId(): number | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? Number(raw) : null;
  } catch {
    return null;
  }
}

interface EquipeState {
  /** The team (club) the app is currently showing. */
  selectedEquipeId: number | null;
  setSelectedEquipeId: (id: number) => void;
}

export const useEquipeStore = create<EquipeState>((set, get) => ({
  selectedEquipeId: readStoredId(),
  setSelectedEquipeId: (id) => {
    if (get().selectedEquipeId === id) return;
    try {
      localStorage.setItem(STORAGE_KEY, String(id));
    } catch {
      // Selection just won't survive a reload
    }
    // A draw in progress belongs to the previous team.
    useDrawStore.getState().clearSelection();
    useDrawStore.setState({ currentDraw: null, isEditing: false });
    set({ selectedEquipeId: id });
  },
}));

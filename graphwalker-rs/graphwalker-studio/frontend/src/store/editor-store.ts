import { create } from 'zustand';

export type Theme = 'dark' | 'light';

interface EditorState {
  showProperties: boolean;
  theme: Theme;
  seed: string;
  autoSeed: boolean;
  globalData: string;
  toggleProperties: () => void;
  setTheme: (theme: Theme) => void;
  setSeed: (seed: string) => void;
  setAutoSeed: (auto: boolean) => void;
  setGlobalData: (data: string) => void;
}

export const useEditorStore = create<EditorState>((set) => ({
  showProperties: true,
  theme: 'dark',
  seed: '',
  autoSeed: true,
  globalData: '',
  toggleProperties: () => set((s) => ({ showProperties: !s.showProperties })),
  setTheme: (theme) => set({ theme }),
  setSeed: (seed) => set({ seed }),
  setAutoSeed: (autoSeed) => set({ autoSeed }),
  setGlobalData: (data) => set({ globalData: data }),
}));

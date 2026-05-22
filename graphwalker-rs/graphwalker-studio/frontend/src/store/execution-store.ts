import { create } from 'zustand';

interface ExecutionState {
  running: boolean;
  paused: boolean;
  delay: number;
  fulfillment: Record<string, number>;
  totalCount: number;
  stepCount: number;
  visited: Record<string, Record<string, number>>;
  currentElement: Record<string, string>;
  modelData: Record<string, string>;
  breakpoints: Set<string>;
  issues: string[];
  checkIssues: string[];

  setRunning: (running: boolean) => void;
  setPaused: (paused: boolean) => void;
  setDelay: (delay: number) => void;
  recordVisit: (modelId: string, elementId: string, fulfillment: number, totalCount: number, data: string) => void;
  loadSnapshot: (elements: Array<{ modelId: string; elementId: string; visitedCount: number }>) => void;
  toggleBreakpoint: (modelId: string, elementId: string) => void;
  hasBreakpoint: (modelId: string, elementId: string) => boolean;
  setIssues: (issues: string[]) => void;
  setCheckIssues: (issues: string[]) => void;
  reset: () => void;
  averageFulfillment: () => number;
}

export const useExecutionStore = create<ExecutionState>((set, get) => ({
  running: false,
  paused: false,
  delay: 50,
  fulfillment: {},
  totalCount: 0,
  stepCount: 0,
  visited: {},
  currentElement: {},
  modelData: {},
  breakpoints: new Set(),
  issues: [],
  checkIssues: [],

  setRunning: (running) => set({ running }),
  setPaused: (paused) => set({ paused }),
  setDelay: (delay) => set({ delay }),

  recordVisit: (modelId, elementId, fulfillment, totalCount, data) =>
    set((s) => {
      const visited = { ...s.visited };
      if (!visited[modelId]) visited[modelId] = {};
      visited[modelId] = {
        ...visited[modelId],
        [elementId]: (visited[modelId][elementId] ?? 0) + 1,
      };
      return {
        visited,
        currentElement: { ...s.currentElement, [modelId]: elementId },
        modelData: { ...s.modelData, [modelId]: data },
        fulfillment: { ...s.fulfillment, [modelId]: fulfillment },
        totalCount,
        stepCount: s.stepCount + 1,
      };
    }),

  loadSnapshot: (elements) =>
    set(() => {
      const visited: Record<string, Record<string, number>> = {};
      let stepCount = 0;
      for (const elem of elements) {
        if (elem.visitedCount > 0) {
          if (!visited[elem.modelId]) visited[elem.modelId] = {};
          visited[elem.modelId][elem.elementId] = elem.visitedCount;
          stepCount += elem.visitedCount;
        }
      }
      return { visited, stepCount };
    }),

  toggleBreakpoint: (modelId, elementId) =>
    set((s) => {
      const bp = new Set(s.breakpoints);
      const key = `${modelId},${elementId}`;
      if (bp.has(key)) bp.delete(key);
      else bp.add(key);
      return { breakpoints: bp };
    }),

  hasBreakpoint: (modelId, elementId) => get().breakpoints.has(`${modelId},${elementId}`),

  setIssues: (issues) => set({ issues }),
  setCheckIssues: (issues) => set({ checkIssues: issues }),

  reset: () =>
    set({
      running: false,
      paused: false,
      fulfillment: {},
      totalCount: 0,
      stepCount: 0,
      visited: {},
      currentElement: {},
      modelData: {},
      issues: [],
    }),

  averageFulfillment: () => {
    const f = get().fulfillment;
    const vals = Object.values(f);
    if (vals.length === 0) return 0;
    return vals.reduce((a, b) => a + b, 0) / vals.length;
  },
}));

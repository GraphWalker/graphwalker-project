import { create } from 'zustand';
import { v4 as uuid } from 'uuid';
import type { GWModel, Vertex, Edge } from './types';

const HISTORY_LIMIT = 50;
const COALESCE_MS = 300;

interface ModelState {
  models: GWModel[];
  selectedModelIndex: number;
  selectedElementId: string | null;
  _past: GWModel[][];
  _future: GWModel[][];
  _lastSnapshotTime: number;

  addModel: () => void;
  loadModels: (models: GWModel[]) => void;
  closeModel: (index: number) => void;
  closeAllModels: () => void;
  selectModel: (index: number) => void;
  selectElement: (id: string | null) => void;
  updateModel: (index: number, updates: Partial<GWModel>) => void;

  addVertex: (modelIndex: number, x: number, y: number) => string;
  addEdge: (modelIndex: number, sourceId: string, targetId: string) => string;
  updateVertex: (modelIndex: number, vertexId: string, updates: Partial<Vertex>) => void;
  updateEdge: (modelIndex: number, edgeId: string, updates: Partial<Edge>) => void;
  deleteElement: (modelIndex: number, elementId: string) => void;
  setStartElement: (modelIndex: number, elementId: string) => void;
  updateVertexPosition: (modelIndex: number, vertexId: string, x: number, y: number) => void;

  undo: () => void;
  redo: () => void;
}

let untitledCount = 0;

export const useModelStore = create<ModelState>((set, get) => {
  function pushSnapshot() {
    const { models, _past, _lastSnapshotTime } = get();
    const now = Date.now();
    const snap = structuredClone(models);
    let newPast: GWModel[][];
    if (now - _lastSnapshotTime < COALESCE_MS && _past.length > 0) {
      newPast = _past;
    } else {
      newPast = [..._past, snap].slice(-HISTORY_LIMIT);
    }
    set({ _past: newPast, _future: [], _lastSnapshotTime: now });
  }

  return {
    models: [],
    selectedModelIndex: -1,
    selectedElementId: null,
    _past: [],
    _future: [],
    _lastSnapshotTime: 0,

    addModel: () => {
      pushSnapshot();
      untitledCount++;
      const model: GWModel = {
        id: uuid(),
        name: `Untitled-${untitledCount}`,
        generator: 'random(edge_coverage(100))',
        startElementId: '',
        vertices: [],
        edges: [],
      };
      set((s) => ({
        models: [...s.models, model],
        selectedModelIndex: s.models.length,
        selectedElementId: null,
      }));
    },

    loadModels: (models) => {
      pushSnapshot();
      const normalized = models.map((m) => ({
        ...m,
        id: m.id || uuid(),
        vertices: m.vertices.map((v) => ({
          ...v,
          id: v.id || uuid(),
          properties: v.properties ?? { x: 0, y: 0 },
        })),
        edges: m.edges.map((e) => ({
          ...e,
          id: e.id || uuid(),
          sourceVertexId: e.sourceVertexId || e.targetVertexId,
        })),
      }));
      set({
        models: normalized,
        selectedModelIndex: normalized.length > 0 ? 0 : -1,
        selectedElementId: null,
      });
    },

    closeModel: (index) => {
      pushSnapshot();
      set((s) => {
        const models = s.models.filter((_, i) => i !== index);
        let si = s.selectedModelIndex;
        if (si >= models.length) si = models.length - 1;
        return { models, selectedModelIndex: si, selectedElementId: null };
      });
    },

    closeAllModels: () => {
      pushSnapshot();
      set({ models: [], selectedModelIndex: -1, selectedElementId: null });
    },

    selectModel: (index) => set({ selectedModelIndex: index, selectedElementId: null }),

    selectElement: (id) => set({ selectedElementId: id }),

    updateModel: (index, updates) => {
      pushSnapshot();
      set((s) => {
        const models = [...s.models];
        models[index] = { ...models[index], ...updates };
        return { models };
      });
    },

    addVertex: (modelIndex, x, y) => {
      pushSnapshot();
      const id = uuid();
      const vertex: Vertex = {
        id,
        name: 'v_new',
        properties: { x, y },
      };
      set((s) => {
        const models = [...s.models];
        models[modelIndex] = {
          ...models[modelIndex],
          vertices: [...models[modelIndex].vertices, vertex],
        };
        return { models, selectedElementId: id };
      });
      return id;
    },

    addEdge: (modelIndex, sourceId, targetId) => {
      pushSnapshot();
      const id = uuid();
      const edge: Edge = {
        id,
        name: 'e_new',
        sourceVertexId: sourceId,
        targetVertexId: targetId,
      };
      set((s) => {
        const models = [...s.models];
        models[modelIndex] = {
          ...models[modelIndex],
          edges: [...models[modelIndex].edges, edge],
        };
        return { models, selectedElementId: id };
      });
      return id;
    },

    updateVertex: (modelIndex, vertexId, updates) => {
      pushSnapshot();
      set((s) => {
        const models = [...s.models];
        const m = { ...models[modelIndex] };
        m.vertices = m.vertices.map((v) => (v.id === vertexId ? { ...v, ...updates } : v));
        models[modelIndex] = m;
        return { models };
      });
    },

    updateEdge: (modelIndex, edgeId, updates) => {
      pushSnapshot();
      set((s) => {
        const models = [...s.models];
        const m = { ...models[modelIndex] };
        m.edges = m.edges.map((e) => (e.id === edgeId ? { ...e, ...updates } : e));
        models[modelIndex] = m;
        return { models };
      });
    },

    deleteElement: (modelIndex, elementId) => {
      pushSnapshot();
      set((s) => {
        const models = [...s.models];
        const m = { ...models[modelIndex] };
        const isVertex = m.vertices.some((v) => v.id === elementId);
        if (isVertex) {
          m.vertices = m.vertices.filter((v) => v.id !== elementId);
          m.edges = m.edges.filter(
            (e) => e.sourceVertexId !== elementId && e.targetVertexId !== elementId,
          );
        } else {
          m.edges = m.edges.filter((e) => e.id !== elementId);
        }
        if (m.startElementId === elementId) m.startElementId = '';
        models[modelIndex] = m;
        return {
          models,
          selectedElementId: s.selectedElementId === elementId ? null : s.selectedElementId,
        };
      });
    },

    setStartElement: (modelIndex, elementId) => {
      pushSnapshot();
      set((s) => {
        const models = [...s.models];
        models[modelIndex] = { ...models[modelIndex], startElementId: elementId };
        return { models };
      });
    },

    updateVertexPosition: (modelIndex, vertexId, x, y) => {
      pushSnapshot();
      const s = get();
      const models = [...s.models];
      const m = { ...models[modelIndex] };
      m.vertices = m.vertices.map((v) =>
        v.id === vertexId ? { ...v, properties: { x, y } } : v,
      );
      models[modelIndex] = m;
      set({ models });
    },

    undo: () => {
      const { _past, _future, models, selectedModelIndex } = get();
      if (_past.length === 0) return;
      const previous = _past[_past.length - 1];
      set({
        models: previous,
        _past: _past.slice(0, -1),
        _future: [..._future, structuredClone(models)],
        selectedElementId: null,
        selectedModelIndex: Math.min(selectedModelIndex, previous.length - 1),
        _lastSnapshotTime: 0,
      });
    },

    redo: () => {
      const { _past, _future, models, selectedModelIndex } = get();
      if (_future.length === 0) return;
      const next = _future[_future.length - 1];
      set({
        models: next,
        _past: [..._past, structuredClone(models)],
        _future: _future.slice(0, -1),
        selectedElementId: null,
        selectedModelIndex: Math.min(selectedModelIndex, next.length - 1),
        _lastSnapshotTime: 0,
      });
    },
  };
});

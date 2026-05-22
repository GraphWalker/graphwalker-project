import { useEffect, useRef, useCallback } from 'react';
import cytoscape from 'cytoscape';
import coseBilkent from 'cytoscape-cose-bilkent';
import type { GWModel } from '@/store/types';
import { useModelStore } from '@/store/model-store';
import { useExecutionStore } from '@/store/execution-store';
import { useEditorStore } from '@/store/editor-store';

cytoscape.use(coseBilkent);

const themes = {
  dark: {
    bg: '#0a0a0f',
    nodeBg: '#2a2a4a',
    nodeText: '#e4e4ef',
    nodeBorder: '#4a4a6a',
    edgeLine: '#7a7a9a',
    edgeText: '#a0a0b8',
    selectedBorder: '#6366f1',
    selectedNodeBg: '#1e1e3a',
    visitedNodeBg: '#1a3a2a',
    visitedBorder: '#22c55e',
    currentNodeBg: '#22c55e',
    currentNodeText: '#0a0a0f',
    currentBorder: '#4ade80',
    startBorder: '#22c55e',
    breakpointBorder: '#ef4444',
    sharedBorder: '#f59e0b',
    menuBg: '#1a1a25',
    menuBorder: '#2a2a3a',
    menuText: '#e4e4ef',
    menuHover: '#2a2a4a',
  },
  light: {
    bg: '#f8f9fa',
    nodeBg: '#ffffff',
    nodeText: '#1a1a2e',
    nodeBorder: '#c0c0d0',
    edgeLine: '#666680',
    edgeText: '#555570',
    selectedBorder: '#6366f1',
    selectedNodeBg: '#e8e8f8',
    visitedNodeBg: '#d4edda',
    visitedBorder: '#22c55e',
    currentNodeBg: '#22c55e',
    currentNodeText: '#ffffff',
    currentBorder: '#16a34a',
    startBorder: '#22c55e',
    breakpointBorder: '#ef4444',
    sharedBorder: '#f59e0b',
    menuBg: '#ffffff',
    menuBorder: '#d0d0d8',
    menuText: '#1a1a2e',
    menuHover: '#f0f0f5',
  },
};

function estimateLabelWidth(label: string): number {
  return Math.max(60, label.length * 7.5 + 24);
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function buildStylesheet(t: typeof themes.dark): any[] {
  return [
    {
      selector: 'node',
      style: {
        label: 'data(label)',
        'text-valign': 'center',
        'text-halign': 'center',
        'background-color': t.nodeBg,
        color: t.nodeText,
        'border-width': 2,
        'border-color': t.nodeBorder,
        'font-size': '12px',
        'font-family': 'Inter, system-ui, sans-serif',
        shape: 'roundrectangle',
        width: 'data(width)',
        height: 32,
        'text-wrap': 'none',
      },
    },
    {
      selector: 'node.start',
      style: { 'border-color': t.startBorder, 'border-width': 3 },
    },
    {
      selector: 'node.visited',
      style: { 'background-color': t.visitedNodeBg, 'border-color': t.visitedBorder },
    },
    {
      selector: 'node.current',
      style: {
        'background-color': t.currentNodeBg,
        'border-color': t.currentBorder,
        'border-width': 3,
      },
    },
    {
      selector: 'node.breakpoint',
      style: { 'border-color': t.breakpointBorder, 'border-width': 3, 'border-style': 'dashed' },
    },
    {
      selector: 'node.shared',
      style: { 'border-color': t.sharedBorder, 'border-style': 'double', 'border-width': 4 },
    },
    {
      selector: 'node:selected',
      style: { 'border-color': t.selectedBorder, 'border-width': 3, 'background-color': t.selectedNodeBg },
    },
    {
      selector: 'edge',
      style: {
        label: 'data(label)',
        width: 2,
        'line-color': t.edgeLine,
        'target-arrow-color': t.edgeLine,
        'target-arrow-shape': 'triangle',
        'curve-style': 'bezier',
        color: t.edgeText,
        'font-size': '11px',
        'font-family': 'Inter, system-ui, sans-serif',
        'text-rotation': 'autorotate',
        'text-margin-y': -10,
      },
    },
    {
      selector: 'edge.start',
      style: { 'line-color': t.startBorder, 'target-arrow-color': t.startBorder, width: 3 },
    },
    {
      selector: 'edge.visited',
      style: { 'line-color': t.visitedBorder, 'target-arrow-color': t.visitedBorder },
    },
    {
      selector: 'edge.current',
      style: { 'line-color': t.currentBorder, 'target-arrow-color': t.currentBorder, width: 3 },
    },
    {
      selector: 'edge:selected',
      style: { 'line-color': t.selectedBorder, 'target-arrow-color': t.selectedBorder, width: 3 },
    },
    {
      selector: ':loop',
      style: {
        'curve-style': 'bezier',
        'loop-direction': '0deg',
        'loop-sweep': '-90deg',
      },
    },
  ];
}

function modelToElements(model: GWModel): cytoscape.ElementDefinition[] {
  const nodes: cytoscape.ElementDefinition[] = [];
  const edges: cytoscape.ElementDefinition[] = [];

  for (const v of model.vertices) {
    const classes: string[] = [];
    if (v.id === model.startElementId) classes.push('start');
    if (v.sharedState) classes.push('shared');
    const label = v.name || v.id;
    nodes.push({
      group: 'nodes',
      data: { id: v.id, label, width: estimateLabelWidth(label) },
      position: v.properties ? { x: v.properties.x, y: v.properties.y } : undefined,
      classes: classes.join(' '),
    });
  }

  for (const e of model.edges) {
    if (!e.sourceVertexId) continue;
    const classes: string[] = [];
    if (e.id === model.startElementId) classes.push('start');
    edges.push({
      group: 'edges',
      data: {
        id: e.id,
        label: e.name || '',
        source: e.sourceVertexId,
        target: e.targetVertexId,
      },
      classes: classes.join(' '),
    });
  }

  return [...nodes, ...edges];
}

interface Props {
  model: GWModel;
  modelIndex: number;
}

export default function GraphEditor({ model, modelIndex }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const cyRef = useRef<cytoscape.Core | null>(null);
  const edgeSourceRef = useRef<string | null>(null);
  const modelIdRef = useRef<string | null>(null);
  const vKeyDownRef = useRef(false);
  const eKeyDownRef = useRef(false);
  const rubberBandRef = useRef<SVGLineElement | null>(null);
  const svgOverlayRef = useRef<SVGSVGElement | null>(null);

  const selectElement = useModelStore((s) => s.selectElement);
  const selectedElementId = useModelStore((s) => s.selectedElementId);
  const addVertex = useModelStore((s) => s.addVertex);
  const addEdge = useModelStore((s) => s.addEdge);
  const deleteElement = useModelStore((s) => s.deleteElement);
  const setStartElement = useModelStore((s) => s.setStartElement);
  const updateVertexPosition = useModelStore((s) => s.updateVertexPosition);
  const toggleBreakpoint = useExecutionStore((s) => s.toggleBreakpoint);
  const modelVisited = useExecutionStore((s) => s.visited[model.id]);
  const currentElementId = useExecutionStore((s) => s.currentElement[model.id]);
  const modelData = useExecutionStore((s) => s.modelData[model.id]);
  const stepCount = useExecutionStore((s) => s.stepCount);
  const allVisited = useExecutionStore((s) => s.visited);
  const allModels = useModelStore((s) => s.models);
  const theme = useEditorStore((s) => s.theme);
  const themeColors = themes[theme];

  const initCy = useCallback(() => {
    if (!containerRef.current) return;
    if (cyRef.current) cyRef.current.destroy();
    const container = containerRef.current;
    const elements = modelToElements(model);
    const hasPositions = model.vertices.some(
      (v) => v.properties && (v.properties.x !== 0 || v.properties.y !== 0),
    );

    const cy = cytoscape({
      container,
      elements,
      style: buildStylesheet(themeColors),
      layout: {
        name: hasPositions ? 'preset' : 'cose-bilkent',
        animate: false,
        idealEdgeLength: 200,
      } as unknown as cytoscape.LayoutOptions,
      wheelSensitivity: 0.3,
    });

    cy.on('tap', (e) => {
      if (e.target === cy) {
        if (vKeyDownRef.current) {
          const pos = e.position;
          addVertex(modelIndex, pos.x, pos.y);
        } else {
          selectElement(null);
          edgeSourceRef.current = null;
          const line = rubberBandRef.current;
          if (line) line.style.display = 'none';
        }
      }
    });

    cy.on('tap', 'node, edge', (e) => {
      selectElement(e.target.id());
    });

    cy.on('mousedown', 'node', (e) => {
      if (eKeyDownRef.current) {
        edgeSourceRef.current = e.target.id();
        const rp = e.target.renderedPosition();
        const line = rubberBandRef.current;
        if (line) {
          line.setAttribute('x1', String(rp.x));
          line.setAttribute('y1', String(rp.y));
          line.setAttribute('x2', String(rp.x));
          line.setAttribute('y2', String(rp.y));
          line.style.display = '';
        }
      }
    });

    cy.on('mouseup', 'node', (e) => {
      if (eKeyDownRef.current && edgeSourceRef.current) {
        addEdge(modelIndex, edgeSourceRef.current, e.target.id());
        edgeSourceRef.current = null;
        const line = rubberBandRef.current;
        if (line) line.style.display = 'none';
      }
    });

    cy.on('mousemove', (e) => {
      if (edgeSourceRef.current && rubberBandRef.current) {
        const rp = e.renderedPosition;
        rubberBandRef.current.setAttribute('x2', String(rp.x));
        rubberBandRef.current.setAttribute('y2', String(rp.y));
      }
    });

    cy.on('dragfree', 'node', (e) => {
      const pos = e.target.position();
      updateVertexPosition(modelIndex, e.target.id(), pos.x, pos.y);
    });

    cy.on('cxttap', 'node', (e) => {
      const nodeId = e.target.id();
      showContextMenu(cy, e.originalEvent as MouseEvent, themeColors, [
        { label: 'Set as start', action: () => setStartElement(modelIndex, nodeId) },
        { label: 'Toggle breakpoint', action: () => toggleBreakpoint(model.id, nodeId) },
        { label: 'Delete', action: () => deleteElement(modelIndex, nodeId) },
      ]);
    });

    cy.on('cxttap', 'edge', (e) => {
      const edgeId = e.target.id();
      showContextMenu(cy, e.originalEvent as MouseEvent, themeColors, [
        { label: 'Set as start', action: () => setStartElement(modelIndex, edgeId) },
        { label: 'Delete', action: () => deleteElement(modelIndex, edgeId) },
      ]);
    });

    cy.on('cxttap', (e) => {
      if (e.target !== cy) return;
      const pos = e.position;
      showContextMenu(cy, e.originalEvent as MouseEvent, themeColors, [
        { label: 'Add vertex', action: () => addVertex(modelIndex, pos.x, pos.y) },
        {
          label: 'Layout: Force-directed',
          action: () =>
            cy.layout({
              name: 'cose-bilkent', animate: true, idealEdgeLength: 200,
            } as unknown as cytoscape.LayoutOptions).run(),
        },
        { label: 'Layout: Circle', action: () => cy.layout({ name: 'circle', animate: true }).run() },
        { label: 'Layout: Grid', action: () => cy.layout({ name: 'grid', animate: true }).run() },
      ]);
    });

    cyRef.current = cy;
    modelIdRef.current = model.id;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [model.id, theme]);

  useEffect(() => {
    initCy();
    return () => { cyRef.current?.destroy(); };
  }, [initCy]);

  useEffect(() => {
    const cy = cyRef.current;
    if (!cy) return;
    if (modelIdRef.current === model.id) {
      modelIdRef.current = null;
      return;
    }

    const existingIds = new Set(cy.elements().map((e) => e.id()));
    const newElements = modelToElements(model);
    const newIds = new Set(newElements.map((e) => e.data.id!));

    for (const id of existingIds) {
      if (!newIds.has(id)) cy.getElementById(id).remove();
    }

    for (const el of newElements) {
      const existing = cy.getElementById(el.data.id!);
      if (existing.length === 0) {
        cy.add(el);
      } else {
        if (el.position && existing.isNode()) {
          const curr = existing.position();
          if (Math.abs(curr.x - el.position.x) > 1 || Math.abs(curr.y - el.position.y) > 1) {
            existing.position(el.position);
          }
        }
        existing.data(el.data);
        if (el.classes !== undefined) {
          const structural = (el.classes as string).split(' ').filter(Boolean);
          const keep = ['visited', 'current'];
          const preserved = keep.filter((c) => existing.hasClass(c));
          existing.classes([...structural, ...preserved].join(' '));
        }
      }
    }
  }, [model]);

  useEffect(() => {
    const cy = cyRef.current;
    if (!cy) return;

    const visitedMap = modelVisited ?? {};
    cy.elements().removeClass('visited current');
    for (const elemId of Object.keys(visitedMap)) {
      cy.getElementById(elemId).addClass('visited');
    }
    if (currentElementId) {
      cy.getElementById(currentElementId).addClass('current');
    }
  }, [modelVisited, currentElementId]);

  useEffect(() => {
    const cy = cyRef.current;
    if (!cy || !selectedElementId) return;
    cy.elements().unselect();
    cy.getElementById(selectedElementId).select();
  }, [selectedElementId]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      const cy = cyRef.current;
      if (!cy) return;
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;

      if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
        e.preventDefault();
        useModelStore.getState().undo();
        return;
      }
      if ((e.ctrlKey || e.metaKey) && (e.key === 'Z' || e.key === 'y')) {
        e.preventDefault();
        useModelStore.getState().redo();
        return;
      }

      if (e.key === 'v' || e.key === 'V') {
        vKeyDownRef.current = true;
      }

      if (e.key === 'e' || e.key === 'E') {
        eKeyDownRef.current = true;
        cy.autoungrabify(true);
      }

      if (e.key === 'Delete' || e.key === 'Backspace') {
        const selected = cy.$(':selected');
        selected.forEach((el) => deleteElement(modelIndex, el.id()));
      }

      if (e.key === 'Escape') {
        edgeSourceRef.current = null;
        selectElement(null);
        const line = rubberBandRef.current;
        if (line) line.style.display = 'none';
      }
    };

    const onKeyUp = (e: KeyboardEvent) => {
      if (e.key === 'v' || e.key === 'V') {
        vKeyDownRef.current = false;
      }
      if (e.key === 'e' || e.key === 'E') {
        eKeyDownRef.current = false;
        edgeSourceRef.current = null;
        const cy = cyRef.current;
        if (cy) cy.autoungrabify(false);
        const line = rubberBandRef.current;
        if (line) line.style.display = 'none';
      }
    };

    document.addEventListener('keydown', onKeyDown);
    document.addEventListener('keyup', onKeyUp);
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      document.removeEventListener('keyup', onKeyUp);
    };
  }, [modelIndex, addVertex, addEdge, deleteElement, selectElement]);

  const dataEntries = modelData
    ? modelData.split(';').filter(Boolean).map((entry) => {
        const eq = entry.indexOf('=');
        if (eq === -1) return { key: entry, value: '' };
        return { key: entry.slice(0, eq), value: entry.slice(eq + 1) };
      })
    : [];

  const countElements = (m: GWModel) =>
    m.vertices.length + m.edges.filter((e) => e.sourceVertexId).length;

  const countVisited = (m: GWModel, vis: Record<string, number> | undefined) => {
    if (!vis) return 0;
    let count = 0;
    for (const v of m.vertices) if (vis[v.id]) count++;
    for (const e of m.edges) if (e.sourceVertexId && vis[e.id]) count++;
    return count;
  };

  const currentTotal = countElements(model);
  const currentVisited = countVisited(model, modelVisited);
  const currentUnvisited = currentTotal - currentVisited;

  const currentVertices = model.vertices.length;
  const currentEdges = model.edges.filter((e) => e.sourceVertexId).length;
  let allVertices = 0;
  let allEdges = 0;
  let allTotal = 0;
  let allVisitedCount = 0;
  for (const m of allModels) {
    allVertices += m.vertices.length;
    allEdges += m.edges.filter((e) => e.sourceVertexId).length;
    allTotal += countElements(m);
    allVisitedCount += countVisited(m, allVisited[m.id]);
  }
  const allUnvisited = allTotal - allVisitedCount;

  return (
    <div className="w-full h-full relative" style={{ background: themeColors.bg }}>
      <div ref={containerRef} className="w-full h-full" />
      {model.vertices.length === 0 && (
        <div
          className="absolute inset-0 flex items-center justify-center pointer-events-none"
          style={{ color: themeColors.edgeText }}
        >
          <p className="text-sm">Add a vertex by pressing the <kbd className="font-semibold">v</kbd> key and left click on the canvas</p>
        </div>
      )}
      <svg
        ref={svgOverlayRef}
        className="absolute inset-0 w-full h-full pointer-events-none"
        style={{ zIndex: 10 }}
      >
        <line
          ref={rubberBandRef}
          stroke={themeColors.edgeLine}
          strokeWidth="2"
          strokeDasharray="6 4"
          style={{ display: 'none' }}
        />
      </svg>
      {dataEntries.length > 0 && (
        <div
          className="absolute bottom-3 left-3 rounded-lg text-xs font-mono pointer-events-none"
          style={{
            background: `${themeColors.menuBg}cc`,
            border: `1px solid ${themeColors.menuBorder}`,
            color: themeColors.menuText,
            padding: '8px 12px',
            maxHeight: '40%',
            overflowY: 'auto',
          }}
        >
          <div
            className="font-semibold mb-1 uppercase tracking-wider"
            style={{ fontSize: '10px', color: themeColors.edgeText }}
          >
            Model Data
          </div>
          <table style={{ borderSpacing: '0 1px' }}>
            <tbody>
              {dataEntries.map((e) => (
                <tr key={e.key}>
                  <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                    {e.key}
                  </td>
                  <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                    {e.value}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <div
        className="absolute bottom-3 right-3 rounded-lg text-xs font-mono pointer-events-none"
        style={{
          background: `${themeColors.menuBg}cc`,
          border: `1px solid ${themeColors.menuBorder}`,
          color: themeColors.menuText,
          padding: '8px 12px',
        }}
      >
        <div
          className="font-semibold mb-1 uppercase tracking-wider"
          style={{ fontSize: '10px', color: themeColors.edgeText }}
        >
          Statistics
        </div>
        <table style={{ borderSpacing: '0 1px' }}>
          <tbody>
            <tr>
              <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                Models
              </td>
              <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                {allModels.length}
              </td>
            </tr>
            <tr>
              <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                Vertices
              </td>
              <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                {currentVertices} ({allVertices})
              </td>
            </tr>
            <tr>
              <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                Edges
              </td>
              <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                {currentEdges} ({allEdges})
              </td>
            </tr>
            {stepCount > 0 && (
              <>
                <tr>
                  <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                    Steps
                  </td>
                  <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                    {stepCount}
                  </td>
                </tr>
                <tr>
                  <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                    Unvisited (all)
                  </td>
                  <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                    {allUnvisited}
                  </td>
                </tr>
                <tr>
                  <td className="pr-3 whitespace-nowrap" style={{ color: themeColors.edgeText }}>
                    Unvisited (model)
                  </td>
                  <td className="whitespace-nowrap" style={{ color: themeColors.nodeText }}>
                    {currentUnvisited}
                  </td>
                </tr>
              </>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

interface MenuItem {
  label: string;
  action: () => void;
}

function showContextMenu(
  cy: cytoscape.Core,
  event: MouseEvent,
  t: typeof themes.dark,
  items: MenuItem[],
) {
  const existing = document.getElementById('gw-context-menu');
  if (existing) existing.remove();

  const menu = document.createElement('div');
  menu.id = 'gw-context-menu';
  menu.style.cssText = `
    position: fixed;
    left: ${event.clientX}px;
    top: ${event.clientY}px;
    z-index: 1000;
    background: ${t.menuBg};
    border: 1px solid ${t.menuBorder};
    border-radius: 8px;
    padding: 4px;
    min-width: 160px;
    box-shadow: 0 8px 32px rgba(0,0,0,0.5);
  `;

  for (const item of items) {
    const btn = document.createElement('button');
    btn.textContent = item.label;
    btn.style.cssText = `
      display: block;
      width: 100%;
      text-align: left;
      padding: 8px 12px;
      border: none;
      background: transparent;
      color: ${t.menuText};
      font-size: 13px;
      font-family: Inter, system-ui, sans-serif;
      border-radius: 4px;
      cursor: pointer;
    `;
    btn.onmouseenter = () => { btn.style.background = t.menuHover; };
    btn.onmouseleave = () => { btn.style.background = 'transparent'; };
    btn.onclick = () => {
      item.action();
      menu.remove();
    };
    menu.appendChild(btn);
  }

  (cy.container()?.ownerDocument ?? document).body.appendChild(menu);

  const close = (e: MouseEvent) => {
    if (!menu.contains(e.target as Node)) {
      menu.remove();
      document.removeEventListener('mousedown', close);
    }
  };
  setTimeout(() => document.addEventListener('mousedown', close), 0);
}

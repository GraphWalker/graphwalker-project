import { useModelStore } from '@/store/model-store';
import { useExecutionStore } from '@/store/execution-store';
import { useEditorStore } from '@/store/editor-store';
import type { GWModel } from '@/store/types';

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="border-b border-border">
      <div className="px-4 py-2 text-xs font-semibold text-text-muted uppercase tracking-wider bg-surface-alt">
        {title}
      </div>
      <div className="p-4 space-y-3">{children}</div>
    </div>
  );
}

function Field({
  label,
  value,
  onChange,
  disabled,
  multiline,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  disabled?: boolean;
  multiline?: boolean;
}) {
  const cls = `
    w-full bg-surface-alt border border-border rounded-md px-3 py-1.5
    text-sm text-text focus:outline-none focus:border-primary
    disabled:opacity-40 disabled:cursor-not-allowed
    transition-colors
  `;
  return (
    <div>
      <label className="block text-xs text-text-muted mb-1">{label}</label>
      {multiline ? (
        <textarea
          className={`${cls} resize-y min-h-[60px]`}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          rows={3}
        />
      ) : (
        <input
          className={cls}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
        />
      )}
    </div>
  );
}

export default function PropertiesPanel() {
  const models = useModelStore((s) => s.models);
  const selectedModelIndex = useModelStore((s) => s.selectedModelIndex);
  const selectedElementId = useModelStore((s) => s.selectedElementId);
  const updateModel = useModelStore((s) => s.updateModel);
  const updateVertex = useModelStore((s) => s.updateVertex);
  const updateEdge = useModelStore((s) => s.updateEdge);
  const setStartElement = useModelStore((s) => s.setStartElement);
  const delay = useExecutionStore((s) => s.delay);
  const setDelay = useExecutionStore((s) => s.setDelay);
  const seed = useEditorStore((s) => s.seed);
  const setSeed = useEditorStore((s) => s.setSeed);
  const autoSeed = useEditorStore((s) => s.autoSeed);
  const setAutoSeed = useEditorStore((s) => s.setAutoSeed);
  const globalData = useEditorStore((s) => s.globalData);
  const setGlobalData = useEditorStore((s) => s.setGlobalData);

  const model: GWModel | undefined = models[selectedModelIndex];
  if (!model) {
    return (
      <div className="w-80 bg-surface border-l border-border shrink-0 flex items-center justify-center text-text-muted text-sm">
        No model selected
      </div>
    );
  }

  const vertex = model.vertices.find((v) => v.id === selectedElementId);
  const edge = model.edges.find((e) => e.id === selectedElementId);
  const noSelection = !vertex && !edge;

  return (
    <div className="w-80 bg-surface border-l border-border shrink-0 overflow-y-auto">
      <Section title="Global">
        <div>
          <div className="flex items-center justify-between mb-1">
            <label className="text-xs text-text-muted">Seed</label>
            <label className="flex items-center gap-1.5 text-xs text-text-muted cursor-pointer select-none">
              <input
                type="checkbox"
                checked={autoSeed}
                onChange={(e) => setAutoSeed(e.target.checked)}
                className="accent-primary"
              />
              Auto
            </label>
          </div>
          <input
            className={`
              w-full bg-surface-alt border rounded-md px-3 py-1.5
              text-sm text-text focus:outline-none focus:border-primary
              disabled:opacity-40 disabled:cursor-not-allowed
              transition-colors
              ${!autoSeed && !seed.trim() ? 'border-warning' : 'border-border'}
            `}
            value={seed}
            onChange={(e) => setSeed(e.target.value)}
            disabled={autoSeed}
            placeholder={autoSeed ? '' : 'Enter a seed number'}
          />
          {!autoSeed && !seed.trim() && (
            <p className="text-xs text-warning mt-1">Enter a seed or enable auto</p>
          )}
        </div>
        <Field
          label="Global data"
          value={globalData}
          onChange={setGlobalData}
          multiline
        />
      </Section>

      <Section title="Execution">
        <Field
          label="Generator"
          value={model.generator}
          onChange={(v) => updateModel(selectedModelIndex, { generator: v })}
        />
        <div>
          <label className="block text-xs text-text-muted mb-1">
            Step delay: {delay}ms
          </label>
          <input
            type="range"
            min={0}
            max={500}
            step={10}
            value={delay}
            onChange={(e) => setDelay(Number(e.target.value))}
            className="w-full accent-primary"
          />
        </div>
      </Section>

      <Section title="Model">
        <Field
          label="Name"
          value={model.name}
          onChange={(v) => updateModel(selectedModelIndex, { name: v })}
        />
        <Field
          label="Actions"
          value={(model.actions ?? []).filter(Boolean).join('\n')}
          onChange={(v) => {
            const actions = v.split('\n').map((s) => s.trim()).filter(Boolean);
            updateModel(selectedModelIndex, { actions });
          }}
          multiline
        />
      </Section>

      <Section title="Element">
        <Field
          label="Name"
          value={vertex?.name ?? edge?.name ?? ''}
          onChange={(v) => {
            if (vertex) updateVertex(selectedModelIndex, vertex.id, { name: v });
            if (edge) updateEdge(selectedModelIndex, edge.id, { name: v });
          }}
          disabled={noSelection}
        />
        {(vertex || noSelection) && (
          <Field
            label="Shared State"
            value={vertex?.sharedState ?? ''}
            onChange={(v) => {
              if (vertex) updateVertex(selectedModelIndex, vertex.id, { sharedState: v || undefined });
            }}
            disabled={noSelection}
          />
        )}
        {(edge || noSelection) && (
          <>
            <Field
              label="Guard"
              value={edge?.guard ?? ''}
              onChange={(v) => {
                if (edge) updateEdge(selectedModelIndex, edge.id, { guard: v || undefined });
              }}
              disabled={noSelection}
            />
            <Field
              label="Weight"
              value={edge?.weight?.toString() ?? ''}
              onChange={(v) => {
                if (edge) updateEdge(selectedModelIndex, edge.id, { weight: v ? Number(v) : undefined });
              }}
              disabled={noSelection}
            />
          </>
        )}
        <Field
          label="Actions"
          value={
            vertex ? (vertex.actions ?? []).filter(Boolean).join('\n')
            : edge ? (edge.actions ?? []).filter(Boolean).join('\n')
            : (model.actions ?? []).filter(Boolean).join('\n')
          }
          onChange={(v) => {
            const actions = v.split('\n').map((s) => s.trim()).filter(Boolean);
            if (vertex) updateVertex(selectedModelIndex, vertex.id, { actions });
            else if (edge) updateEdge(selectedModelIndex, edge.id, { actions });
            else updateModel(selectedModelIndex, { actions });
          }}
          multiline
        />
        <Field
          label="Requirements"
          value={vertex?.requirements?.join('\n') ?? edge?.requirements?.join('\n') ?? ''}
          onChange={(v) => {
            const reqs = v.split('\n').map((s) => s.trim()).filter(Boolean);
            if (vertex) updateVertex(selectedModelIndex, vertex.id, { requirements: reqs });
            if (edge) updateEdge(selectedModelIndex, edge.id, { requirements: reqs });
          }}
          disabled={noSelection}
          multiline
        />
        {(vertex || edge) && (
          <div className="flex items-center gap-2">
            <label className="text-xs text-text-muted">Start element</label>
            <button
              onClick={() => setStartElement(selectedModelIndex, (vertex?.id ?? edge?.id)!)}
              className={`
                px-2 py-0.5 text-xs rounded-md border transition-colors
                ${model.startElementId === (vertex?.id ?? edge?.id)
                  ? 'bg-success/20 border-success text-success'
                  : 'border-border text-text-muted hover:border-border-hover'}
              `}
            >
              {model.startElementId === (vertex?.id ?? edge?.id) ? 'Start' : 'Set as start'}
            </button>
          </div>
        )}
      </Section>
    </div>
  );
}

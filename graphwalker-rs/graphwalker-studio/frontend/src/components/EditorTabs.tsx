import { X, Plus } from 'lucide-react';
import type { GWModel } from '@/store/types';
import { useExecutionStore } from '@/store/execution-store';

interface Props {
  models: GWModel[];
  selectedIndex: number;
  onSelect: (index: number) => void;
  onClose: (index: number) => void;
  onAdd: () => void;
}

export default function EditorTabs({ models, selectedIndex, onSelect, onClose, onAdd }: Props) {
  const visited = useExecutionStore((s) => s.visited);
  const running = useExecutionStore((s) => s.running);
  const paused = useExecutionStore((s) => s.paused);

  if (models.length === 0) return null;

  return (
    <div className="flex items-center bg-surface border-b border-border h-9 shrink-0 overflow-x-auto">
      {models.map((m, i) => {
        const hasVisited = (running || paused) && visited[m.id] && Object.keys(visited[m.id]).length > 0;
        return (
          <div
            key={m.id}
            onClick={() => onSelect(i)}
            className={`
              flex items-center gap-2 px-3 h-full text-sm cursor-pointer border-r border-border
              transition-colors select-none whitespace-nowrap
              ${i === selectedIndex
                ? 'bg-background text-text border-b-2 border-b-primary'
                : 'text-text-muted hover:text-text hover:bg-surface-alt'}
            `}
          >
            {hasVisited && (
              <span className="w-1.5 h-1.5 rounded-full bg-success shrink-0" />
            )}
            <span>{m.name}</span>
            <button
              onClick={(e) => { e.stopPropagation(); onClose(i); }}
              className="hover:text-danger rounded p-0.5 transition-colors"
            >
              <X size={12} />
            </button>
          </div>
        );
      })}
      <button
        onClick={onAdd}
        className="flex items-center justify-center w-9 h-full text-text-muted hover:text-text hover:bg-surface-alt transition-colors"
        title="New model"
      >
        <Plus size={14} />
      </button>
    </div>
  );
}

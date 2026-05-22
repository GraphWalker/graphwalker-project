import {
  FilePlus,
  FolderOpen,
  Save,
  Undo2,
  Redo2,
  Play,
  Pause,
  SkipForward,
  Square,
  PanelRight,
  Sun,
  Moon,
} from 'lucide-react';
import { useEditorStore } from '@/store/editor-store';
import { useModelStore } from '@/store/model-store';

interface Props {
  onNewModel: () => void;
  onOpenFile: () => void;
  onSaveFile: () => void;
  onPlay: () => void;
  onPause: () => void;
  onStep: () => void;
  onStop: () => void;
  running: boolean;
  paused: boolean;
  observing?: boolean;
}

function IconButton({
  icon: Icon,
  title,
  onClick,
  disabled,
  active,
}: {
  icon: React.ElementType;
  title: string;
  onClick: () => void;
  disabled?: boolean;
  active?: boolean;
}) {
  return (
    <button
      title={title}
      onClick={onClick}
      disabled={disabled}
      className={`
        flex items-center justify-center w-10 h-10 rounded-lg transition-colors
        ${active ? 'bg-primary/20 text-primary' : 'text-text-muted hover:text-text hover:bg-surface-alt'}
        ${disabled ? 'opacity-30 cursor-not-allowed' : 'cursor-pointer'}
      `}
    >
      <Icon size={18} />
    </button>
  );
}

export default function Sidebar(props: Props) {
  const { showProperties, toggleProperties, theme, setTheme } = useEditorStore();
  const canUndo = useModelStore((s) => s._past.length > 0);
  const canRedo = useModelStore((s) => s._future.length > 0);
  const undo = useModelStore((s) => s.undo);
  const redo = useModelStore((s) => s.redo);

  return (
    <div className="flex flex-col items-center w-12 bg-surface border-r border-border py-3 gap-1 shrink-0">
      <div className="flex flex-col gap-1 pb-3 border-b border-border mb-1">
        <IconButton icon={FilePlus} title="New model" onClick={props.onNewModel} />
        <IconButton icon={FolderOpen} title="Open file" onClick={props.onOpenFile} />
        <IconButton icon={Save} title="Save" onClick={props.onSaveFile} />
      </div>

      <div className="flex flex-col gap-1 pb-3 border-b border-border mb-1">
        <IconButton icon={Undo2} title="Undo (Ctrl+Z)" onClick={undo} disabled={!canUndo} />
        <IconButton icon={Redo2} title="Redo (Ctrl+Shift+Z)" onClick={redo} disabled={!canRedo} />
      </div>

      <div className="flex flex-col gap-1 pb-3 border-b border-border mb-1">
        {props.running && !props.paused ? (
          <IconButton icon={Pause} title="Pause" onClick={props.onPause} />
        ) : (
          <IconButton icon={Play} title="Play" onClick={props.onPlay} />
        )}
        <IconButton
          icon={SkipForward}
          title="Step"
          onClick={props.onStep}
          disabled={props.running && !props.paused && !props.observing}
        />
        <IconButton
          icon={Square}
          title="Stop"
          onClick={props.onStop}
          disabled={!props.running && !props.paused}
        />
      </div>

      <div className="flex flex-col gap-1">
        <IconButton
          icon={PanelRight}
          title="Toggle properties"
          onClick={toggleProperties}
          active={showProperties}
        />
        <IconButton
          icon={theme === 'dark' ? Sun : Moon}
          title="Toggle theme"
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
        />
      </div>
    </div>
  );
}

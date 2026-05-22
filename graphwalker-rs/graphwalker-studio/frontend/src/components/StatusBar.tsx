import { useState, useEffect, useSyncExternalStore } from 'react';
import { AlertTriangle, X, Eye, Radio } from 'lucide-react';
import { useExecutionStore } from '@/store/execution-store';
import { useModelStore } from '@/store/model-store';
import { useSessionStore } from '@/store/session-store';
import { wsClient } from '@/client/websocket';

function useWsConnected(): boolean {
  return useSyncExternalStore(
    (cb) => wsClient.onStatus(cb),
    () => wsClient.connected,
  );
}

interface Props {
  onSubscribeSession: (sessionId: string) => void;
  onUnsubscribeSession: () => void;
}

export default function StatusBar({ onSubscribeSession, onUnsubscribeSession }: Props) {
  const running = useExecutionStore((s) => s.running);
  const paused = useExecutionStore((s) => s.paused);
  const issues = useExecutionStore((s) => s.issues);
  const setIssues = useExecutionStore((s) => s.setIssues);
  const checkIssues = useExecutionStore((s) => s.checkIssues);
  const setCheckIssues = useExecutionStore((s) => s.setCheckIssues);
  const fulfillment = useExecutionStore((s) => s.fulfillment);
  const hasModels = useModelStore((s) => s.models.length > 0);
  const connected = useWsConnected();

  const sessions = useSessionStore((s) => s.sessions);
  const subscribedSessionId = useSessionStore((s) => s.subscribedSessionId);
  const observing = useSessionStore((s) => s.observing);

  const [expanded, setExpanded] = useState(false);
  const [expandedSource, setExpandedSource] = useState<'issues' | 'check' | null>(null);
  const [showSessions, setShowSessions] = useState(false);

  useEffect(() => {
    if (issues.length > 0) { setExpanded(true); setExpandedSource('issues'); }
  }, [issues]);

  useEffect(() => {
    if (checkIssues.length > 0) { setExpanded(true); setExpandedSource('check'); }
  }, [checkIssues]);

  const vals = Object.values(fulfillment);
  const pct = vals.length === 0 ? 0 : vals.reduce((a, b) => a + b, 0) / vals.length;
  const hasIssues = issues.length > 0;
  const hasCheckIssues = checkIssues.length > 0;
  const checkOk = hasModels && !hasCheckIssues;

  const visibleIssues = expanded && expandedSource === 'issues' ? issues
    : expanded && expandedSource === 'check' ? checkIssues
    : [];
  const visibleLabel = expandedSource === 'issues' ? 'Execution Issues' : 'Model Check';
  const dismissVisible = () => {
    if (expandedSource === 'issues') setIssues([]);
    if (expandedSource === 'check') setCheckIssues([]);
    setExpanded(false);
    setExpandedSource(null);
  };

  return (
    <div className="shrink-0">
      {visibleIssues.length > 0 && (
        <div className="border-t border-border bg-surface max-h-48 overflow-y-auto">
          <div className="flex items-center justify-between px-3 py-1.5 bg-danger/10 border-b border-border">
            <span className="text-xs font-semibold text-danger flex items-center gap-1.5">
              <AlertTriangle size={12} />
              {visibleLabel} ({visibleIssues.length})
            </span>
            <button
              onClick={dismissVisible}
              className="text-text-muted hover:text-text p-0.5 rounded transition-colors"
              title="Dismiss"
            >
              <X size={12} />
            </button>
          </div>
          <ul className="px-3 py-1.5 space-y-0.5">
            {visibleIssues.map((issue, i) => (
              <li key={i} className="text-xs text-text-muted py-0.5 flex gap-2">
                <span className="text-danger shrink-0">&#x2022;</span>
                <span>{issue}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {showSessions && sessions.length > 0 && (
        <div className="border-t border-border bg-surface max-h-48 overflow-y-auto">
          <div className="flex items-center justify-between px-3 py-1.5 bg-primary/10 border-b border-border">
            <span className="text-xs font-semibold text-primary flex items-center gap-1.5">
              <Radio size={12} />
              Active Sessions ({sessions.length})
            </span>
            <button
              onClick={() => setShowSessions(false)}
              className="text-text-muted hover:text-text p-0.5 rounded transition-colors"
              title="Close"
            >
              <X size={12} />
            </button>
          </div>
          <ul className="px-3 py-1.5 space-y-0.5">
            {sessions.map((s) => {
              const active = subscribedSessionId === s.id;
              return (
                <li
                  key={s.id}
                  onClick={() => {
                    if (active) { onUnsubscribeSession(); }
                    else { onSubscribeSession(s.id); }
                    setShowSessions(false);
                  }}
                  className={`flex items-center gap-1.5 text-xs py-1 px-1.5 rounded cursor-pointer transition-colors ${
                    active
                      ? 'bg-primary/15 text-primary'
                      : 'text-text hover:bg-surface-alt'
                  }`}
                >
                  <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${active ? 'bg-primary' : 'bg-success animate-pulse'}`} />
                  <span className="flex-1">{s.name}</span>
                  {active && <Eye size={10} className="shrink-0" />}
                </li>
              );
            })}
          </ul>
        </div>
      )}

      <div className="flex items-center h-7 bg-surface border-t border-border px-3 text-xs">
        <div className="flex items-center gap-1.5 mr-3">
          <span
            className={`w-2 h-2 rounded-full ${connected ? 'bg-success' : 'bg-danger'}`}
            title={connected ? 'Connected' : 'Disconnected'}
          />
          <span className="text-text-muted">
            {connected ? 'Connected' : 'Disconnected'}
          </span>
        </div>

        {hasModels && (
          <button
            className="flex items-center gap-1.5 mr-3"
            onClick={() => {
              if (!hasCheckIssues) return;
              const show = !(expanded && expandedSource === 'check');
              setExpanded(show);
              setExpandedSource(show ? 'check' : null);
            }}
            title={checkOk ? 'Model OK' : `${checkIssues.length} model issue${checkIssues.length > 1 ? 's' : ''}`}
          >
            <span
              className={`w-2 h-2 rounded-full ${checkOk ? 'bg-success' : 'bg-danger'}`}
            />
            <span className={checkOk ? 'text-text-muted' : 'text-danger'}>
              {checkOk ? 'Model OK' : `${checkIssues.length} model issue${checkIssues.length > 1 ? 's' : ''}`}
            </span>
          </button>
        )}

        {sessions.length > 0 && (
          <button
            className="flex items-center gap-1.5 mr-3"
            onClick={() => setShowSessions(!showSessions)}
            title={`${sessions.length} active session${sessions.length > 1 ? 's' : ''}`}
          >
            <Radio size={12} className={observing ? 'text-primary' : 'text-text-muted'} />
            <span className={observing ? 'text-primary' : 'text-text-muted'}>
              {observing
                ? `Watching: ${sessions.find((s) => s.id === subscribedSessionId)?.name ?? 'session'}`
                : `Sessions (${sessions.length})`}
            </span>
          </button>
        )}

        {(running || paused) && (
          <div className="flex items-center gap-2 flex-1">
            <div className="w-48 h-1.5 bg-surface-alt rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-300 ${
                  hasIssues ? 'bg-danger' : 'bg-success'
                }`}
                style={{ width: `${Math.min(pct * 100, 100)}%` }}
              />
            </div>
            <span className="text-text-muted">{(pct * 100).toFixed(0)}%</span>
            {paused && <span className="text-warning">Paused</span>}
          </div>
        )}

        {hasIssues && (
          <button
            onClick={() => {
              const show = !(expanded && expandedSource === 'issues');
              setExpanded(show);
              setExpandedSource(show ? 'issues' : null);
            }}
            className="text-danger ml-auto flex items-center gap-1.5 hover:text-danger/80 transition-colors"
          >
            <AlertTriangle size={12} />
            {issues.length} issue{issues.length > 1 ? 's' : ''}
          </button>
        )}

        {!running && !paused && !hasIssues && (
          <span className="text-text-muted ml-auto">Ready</span>
        )}
      </div>
    </div>
  );
}

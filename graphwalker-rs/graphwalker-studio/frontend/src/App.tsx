import { useRef, useCallback, useEffect } from 'react';
import { useModelStore } from '@/store/model-store';
import { useExecutionStore } from '@/store/execution-store';
import { useEditorStore } from '@/store/editor-store';
import { useSessionStore } from '@/store/session-store';
import { wsClient } from '@/client/websocket';
import type { TestFile, GWModel } from '@/store/types';

import Sidebar from '@/components/Sidebar';
import EditorTabs from '@/components/EditorTabs';
import GraphEditor from '@/components/GraphEditor';
import PropertiesPanel from '@/components/PropertiesPanel';
import StatusBar from '@/components/StatusBar';

export default function App() {
  const models = useModelStore((s) => s.models);
  const selectedModelIndex = useModelStore((s) => s.selectedModelIndex);
  const addModel = useModelStore((s) => s.addModel);
  const loadModels = useModelStore((s) => s.loadModels);
  const closeModel = useModelStore((s) => s.closeModel);
  const selectModel = useModelStore((s) => s.selectModel);
  const selectElement = useModelStore((s) => s.selectElement);

  const running = useExecutionStore((s) => s.running);
  const paused = useExecutionStore((s) => s.paused);
  const delay = useExecutionStore((s) => s.delay);
  const setRunning = useExecutionStore((s) => s.setRunning);
  const setPaused = useExecutionStore((s) => s.setPaused);
  const recordVisit = useExecutionStore((s) => s.recordVisit);
  const loadSnapshot = useExecutionStore((s) => s.loadSnapshot);
  const hasBreakpoint = useExecutionStore((s) => s.hasBreakpoint);
  const setIssues = useExecutionStore((s) => s.setIssues);
  const setCheckIssues = useExecutionStore((s) => s.setCheckIssues);
  const reset = useExecutionStore((s) => s.reset);

  const showProperties = useEditorStore((s) => s.showProperties);
  const theme = useEditorStore((s) => s.theme);

  const observing = useSessionStore((s) => s.observing);
  const breakpoints = useExecutionStore((s) => s.breakpoints);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const runningRef = useRef(false);
  const delayRef = useRef(delay);
  delayRef.current = delay;

  useEffect(() => {
    const unsubBroadcast = wsClient.onBroadcast((msg) => {
      switch (msg.command) {
        case 'sessionCreated':
          useSessionStore.getState().addSession({
            id: msg.sessionId as string,
            name: msg.name as string,
          });
          break;
        case 'sessionEnded': {
          const endedId = msg.sessionId as string;
          const ss = useSessionStore.getState();
          if (ss.subscribedSessionId === endedId) {
            ss.setSubscribed(null);
            ss.setObserving(false);
            useExecutionStore.getState().setRunning(false);
          }
          ss.removeSession(endedId);
          break;
        }
        case 'visitedElement': {
          if (!useSessionStore.getState().observing) break;
          const modelId = msg.modelId as string;
          const elementId = msg.elementId as string;
          const fulfillment = (msg.stopConditionFulfillment as number) ?? 0;
          const totalCount = (msg.totalCount as number) ?? 0;
          const data = (msg.data as string) ?? '';
          useExecutionStore.getState().recordVisit(modelId, elementId, fulfillment, totalCount, data);

          const currentModels = useModelStore.getState().models;
          const modelIdx = currentModels.findIndex((m) => m.id === modelId);
          if (modelIdx >= 0 && modelIdx !== useModelStore.getState().selectedModelIndex) {
            useModelStore.getState().selectModel(modelIdx);
          }
          useModelStore.getState().selectElement(elementId);
          break;
        }
        case 'sessionPaused':
          if (useSessionStore.getState().observing) {
            useExecutionStore.getState().setPaused(true);
            useExecutionStore.getState().setRunning(false);
          }
          break;
        case 'sessionResumed':
          if (useSessionStore.getState().observing) {
            useExecutionStore.getState().setPaused(false);
            useExecutionStore.getState().setRunning(true);
          }
          break;
      }
    });

    return unsubBroadcast;
  }, []);

  useEffect(() => {
    wsClient.connect().then(() => {
      wsClient.send({ command: 'listSessions' }).then((resp) => {
        if (resp.success) {
          useSessionStore.getState().setSessions(
            (resp.sessions as Array<{ id: string; name: string }>) ?? [],
          );
        }
      }).catch(() => {});
    }).catch(() => {});
    const unsub = wsClient.onStatus((connected) => {
      if (!connected && !runningRef.current) {
        const timer = setInterval(() => {
          if (!wsClient.connected) {
            wsClient.connect().then(() => {
              wsClient.send({ command: 'listSessions' }).then((resp) => {
                if (resp.success) {
                  useSessionStore.getState().setSessions(
                    (resp.sessions as Array<{ id: string; name: string }>) ?? [],
                  );
                }
              }).catch(() => {});
            }).catch(() => {});
          } else {
            clearInterval(timer);
          }
        }, 5000);
        return () => clearInterval(timer);
      }
    });
    return unsub;
  }, []);

  useEffect(() => {
    if (!observing) return;
    const sid = useSessionStore.getState().subscribedSessionId;
    if (!sid) return;
    wsClient.send({ command: 'setDelay', sessionId: sid, value: delay }).catch(() => {});
  }, [delay, observing]);

  useEffect(() => {
    if (!observing) return;
    const sid = useSessionStore.getState().subscribedSessionId;
    if (!sid) return;
    wsClient.send({
      command: 'setBreakpoints',
      sessionId: sid,
      breakpoints: Array.from(breakpoints),
    }).catch(() => {});
  }, [breakpoints, observing]);

  const stepOnce = useCallback(async (): Promise<boolean> => {
    const hasNextResp = await wsClient.send({ command: 'hasNext' });
    if (!hasNextResp.hasNext) return false;

    const nextResp = await wsClient.send({ command: 'getNext' });
    const modelId = nextResp.modelId as string;
    const elementId = nextResp.elementId as string;
    const fulfillment = (nextResp.stopConditionFulfillment as number) ?? 0;
    const totalCount = (nextResp.totalCount as number) ?? 0;
    const data = (nextResp.data as string) ?? '';

    recordVisit(modelId, elementId, fulfillment, totalCount, data);

    const currentModels = useModelStore.getState().models;
    const modelIdx = currentModels.findIndex((m) => m.id === modelId);
    if (modelIdx >= 0 && modelIdx !== useModelStore.getState().selectedModelIndex) {
      selectModel(modelIdx);
    }
    selectElement(elementId);

    if (hasBreakpoint(modelId, elementId)) {
      setPaused(true);
      runningRef.current = false;
      return false;
    }

    return true;
  }, [recordVisit, selectModel, selectElement, hasBreakpoint, setPaused]);

  const runLoop = useCallback(async () => {
    runningRef.current = true;
    while (runningRef.current) {
      try {
        const cont = await stepOnce();
        if (!cont) {
          if (runningRef.current) {
            setRunning(false);
            runningRef.current = false;
          }
          break;
        }
        if (delayRef.current > 0) {
          await new Promise((r) => setTimeout(r, delayRef.current));
        }
      } catch (err) {
        const msg = err instanceof Error ? err.message : String(err);
        setIssues([msg]);
        setRunning(false);
        runningRef.current = false;
        break;
      }
    }
  }, [stepOnce, setRunning, setIssues]);

  const onPlay = useCallback(async () => {
    if (observing) {
      const sid = useSessionStore.getState().subscribedSessionId;
      if (sid) {
        setPaused(false);
        setRunning(true);
        wsClient.send({ command: 'resumeSession', sessionId: sid }).catch(() => {});
      }
      return;
    }

    if (paused) {
      setPaused(false);
      setRunning(true);
      runLoop();
      return;
    }

    if (models.length === 0) return;

    try {
      await wsClient.connect();
      const gwPayload: TestFile = { name: 'TEST', models };
      const { seed, autoSeed, globalData, setSeed } = useEditorStore.getState();
      const startMsg: Record<string, unknown> = { command: 'start', gw: gwPayload };
      if (!autoSeed && seed.trim()) startMsg.seed = Number(seed.trim());
      if (globalData.trim()) startMsg.globalData = globalData.trim();
      const resp = await wsClient.send(startMsg);
      if (resp.seed != null) setSeed(String(resp.seed));
      reset();
      setRunning(true);
      runLoop();
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      setIssues([msg]);
    }
  }, [models, paused, observing, setPaused, setRunning, reset, runLoop, setIssues]);

  const onPause = useCallback(() => {
    if (observing) {
      const sid = useSessionStore.getState().subscribedSessionId;
      if (sid) {
        setPaused(true);
        setRunning(false);
        wsClient.send({ command: 'pauseSession', sessionId: sid }).catch(() => {});
      }
      return;
    }
    runningRef.current = false;
    setPaused(true);
    setRunning(false);
  }, [observing, setPaused, setRunning]);

  const onStep = useCallback(async () => {
    if (observing) {
      const sid = useSessionStore.getState().subscribedSessionId;
      if (sid) {
        setPaused(true);
        setRunning(false);
        wsClient.send({ command: 'stepSession', sessionId: sid }).catch(() => {});
      }
      return;
    }

    if (!paused && !running) {
      try {
        await wsClient.connect();
        const gwPayload: TestFile = { name: 'TEST', models };
        const { seed, autoSeed, globalData, setSeed } = useEditorStore.getState();
        const startMsg: Record<string, unknown> = { command: 'start', gw: gwPayload };
        if (!autoSeed && seed.trim()) startMsg.seed = Number(seed.trim());
        if (globalData.trim()) startMsg.globalData = globalData.trim();
        const resp = await wsClient.send(startMsg);
        if (resp.seed != null) setSeed(String(resp.seed));
        reset();
        setPaused(true);
      } catch (err) {
        const msg = err instanceof Error ? err.message : String(err);
        setIssues([msg]);
        return;
      }
    }
    try {
      await stepOnce();
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      setIssues([msg]);
    }
  }, [models, paused, running, observing, reset, setPaused, setRunning, stepOnce, setIssues]);

  const onStop = useCallback(async () => {
    runningRef.current = false;
    const ss = useSessionStore.getState();
    if (ss.observing) {
      try {
        await wsClient.send({ command: 'unsubscribeSession' });
      } catch { /* ignore */ }
      ss.setSubscribed(null);
      ss.setObserving(false);
      reset();
      return;
    }
    reset();
    await wsClient.close();
  }, [reset]);

  const onSubscribeSession = useCallback(async (sessionId: string) => {
    try {
      await wsClient.connect();
      const resp = await wsClient.send({ command: 'subscribeSession', sessionId });
      if (resp.success) {
        const modelsPayload = resp.models as { models?: GWModel[] } | null;
        if (modelsPayload?.models) {
          loadModels(modelsPayload.models);
        }

        reset();

        const elements = (resp.elements as Array<{
          modelId: string;
          elementId: string;
          visitedCount: number;
        }>) ?? [];
        loadSnapshot(elements);

        const editorState = useEditorStore.getState();
        if (resp.seed != null) {
          editorState.setSeed(String(resp.seed));
          editorState.setAutoSeed(false);
        } else {
          editorState.setSeed('');
          editorState.setAutoSeed(true);
        }

        useSessionStore.getState().setSubscribed(sessionId);
        useSessionStore.getState().setObserving(true);

        if (resp.paused) {
          setPaused(true);
          setRunning(false);
        } else {
          setRunning(true);
        }
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      setIssues([msg]);
    }
  }, [loadModels, reset, loadSnapshot, setPaused, setRunning, setIssues]);

  const onUnsubscribeSession = useCallback(async () => {
    try {
      await wsClient.send({ command: 'unsubscribeSession' });
    } catch { /* ignore */ }
    useSessionStore.getState().setSubscribed(null);
    useSessionStore.getState().setObserving(false);
    reset();
  }, [reset]);

  const onOpenFile = useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const checkModels = useCallback(async (testFile: TestFile) => {
    try {
      await wsClient.connect();
      const resp = await wsClient.send({ command: 'check', gw: testFile });
      const issues = (resp.issues as string[] | undefined) ?? [];
      setCheckIssues(issues);
    } catch {
      // check is best-effort; don't block loading if websocket is unavailable
    }
  }, [setCheckIssues]);

  useEffect(() => {
    if (models.length === 0) {
      setCheckIssues([]);
      return;
    }
    const timer = setTimeout(() => {
      checkModels({ name: 'TEST', models });
    }, 500);
    return () => clearTimeout(timer);
  }, [models, checkModels, setCheckIssues]);

  const handleFileSelected = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const data = JSON.parse(reader.result as string) as TestFile;
          if (data.models) {
            loadModels(data.models);
            checkModels(data);
            const store = useEditorStore.getState();
            if (data.seed != null) {
              store.setSeed(String(data.seed));
              store.setAutoSeed(false);
            } else {
              store.setSeed('');
              store.setAutoSeed(true);
            }
          }
        } catch (err) {
          setIssues([`Failed to parse file: ${err}`]);
        }
      };
      reader.readAsText(file);
      e.target.value = '';
    },
    [loadModels, checkModels, setIssues],
  );

  const onSaveFile = useCallback(() => {
    if (models.length === 0) return;
    const { seed, autoSeed } = useEditorStore.getState();
    const data: TestFile = { models };
    if (!autoSeed && seed.trim()) data.seed = Number(seed.trim());
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'test.json';
    a.click();
    URL.revokeObjectURL(url);
  }, [models]);

  return (
    <div className={`flex h-full ${theme === 'light' ? 'theme-light' : ''}`}>
      <input
        ref={fileInputRef}
        type="file"
        accept=".json,.graphml"
        className="hidden"
        onChange={handleFileSelected}
      />

      <Sidebar
        onNewModel={addModel}
        onOpenFile={onOpenFile}
        onSaveFile={onSaveFile}
        onPlay={onPlay}
        onPause={onPause}
        onStep={onStep}
        onStop={onStop}
        running={running}
        paused={paused}
        observing={observing}
      />

      <div className="flex flex-col flex-1 min-w-0">
        <EditorTabs
          models={models}
          selectedIndex={selectedModelIndex}
          onSelect={selectModel}
          onClose={closeModel}
          onAdd={addModel}
        />

        <div className="flex flex-1 min-h-0">
          <div className="flex-1 min-w-0 relative">
            {models.length === 0 ? (
              <div className="flex items-center justify-center h-full text-text-muted">
                <div className="text-center space-y-2">
                  <p className="text-lg">GraphWalker Studio</p>
                  <p className="text-sm">Click on the New Model button on the left sidebar</p>
                </div>
              </div>
            ) : (
              models.map((m, i) => (
                <div
                  key={m.id}
                  className="absolute inset-0"
                  style={{ visibility: i === selectedModelIndex ? 'visible' : 'hidden' }}
                >
                  <GraphEditor model={m} modelIndex={i} />
                </div>
              ))
            )}
          </div>

          {showProperties && <PropertiesPanel />}
        </div>

        <StatusBar
          onSubscribeSession={onSubscribeSession}
          onUnsubscribeSession={onUnsubscribeSession}
        />
      </div>
    </div>
  );
}

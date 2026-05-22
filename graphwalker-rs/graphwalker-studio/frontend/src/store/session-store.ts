import { create } from 'zustand';

export interface Session {
  id: string;
  name: string;
}

interface SessionState {
  sessions: Session[];
  subscribedSessionId: string | null;
  observing: boolean;

  setSessions: (sessions: Session[]) => void;
  addSession: (session: Session) => void;
  removeSession: (sessionId: string) => void;
  setSubscribed: (sessionId: string | null) => void;
  setObserving: (observing: boolean) => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  sessions: [],
  subscribedSessionId: null,
  observing: false,

  setSessions: (sessions) => set({ sessions }),
  addSession: (session) =>
    set((s) => {
      if (s.sessions.some((x) => x.id === session.id)) return s;
      return { sessions: [...s.sessions, session] };
    }),
  removeSession: (sessionId) =>
    set((s) => ({
      sessions: s.sessions.filter((sess) => sess.id !== sessionId),
      subscribedSessionId: s.subscribedSessionId === sessionId ? null : s.subscribedSessionId,
      observing: s.subscribedSessionId === sessionId ? false : s.observing,
    })),
  setSubscribed: (sessionId) => set({ subscribedSessionId: sessionId }),
  setObserving: (observing) => set({ observing }),
}));

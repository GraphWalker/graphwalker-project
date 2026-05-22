export interface WsResponse {
  command: string;
  success: boolean;
  message?: string;
  [key: string]: unknown;
}

type StatusListener = (connected: boolean) => void;
type BroadcastListener = (msg: WsResponse) => void;

export class WebSocketClient {
  private socket: WebSocket | null = null;
  private url: string;
  private statusListeners: Set<StatusListener> = new Set();
  private broadcastListeners: Set<BroadcastListener> = new Set();
  private _connected = false;
  private pendingResolve: ((resp: WsResponse) => void) | null = null;
  private pendingReject: ((err: unknown) => void) | null = null;
  private pendingCommand: string | null = null;

  constructor(url: string) {
    this.url = url;
  }

  onStatus(listener: StatusListener): () => void {
    this.statusListeners.add(listener);
    listener(this._connected);
    return () => this.statusListeners.delete(listener);
  }

  onBroadcast(listener: BroadcastListener): () => void {
    this.broadcastListeners.add(listener);
    return () => this.broadcastListeners.delete(listener);
  }

  private setConnected(value: boolean) {
    if (this._connected === value) return;
    this._connected = value;
    for (const listener of this.statusListeners) listener(value);
  }

  private isResponseToRequest(response: WsResponse): boolean {
    if (!this.pendingCommand) return false;
    if (response.command === 'issues') return true;
    if (this.pendingCommand === 'getNext' && response.command === 'visitedElement') return true;
    if (this.pendingCommand === 'subscribeSession' && response.command === 'subscribeSession')
      return true;
    if (this.pendingCommand !== 'getNext' && this.pendingCommand === response.command) return true;
    return false;
  }

  async connect(): Promise<void> {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) return;
    await this.close();
    return new Promise((resolve, reject) => {
      this.socket = new WebSocket(this.url);
      let opened = false;

      this.socket.onopen = () => {
        opened = true;
        this.setConnected(true);
        resolve();
      };

      this.socket.onerror = () => {
        this.setConnected(false);
        if (!opened) {
          reject(new Error('WebSocket connection failed'));
        }
        if (this.pendingReject) {
          this.pendingReject(new Error('WebSocket error'));
          this.pendingResolve = null;
          this.pendingReject = null;
          this.pendingCommand = null;
        }
      };

      this.socket.onclose = () => {
        this.socket = null;
        this.setConnected(false);
      };

      this.socket.onmessage = (event: MessageEvent) => {
        const response: WsResponse = JSON.parse(event.data as string);

        if (this.pendingResolve && this.isResponseToRequest(response)) {
          if (response.command === 'issues') {
            this.pendingReject?.(response);
          } else {
            this.pendingResolve(response);
          }
          this.pendingResolve = null;
          this.pendingReject = null;
          this.pendingCommand = null;
          return;
        }

        for (const listener of this.broadcastListeners) {
          listener(response);
        }
      };
    });
  }

  async send(message: Record<string, unknown>): Promise<WsResponse> {
    return new Promise((resolve, reject) => {
      if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
        reject(new Error('WebSocket not connected'));
        return;
      }
      this.pendingResolve = resolve;
      this.pendingReject = reject;
      this.pendingCommand = message.command as string;
      this.socket.send(JSON.stringify(message));
    });
  }

  async close(): Promise<void> {
    if (!this.socket) return;
    return new Promise((resolve) => {
      if (!this.socket) {
        resolve();
        return;
      }
      this.socket.onclose = () => {
        this.socket = null;
        this.setConnected(false);
        resolve();
      };
      this.socket.close();
    });
  }

  get connected(): boolean {
    return this._connected;
  }
}

function getWsUrl(): string {
  const host = window.location.hostname || 'localhost';
  const port = (window as unknown as Record<string, unknown>).GW_WS_PORT ?? 9999;
  return `ws://${host}:${port}`;
}

export const wsClient = new WebSocketClient(getWsUrl());



export default class WebSocketClient {

  constructor(url) {
    this.url = url;
  }

  async connect() {
    // make sure there is no connection
    await this.close();
    // create a new connection
    return new Promise((resolve, reject) => {
      this.socket = new WebSocket(this.url);
      this.socket.onerror = reject;
      this.socket.onopen = (event) => {
        resolve(event);
      }
    })
  }

  async send(message) {
    return new Promise((resolve, reject) => {
      this.socket.onerror = reject;
      this.socket.onmessage = (event) => {
        const response = JSON.parse(event.data);
        if (message.command === "getNext" && response.command === 'visitedElement') {
          resolve(JSON.parse(event.data));
        } else if (response.command === 'issues') {
          reject(JSON.parse(event.data));
        } else if (message.command !== "getNext" && message.command === response.command) {
          resolve(JSON.parse(event.data));
        } 
      }
      this.socket.send(JSON.stringify(message));
    })
  }

  async close() {
    // return if the connection is already closed
    if (this.socket == null) {
      return Promise.resolve();
    }
    // close the current connection
    return new Promise((resolve, reject) => {
      this.socket.onerror = reject;
      this.socket.onclose = (event) => {
        this.socket = null;
        resolve(event);
      }
      this.socket.close();
    })
  }
}

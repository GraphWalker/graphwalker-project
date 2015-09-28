<connection-subpane>
  <h5>GraphWalker settings</h5>
  <ul>
    <li><input name="ws_url" disabled="{ connected }" type="text" />
    <button class="connect" onclick="{ toggleConnection }">{ connected ? 'Disconnect' : 'Connect' }</button></li>
    <li><a href="" onclick={ toggle('showTextarea') }>{showTextarea ? 'Hide' : 'Show'} connection log</a></li>
    <li show={showTextarea}><textarea name="output" readonly="true"></textarea></li>
  </ul>

  <style scoped>
    button.connect {
      width: 75px;
    }

    textarea[name='output'] {
      width: 285px;
      min-height: 100px;
      resize: vertical;
      border: 0;
      outline: none;
    }

    input[name='ws_url'] {
      width: 210px;
    }
  </style>

  var ConnectionActions = require('actions/ConnectionActions');

  var self = this;

  self.mixin('tagUtils');

  self.connected = false;
  self.showTextarea = false;


  self.on("mount", function() {
    self.ws_url.value = (window.debug ? 'ws://localhost:9999' : '');
    // Set up connection listeners
    ConnectionActions.addConnectionListener({
      onopen: function(websocket) {
        self.write('connection opened');
        self.connected = true;
        self.ws_url.value = websocket.url;
        self.update();
      },
      onclose: function() {
        self.write('disconnected');
        self.connected = false;
        self.update();
      },
      onmessage: function(message) {
        self.write(JSON.stringify(message));
      }
    });
  });

  toggleConnection() {
    self.connected ? self.disconnect() : self.connect();
  }
  connect() {
    var url = self.ws_url.value;
    self.write('connecting to', url);

    ConnectionActions.isSocketOpen(function(isOpen) {
      // Close existing connection before connecting anew
      if (isOpen) ConnectionActions.disconnect();
      ConnectionActions.connect(url);
    });
  }
  disconnect() {
    ConnectionActions.disconnect();
  }
  write() {
    self.output.value += '\n' + [].slice.call(arguments, 0).join(' ');
  }
</connection-subpane>

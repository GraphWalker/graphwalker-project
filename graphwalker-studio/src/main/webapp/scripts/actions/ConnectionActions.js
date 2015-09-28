define(['app/RiotControl', 'constants/ConnectionConstants'],
function() {
  'use strict';

  var Constants       = require('constants/ConnectionConstants');
  var RiotControl     = require('app/RiotControl');

  var CALLS  = Constants.calls;
  var EVENTS = Constants.events;

  return {
    // Listeners

    /**
    * Subscribes to onopen, onclosed and onmessage
    * @param {object} handlers - object with handlers
    */
    addConnectionListener: function(handlers) {
      var onopen = handlers.onopen;
      if (onopen) {
        RiotControl.on(EVENTS.CONNECTION_ESTABLISHED, onopen);
      }

      var onclose = handlers.onclose;
      if (onclose) {
        RiotControl.on(EVENTS.CONNECTION_CLOSED, onclose);
      }

      var onmessage = handlers.onmessage;
      if (onmessage) {
        RiotControl.on(EVENTS.INCOMING_MESSAGE, onmessage);
      }
    },

    // Triggers
    getWebSocket: function(callback) {
      RiotControl.trigger(CALLS.GET_WEBSOCKET, callback);
    },
    isSocketOpen: function(callback) {
      this.getWebSocket(function(websocket) {
        // !!var coerces var to boolean
        callback(!!websocket);
      });
    },
    connect: function(url, callback) {
      RiotControl.trigger(CALLS.CONNECT, url);
      // callback will receive the websocket upon connection
      if (callback) this.addConnectionListener({onopen: callback});
    },
    disconnect: function(callback) {
      RiotControl.trigger(CALLS.CLOSE);
      // callback will receive the websocket upon connection
      if (callback) this.addConnectionListener({onclose: callback});
    },
    send: function(message) {
      RiotControl.trigger(CALLS.SEND, message);
    },
    startReading: function(callback) {
      RiotControl.on(EVENTS.INCOMING_MESSAGE, callback);
    },
    stopReading: function(callback) {
      RiotControl.off(EVENTS.INCOMING_MESSAGE, callback);
    },
    readNext: function(callback) {
      RiotControl.one(EVENTS.INCOMING_MESSAGE, callback);
    },
    readUntil: function(callback) {
      var _this = this;
      var onmessage = function(message) {
        // The return value of the callback func determines whether
        // it should continue listening for messages or stop.
        var stop = callback(message);
        if (stop) {
          // unbind onmessage listener
          _this.stopReading(onmessage);
        }
      };
      // bind onmessage
      this.startReading(onmessage);
    }
  }
});

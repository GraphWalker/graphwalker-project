define(['riot', 'constants/VertexConstants', 'app/RiotControl', 'jquery', 'jsplumb', 'store/Utils'],
 function() {
  'use strict';

  var $           = require('jquery');
  var jsp         = require('jsplumb');
  var riot        = require('riot');
  var Utils       = require('store/Utils');
  var Constants   = require('constants/VertexConstants');
  var RiotControl = require('app/RiotControl');

  function VertexStore() {
    var self = riot.observable(this);

    // Register store with RiotControl. All subsequent `trigger` and `on` method calls through
    // RiotControl will be passed on to this store.
    RiotControl.addStore(self);

    // DATA STORE
    self.vertices = [];

    // Utils
    var _getVertex = Utils.getElement.bind(undefined, self.vertices);

    // Event listeners
    var CALLS = Constants.calls;
    var EVENTS = Constants.events;
    var EMIT_CHANGE = EVENTS.VERTEX_LIST_CHANGED;

    self.on(CALLS.GET_ALL_VERTICES, function(callback) {
      callback(self.vertices)
    });

    self.on(CALLS.GET_VERTEX, function(vertexId, callback) {
      callback(_getVertex(vertexId));
    });

    self.on(CALLS.ADD_VERTEX, function(vertex) {
      self.vertices.push(vertex);
    });

    self.on(CALLS.CHANGE_VERTEX, function(query, props) {
      var vertex = _getVertex(query);
      $.extend(true, vertex, props);
      self.trigger(EMIT_CHANGE, self.vertices);
    });

    self.on(CALLS.REMOVE_VERTEX, function(vertices, callback) {
      vertices.forEach(function(el) {
        var vertex = _getVertex(el);
        self.vertices.remove(vertex);
      });

      // HACK: riot/#1003 workaround. Prevents vertex labels switching DOM nodes.
      self.trigger(EMIT_CHANGE, []);
      self.trigger(EMIT_CHANGE, self.vertices);
      callback();
    });

  }

  return new VertexStore();
});

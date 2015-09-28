define(['app/RiotControl', 'constants/VertexConstants',
'jquery', './EdgeActions', 'constants/StudioConstants'],
function() {
  'use strict';

  var $               = require('jquery');
  var Constants       = require('constants/VertexConstants');
  var RiotControl     = require('app/RiotControl');
  var EdgeActions     = require('actions/EdgeActions');
  var StudioConstants = require('constants/StudioConstants');

  var CALLS  = Constants.calls;
  var EVENTS = Constants.events;
  var STATUS = StudioConstants.status;

  var counter = 0; // 'A'

  return {
    // Listeners
    addChangeListener: function(callback) {
      RiotControl.on(EVENTS.VERTEX_LIST_CHANGED, callback);
    },

    // Triggers
    getAll: function(callback) {
      RiotControl.trigger(CALLS.GET_ALL_VERTICES, callback);
    },
    get: function(vertexId, callback) {
      RiotControl.trigger(CALLS.GET_VERTEX, vertexId, callback);
    },
    add: function(newVertex) {
      // Give vertex temporary ID if not already set
      newVertex.id = newVertex.id || 'v_' + ++counter;
      newVertex.type = StudioConstants.types.T_VERTEX;
      RiotControl.trigger(CALLS.ADD_VERTEX, newVertex);

      newVertex.name = newVertex.name || 'Vertex ' + counter;
      var props = {name: newVertex.name};
      RiotControl.trigger(CALLS.CHANGE_VERTEX, newVertex.id, props);
    },
    setProps: function(query, props) {
      RiotControl.trigger(CALLS.CHANGE_VERTEX, query, props);
    },
    remove: function(vertexIds) {
      if (!Array.isArray(vertexIds)) vertexIds = [vertexIds];
      RiotControl.trigger(CALLS.REMOVE_VERTEX, vertexIds, function() {
        EdgeActions.getForVertices(vertexIds, function(edgesToRemove) {
          EdgeActions.remove(edgesToRemove);
        });
      });

      // Clear selection to refresh the properties pane
      RiotControl.trigger(StudioConstants.calls.CLEAR_SELECTION);
    },
    getDomId: function(idArray, callback) {
      if (!idArray || idArray.length === 0) {
        callback([]);
        return;
      }
      if (!Array.isArray(idArray)) idArray = [idArray];
      // TODO instead of using a counter, use promises
      var DomIdDictionary = {_counter: 0};
      idArray.forEach(function(el) {
        this.get(el, function(vertex) {
          console.assert(vertex, 'Couldn\'t fetch vertex for id', el);
          DomIdDictionary[el] = vertex.view.domId;
          if (++DomIdDictionary._counter === idArray.length) callback(DomIdDictionary);
        });
      }, this);
    }
  }
});

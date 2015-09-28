define(['app/RiotControl', 'constants/EdgeConstants', 'jquery', 'jsplumb', 'constants/StudioConstants'],
function() {
  'use strict';

  var $               = require('jquery');
  var jsp             = require('jsplumb');
  var Constants       = require('constants/EdgeConstants');
  var RiotControl     = require('app/RiotControl');
  var StudioConstants = require('constants/StudioConstants');

  var CALLS  = Constants.calls;
  var EVENTS = Constants.events;
  var STATUS = StudioConstants.status;


  var counter = 0;

  return {
    // Listeners
    addChangeListener: function(callback) {
      RiotControl.on(EVENTS.EDGE_LIST_CHANGED, callback);
    },

    // Triggers
    getAll: function(callback) {
      RiotControl.trigger(CALLS.GET_ALL_EDGES, callback);
    },
    get: function(edgeId, callback) {
      RiotControl.trigger(CALLS.GET_EDGE, edgeId, callback);
    },
    add: function(newEdge) {
      // Give edge temporary ID if not already set
      newEdge.id = newEdge.id || 'e_' + ++counter;
      newEdge.name = newEdge.name || 'Edge ' + counter;

      newEdge.type = StudioConstants.types.T_EDGE;
      RiotControl.trigger(CALLS.ADD_EDGE, newEdge);
    },
    setProps: function(query, props) {
      RiotControl.trigger(CALLS.CHANGE_EDGE, query, props);
    },
    remove: function(edgeIds) {
      if (!Array.isArray(edgeIds)) edgeIds = [edgeIds];
      RiotControl.trigger(CALLS.REMOVE_EDGE, edgeIds);

      // Clear selection to refresh the properties pane
      RiotControl.trigger(StudioConstants.calls.CLEAR_SELECTION);
    },
    getForVertices: function(vertexIds, callback) {
      if (!Array.isArray(vertexIds)) vertexIds = [vertexIds];
      this.getAll(function(allEdges) {
        var results = allEdges.filter(function(el) {
          return vertexIds.contains(el.sourceVertexId) || vertexIds.contains(el.targetVertexId);
        });
        callback(results);
      });
    }
  }
});

define(['riot', 'app/RiotControl', 'constants/GraphWalkerConstants', 'actions/GraphWalkerActions',
'constants/VertexConstants', 'constants/EdgeConstants', 'constants/ModelConstants'],
function() {
  'use strict';

  var riot            = require('riot');
  var Actions         = require('actions/GraphWalkerActions');
  var Constants       = require('constants/GraphWalkerConstants');
  var RiotControl     = require('app/RiotControl');
  var EdgeConstants   = require('constants/EdgeConstants');
  var VertexConstants = require('constants/VertexConstants');
  var ModelConstants  = require('constants/ModelConstants');


  function GraphWalkerStore() {
    var self = riot.observable(this);

    // Register store with RiotControl. All subsequent `trigger` and `on` method calls through
    // RiotControl will be passed on to this store.
    RiotControl.addStore(self);

    // Event listeners
    var VCALLS   = VertexConstants.calls;
    var ECALLS   = EdgeConstants.calls;
    var MCALLS   = ModelConstants.calls;

    // React to VertexStore events
    self.on(VCALLS.ADD_VERTEX, function(vertex) {
      Actions.addVertex(vertex);
    });

    self.on(VCALLS.CHANGE_VERTEX, function(query, props) {
      Actions.updateVertex(query, props);
    });

    self.on(VCALLS.REMOVE_VERTEX, function(vertices) {
      vertices.forEach(Actions.removeVertex);
    });

    // React to EdgeStore events
    self.on(ECALLS.ADD_EDGE, function(edge) {
      Actions.addEdge(edge);
    });

    self.on(ECALLS.CHANGE_EDGE, function(query, props) {
      // Determine whether the change should be verified with GW
      if (props.name) Actions.updateEdge(query, props);
    });

    self.on(ECALLS.REMOVE_EDGE, function(edges) {
      edges.forEach(Actions.removeEdge);
    });

    // React to ModelStore events
    self.on(MCALLS.ADD_MODEL, function(model) {
      Actions.addModel(model);
    });
  }

  return new GraphWalkerStore();
});

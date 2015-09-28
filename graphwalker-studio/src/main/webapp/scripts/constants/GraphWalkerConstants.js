define(function() {

  var constants = {};


  // Keys might not always be the same as values. Change
  // only the values upon GW API change etc.
  constants.methods = {
    'ADDMODEL': 'addModel',
    'ADDVERTEX': 'addVertex',
    'UPDATEVERTEX': 'updateVertex',
    'ADDEDGE': 'addEdge',
    'UPDATEEDGE': 'updateEdge',
    'START': 'startRunning',
    'NEXT': 'getNextElement',
    'STOP': 'stopRunning'
  }

  return constants;
});

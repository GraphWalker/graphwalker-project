define(['constants/Utils'], function(Utils) {

  var Enum = Utils.Enum;
  var constants = {};

  // Keys might not always be the same as values. Change
  // only the values upon GW API change etc.
  constants.methods = {
    'ADDMODEL': 'addModel',
    'ADDVERTEX': 'addVertex',
    'REMOVEVERTEX': 'removeVertex',
    'UPDATEVERTEX': 'updateVertex',
    'ADDEDGE': 'addEdge',
    'REMOVEEDGE': 'removeEdge',
    'UPDATEEDGE': 'updateEdge',
    'START': 'startRunning',
    'NEXT': 'getNextElement',
    'STOP': 'stopRunning'
  }

  return constants;
});

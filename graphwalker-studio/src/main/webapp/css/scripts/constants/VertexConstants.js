define(['constants/Utils'], function(Utils) {

  var Enum = Utils.Enum;
  var constants = {};

  constants.calls = new Enum([
    'GET_ALL_VERTICES',
    'GET_VERTEX',
    'ADD_VERTEX',
    'REMOVE_VERTEX',
    'CHANGE_VERTEX'
  ]);

  constants.events = new Enum([
    'VERTEX_LIST_CHANGED'
  ]);

  return constants;
});

define(['constants/Utils'], function(Utils) {

  var Enum = Utils.Enum;
  var constants = {};

  constants.calls = new Enum([
    'GET_ALL_MODELS',
    'GET_MODEL',
    'ADD_MODEL',
    'REMOVE_MODEL',
    'CHANGE_MODEL'
  ]);

  constants.events = new Enum([
    'MODEL_LIST_CHANGED'
  ]);


  return constants;
});

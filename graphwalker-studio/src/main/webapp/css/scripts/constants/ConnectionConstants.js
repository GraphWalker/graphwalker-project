define(['constants/Utils'], function(Utils) {

  var Enum = Utils.Enum;
  var constants = {};

  constants.events = new Enum([
    'CONNECTION_ESTABLISHED',
    'CONNECTION_CLOSED',
    'INCOMING_MESSAGE'
  ]);

  constants.calls = new Enum([
    'GET_WEBSOCKET',
    'CONNECT',
    'CLOSE',
    'SEND',
    'READ'
  ]);


  return constants;
});

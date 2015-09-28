define(['riot', 'constants/ModelConstants', 'app/RiotControl', 'jquery', 'jsplumb', 'store/Utils'],
 function() {
  'use strict';

  var $           = require('jquery');
  var jsp         = require('jsplumb');
  var riot        = require('riot');
  var Utils       = require('store/Utils');
  var Constants   = require('constants/ModelConstants');
  var RiotControl = require('app/RiotControl');

  function ModelStore() {
    var self = riot.observable(this);

    // Register store with RiotControl. All subsequent `trigger` and `on` method calls through
    // RiotControl will be passed on to this store.
    RiotControl.addStore(self);

    // DATA STORE
    self.models = [];

    // Utils
    var _getModel = Utils.getElement.bind(undefined, self.models);

    // Event listeners
    var CALLS = Constants.calls;
    var EVENTS = Constants.events;
    var EMIT_CHANGE = EVENTS.MODEL_LIST_CHANGED;

    self.on(CALLS.GET_ALL_MODELS, function(callback) {
      callback(self.models)
    });

    self.on(CALLS.GET_MODEL, function(modelId, callback) {
      callback(_getModel(modelId));
    });

    self.on(CALLS.ADD_MODEL, function(model) {
      self.models.push(model);
      self.trigger(EMIT_CHANGE, self.models);
    });

    self.on(CALLS.CHANGE_MODEL, function(query, props) {
      var model = _getModel(query);
      $.extend(true, model, props);
      self.trigger(EMIT_CHANGE, self.models);
    });

    self.on(CALLS.REMOVE_MODEL, function(query) {
      var model = _getModel(query);
      self.models.remove(model);
      self.trigger(EMIT_CHANGE, self.models);
    });

  }

  return new ModelStore();
});

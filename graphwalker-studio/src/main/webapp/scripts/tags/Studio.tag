<studio>
  <studio-sidebar selection={ selection } model={ model } options={ opts }
    vertices={ vertices } edges={ edges } models={ models }/>

  <studio-tabs tabs={ tabs } model={ model } />

  <studio-canvas selection={ selection } model={ model } show={ tabs.length } options={ opts }
    vertices={ vertices } edges={ edges }/>

  <style>
    studio {
      height: 98%;
      display: block;
      background-color: #dedede;
      margin-bottom: 10px;
    }
  </style>

  var jsp               = require('jsplumb');
  var EdgeActions       = require('actions/EdgeActions');
  var RiotControl       = require('app/RiotControl');
  var ModelActions      = require('actions/ModelActions');
  var VertexActions     = require('actions/VertexActions');
  var StudioConstants   = require('constants/StudioConstants');
  var ConnectionActions = require('actions/ConnectionActions');

  var self = this;

  // STATE-HOLDING VARIABLES

  self.vertices = [];
  self.edges = [];
  self.models = [];

  VertexActions.getAll(function(vertices) {
    self.vertices = vertices;
  });
  VertexActions.addChangeListener(function(vertices) {
    self.vertices = vertices;
    updateElements(self.vertices, false, true);
    self.update();
  });

  EdgeActions.getAll(function(edges) {
    self.edges = edges;
  });
  EdgeActions.addChangeListener(function(edges) {
    self.edges = edges;
    updateElements(self.edges, false, true);
    self.update();
  });

  ModelActions.addChangeListener(function(models) {
    self.models = models;
    self.update();
  });

  // Update element objects in selection
  // TODO: change selection to only hold references (id) to objects like self.tabs?
  var updateElements = function(collection) {
    var _sel = self.selection;
    self.selection.update(_sel.map(function(el) {
      return collection.getBy('id', el.id)[0] || el;
    }));
  };

  // STATE-HOLDING VARIABLES WITH EXTRA HELPER METHODS

  // SELECTION
  self.selection = [];
  Object.defineProperty(self, 'selection', { writable: false }); // Prevent from overwriting object
  self.selection.clear = function(preventUpdate) {
    this.constructor.prototype.clear.apply(this);
    if (!preventUpdate) self.update();
  };
  self.selection.update = function(elements, toggle, preventUpdate) {
    // If `elements` is falsy, clear selection
    if (!elements || elements.length === 0) {

      // If selection already is null prevent update.
      if (this.length === 0) return;

      this.clear(true);
    } else {
      if (!Array.isArray(elements)) elements = [elements]; // Wrap single element into array

      if (toggle) {
        // If element isn't currently selected, add
        // it to selection otherwise deselect it.
        elements.forEach(function(element) {
          self.selection.toggle(element);
        });
      } else {
        this.clear(true);
        this.push.apply(this, elements); // concatenates the array in place
      }

    }
    if (!preventUpdate) self.update();
  }.bind(self.selection);

  // TABS
  self.tabs = [];
  Object.defineProperty(self, 'tabs', { writable: false }); // Prevent from overwriting object
  self.tabs.open = function(modelId, preventUpdate) {
    if (!this.contains(modelId)) {
      // Open existing model and set it as active
      this.push(modelId);
      if (!preventUpdate) self.update();
    }
  }.bind(self.tabs);
  self.tabs.getObjects = function() {
    return this.map(function(modelId) {
      return self.models.getBy('id', modelId)[0];
    });
  }.bind(self.tabs);
  self.tabs.close = function(modelId) {
    var index = this.indexOf(modelId);

    // Select different tab if currently selected tab is the one being closed
    if (!self.model.id || self.model.id === modelId) {
      // Try selecting model immediately next to the left
      var next = index - 1;
      next = next < 0 ? 1 : next;
      self.model.set(this[next]);
    }
    this.splice(index, 1);
    self.update();
  }.bind(self.tabs);

  // CURRENT MODEL
  var _modelHelperFunctions = {
    // Helper setter for calling from children
    set: function(model) {
      self.model = model;
    },
    // Create new model and set it as active
    new: function() {
      ModelActions.add({}, function(model) {
        self.model.set(model.id);
      });
    },
    // Load existing model
    load: function(model) {
      ModelActions.add(model, function(model) {
        self.model.set(model.id);
      });
    }
  };
  Object.defineProperty(self, 'model', {
    get: function() {
      var model = self.models.getBy('id', this._modelId)[0];
      return $.extend({}, model, _modelHelperFunctions);
    },
    set: function(modelId) {
      // HACK: riot/#1003 workaround. Prevents vertex labels switching DOM nodes.
      this._modelId = '';
      this.update();

      if (modelId) {
        this._modelId = modelId;
        self.tabs.open(modelId, true);
        this.selection.clear();

        // Restore pan position // TODO: belongs in Canvas.tag
        if (self.model.view && self.model.view.panzoom) {
          $('#canvas-body').panzoom('setMatrix', self.model.view.panzoom);
        } else {
          $('#canvas-body').panzoom('reset', { animate: false });
        }
      }
    }
  });

  // Handle passed in options
  self.on('mount', function() {
    if (opts.autoConnect && opts.autoConnect.enabled) {
      ConnectionActions.connect(opts.autoConnect.url);
    }
  });

  RiotControl.on(StudioConstants.calls.CLEAR_SELECTION, function() {
    self.selection.clear();
  });

</studio>

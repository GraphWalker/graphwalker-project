<vertex id="{ view.domId }" class="{ selected: selected } { status.toLowerCase() }" tabindex="1"
  vertex-id="{ id }">
  <div class="label-div">
    <editable type="text" class="label" off={ !selected } callback={ changeName } >{ name }</editable>
  </div>

  <style>
  vertex {
    background-clip: padding-box;
    border: 1px solid #325262;
    position: absolute !important;
    display: table !important;
    border-radius: 15px;
    cursor: default;
    background-color: rgba(192, 215, 221, 0.85);
    box-sizing: border-box;
  }

  vertex:focus {
    outline: none;
  }

  vertex.selected {
    border: 2px solid #3db2e3;
    cursor: move;
  }

  vertex.rubberband-hover {
    border: 2px solid #21cfdf;
  }

  vertex.unverified {
    border: 1px dashed #325262;
  }

  vertex.error {
    background-color: rgba(255, 200, 128, 0.85);
  }

  .label-div {
    display: table-cell;
    vertical-align: middle;
    text-align: center;
    padding: 10px;
  }

  .label {
    margin: 0;
    display: inline-block;
    min-width: 20px;
    min-height: 10pt;
  }

  .label:hover, .label:focus {
    background-color: rgba(210, 245, 248, 0.75);
    background-clip: content-box;
    outline: none;
  }

  .label::selection {
    background-color: #00c7c0;
  }

  .jsplumb-drag-hover {
    border: 1px solid #21cfdf;
  }

  vertex input {
    width: 90px;
  }
  </style>

  var $                = require('jquery');
  var jsp              = require('jsplumb');
  var Constants        = require('constants/VertexConstants');
  var StudioConstants  = require('constants/StudioConstants');
  var VertexActions    = require('actions/VertexActions');
  var ActionUtils      = require('actions/Utils');

  var self = this;
  var $root;

  var GRID_SIZE = 20;

  self.defaults = {
    label: self.id,
    status: StudioConstants.status.UNVERIFIED,
    view: {
      domId: 'd_'+self.id,
      width: 120,
      height: 80
    }
  };

  self.one('update', function() {
    // TODO: write custom extend func without overwrite
    // (i.e. extend self with defaults but dont overwrite)
    var merged = $.extend(true, {}, self.defaults, self);
    $.extend(true, self, merged);

    if (!self.view.top || !self.view.left) {
      // Calculate and set offset
      var position = {
        'top': (function() {
          var pos = self.view.centerY - (self.view.height / 2);
          return GRID_SIZE * Math.floor(pos / GRID_SIZE);
        })(),
        'left': (function() {
          var pos = self.view.centerX - (self.view.width / 2);
          return GRID_SIZE * Math.floor(pos / GRID_SIZE);
        })()
      };
      $.extend(self.view, position);

      // Store dimensions and offset, and a reference to the DOM element in the model
      VertexActions.setProps(self.id, self.view);
    }

    if (!self.view.css) {
      // Create custom css property getter
      Object.defineProperty(self.view, 'css', {
        get: function() {
          return {
            'height': this.height,
            'width' : this.width,
            'top'   : this.top,
            'left'  : this.left
          };
        }
      });
    }
  });

  self.on('mount', function() {
    $root = $(self.root);

    // Hide the element until everything is set, especially dimensions and offset
    $root.hide();

    // Make into jsPlumb source & target
    jsp.makeSource(self.root);
    jsp.makeTarget(self.root);

    // Make draggable
    jsp.draggable(self.root, {
      containment: true,
      snapThreshold: 10,
      grid: [GRID_SIZE,GRID_SIZE],
      filter: ".ui-resizable-handle",
      start: function(params) {
        // Avoid setting listeners on vertices not being directly
        // dragged (i.e. dragged as part of selection but not under
        // the cursor => hence will not trigger click anyway)
        var isElementBeingDragged = params.e;
        if (!isElementBeingDragged) return;

        // Avoid resetting the selection by triggering the click
        // handler on mouseup.
        self.root.addEventListener('click', function handler(e) {
          e.stopPropagation();
          this.removeEventListener('click', handler, true);
        }, true);
      },
      stop: function(params) {
        var updatePositionInModel = function() {
          //VertexActions.setProps(self.id, {left: params.pos[0], top: params.pos[1]});
          VertexActions.setProps(self.id, {view: {left: params.pos[0], top: params.pos[1]}});
        };
        ActionUtils.bufferedAction(updatePositionInModel, 'jsp.draggable.stop', params.selection.length);
      }
    });

    // Make resizable
    $root.resizable({
      grid: [GRID_SIZE,GRID_SIZE],
      resize: function(e, ui) {
        // Clear the offset and size cache of jsp and repaint the vertex.
        // This prevents endpoints from appearing at pre-resize offsets.
        jsp.revalidate(ui.element.get(0));
      },
      stop: function(e, ui) {
        // Update the vertex dimensions
        VertexActions.setProps(self.id, ui.size);
      }
    });

    // Make selectable on focus and on click
    $root.on('focus click', function(e) {
      // Toggle if meta key was down during the click.
      var toggle = e.type === 'click' ? e.metaKey : false;
      self.opts.selection.update(self, toggle);
    });

    // MouseEvent multiplexing. Trigger click as usual, trigger
    // mousedown-n-drag only after the cursor has left the element.
    self.handleEvent = function(evt) {
      switch(evt.type) {
        case 'mousedown':
          // Stop propagation (i.e. triggering other handlers set by e.g. jsp)
          evt.stopPropagation();
          // Prevent setting focus (which would trigger the select handler)
          evt.preventDefault();
          self.root.addEventListener('mouseleave', self, true);
          self.root.addEventListener('mouseup', self, true);
          break;

        case 'mouseup':
          self.root.removeEventListener('mouseleave', self, true);
          self.root.removeEventListener('mouseup', self, true);
          break;

        case 'mouseleave':
          // Don't trigger when hovering over child elements, e.g. label
          if (evt.target != self.root) break;

          self.root.removeEventListener('mouseleave', self, true);
          self.root.removeEventListener('mouseup', self, true);

          // Allow the `mousedown` event to propagate
          self.root.removeEventListener('mousedown', self, true);


          // Make sure connection endpoints start precisely at the edge of the
          // vertex by trimming any offset caused by lag between mouse drag
          // and the mouseup event.
          var vertexDimensions = evt.target.getBoundingClientRect();
          var _e = $.extend({}, evt, {
            clientY: (function() {
              if (evt.clientY > vertexDimensions.bottom) return vertexDimensions.bottom;
              if (evt.clientY < vertexDimensions.top) return vertexDimensions.top;
            })(),
            clientX: (function() {
              if (evt.clientX > vertexDimensions.right) return vertexDimensions.right;
              if (evt.clientX < vertexDimensions.left) return vertexDimensions.left;
            })()
          });

          // Re-trigger mousedown event
          self.root.dispatchEvent(new MouseEvent('mousedown', _e));

          // Reactivate our event multiplexer
          self.root.addEventListener('mousedown', self, true);
          break;
      }
    };
    self.root.addEventListener('mousedown', self, true);

    // Revalidate to set the correct offset for dragging connections
    setTimeout(function() {
      // Run inside setTimeout to schedule it at the end of the
      // event queue so that the DOM redrawing has a chance to
      // catch up.
      jsp.revalidate(self.root);
    }, 0);
    // Trigger `updated` to set draggable/source/resize properties
    self.trigger('updated');
  });

  self.on('update', function() {
    self.selected = opts.selection.mapBy('id').contains(self.id);
    self.resizable = opts.selection.length === 1;
  });

  self.on('updated', function() {
    if ($root) {
      // Update dimenions and offset
      $root.show().css(self.view.css);

      // Set vertex id on the DOM element (used e.g. in rubberband selection)
      self.root['_vertexObject'] = self;

      // Selection-based settings
      var selected = self.selected;
      var resizable = selected && self.resizable;

      /**  __________________________
       *  | FUNCTION      | SELECTED |
       *  | MouseEvent mux| Off      |
       *  | SourceEnabled | Off      |
       *  | Draggable     | On       |
       *  | Resizable     | On       |
       *   --------------------------
       */

      // SourceEnabled
      jsp.setSourceEnabled(self.root, !selected);

      // Draggable
      jsp.setDraggable(self.root, selected);

      // Resizable
      $root.resizable(resizable ? 'enable' : 'disable');
      $root.children('.ui-resizable-handle').toggle(resizable);

      // MouseEvent mux
      var modifyEventListener = selected ? self.root.removeEventListener : self.root.addEventListener;
      modifyEventListener.call(self.root, 'mousedown', self, true);
    }
  });

  self.on('unmount', function() {
    jsp.remove(self.root);
  });

  changeName(newValue) {
    var props = {name: newValue};
    VertexActions.setProps(self.id, props);
  }
</vertex>

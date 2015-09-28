<studio-canvas class="{ highlight: !selection.length }">
  <div class="zoom-button" id="zoom-in"><span class="octicon octicon-plus"></span></div>
  <div class="zoom-button" id="zoom-out"><span class="octicon octicon-dash"></span></div>
  <input type="range" id="zoom-range" step="0.05" min="0.1" max="3">
  <div id="canvas-body">
    <vertex each={ filterByModel(opts.vertices) } selection={ parent.opts.selection } />
    <edge each={ filterByModel(opts.edges) } selection={ parent.opts.selection } />
  </div>
  <div id="minimap" if={ opts.options.canvas.minimap }>
    <div class="minimap-element" each={ filterByModel(opts.vertices) } data-view={ JSON.stringify(this.view) }
    style="display:none;"></div>
    <div id="viewport"></div>
  </div>

  <style>
    studio-canvas {
      height: calc(100% - 34px);
      box-sizing: border-box;
      display: block;
      margin-right: 310px;
      border: 1px solid #5b8590;
      overflow: hidden;
      position: relative;
    }
    #canvas-body {
      background: #f9f9f9;
      background-image: url('grid.png');
      background-blend-mode: overlay;
      position: absolute;
      -webkit-backface-visibility: initial !important;
      -webkit-transform-origin: 50% 50%;
    }
    .zoom-button {
      text-align: center;
      background-color: #325262;
      color: white;
      width: 20px;
      height: 20px;
      position: absolute;
      top: 10px;
      z-index: 1;
      border-radius: 4px;
    }
    .zoom-button .octicon {
      font-size: 11px;
    }
    #zoom-range {
      position: absolute;
      top: 100px;
      right: -42px;
      z-index: 1;
      transform: rotate(270deg);
      -webkit-transform: rotate(270deg);
    }
    #zoom-in {
      right: 5px;
    }
    #zoom-out {
      right: 27px;
    }
    #minimap {
      border: 1px solid black;
      position: absolute;
      right: 5px;
      bottom: 5px;
      background-color: rgb(203, 203, 203);
      opacity: 0.6;
    }
    #minimap > #viewport {
      border: 2px solid red;
      background-color: white;
      position: absolute;
      box-sizing: border-box;
    }
    #minimap > .minimap-element {
      position: absolute;
      background-color: #1c4105;
      z-index: 1;
    }
  </style>

  var $                 = require('jquery');
  var jsp               = require('jsplumb');
  var RiotControl       = require('app/RiotControl');
  var VertexActions     = require('actions/VertexActions');
  var EdgeActions       = require('actions/EdgeActions');
  var ModelActions      = require('actions/ModelActions');
  var ActionUtils       = require('actions/Utils');
  var StudioConstants   = require('constants/StudioConstants');
  var ConnectionActions = require('actions/ConnectionActions');
  var rubberband        = require('utils/rubberband');

  var LEFT_BUTTON  = 0;
  var RIGHT_BUTTON = 2;
  var ALT_KEY    = 91;
  var ALT_KEY_FF = 224; // Firefox uses a different keycode for ALT for some reason.
  var SPACEBAR   = 32;
  var SHIFT_KEY  = 16;

  // Canvas dimensions
  var CANVAS_SIZE = 10000;
  var MINIMAP_SIZE = 160;

  var self = this;


  addVertex(e) {
    // Prepare vertex object
    var vertex = {
      modelId: opts.model.id,
      view: {
        centerY: e.offsetY,
        centerX: e.offsetX
      }
    }
    // Dispatch action
    VertexActions.add(vertex);
  }

  addEdge(sourceDomId, targetDomId) {
    var sourceVertexId = $('#'+sourceDomId).attr('vertex-id');
    var targetVertexId = $('#'+targetDomId).attr('vertex-id');
    var edge = {
      modelId: opts.model.id,
      sourceDomId: sourceDomId,
      targetDomId: targetDomId,
      sourceVertexId: sourceVertexId,
      targetVertexId: targetVertexId
    };
    EdgeActions.add(edge);
  }

  filterByModel(elements) {
    return elements.filter(function(el) { return el.modelId === opts.model.id });
  }

  self.on('mount', function() {
    // Set canvas dimensions and center it
    $('#canvas-body').css({
      height: CANVAS_SIZE,
      width: CANVAS_SIZE,
      top: -CANVAS_SIZE/2,
      left: -CANVAS_SIZE/2
    });

    // Init jsPlumb
    jsp.ready(function() {
      // Defaults
      jsp.importDefaults({
        Endpoint: ['Dot', {radius: 2}],
        Anchor: 'Continuous',
        Connector: [
          'StateMachine', {
            curviness: 0,
            proximityLimit: 260
        }],
        HoverPaintStyle: {strokeStyle: '#21cfdf', lineWidth: 1 },
        PaintStyle: {strokeStyle: '#325262', lineWidth: 1, dashstyle: "4 2" },
        ConnectionOverlays: [
            [ 'Arrow', {
                location: 1,
                id: 'arrow',
                length: 12,
                foldback: 0.1
            } ],
            [ 'Label', { id: 'label', cssClass: 'edge-label', events: {
              click: function(label) {
                $(label.getElement()).find('editable').click();
              }
            } }]
        ]
      });

      // Register connection types
      jsp.registerConnectionType('selected', {
        paintStyle: {strokeStyle: '#3db2e3', lineWidth: 2 }
      });
      jsp.registerConnectionType('verified', {
        paintStyle: {strokeStyle: '#325262', lineWidth: 1, dashstyle: "0 0" }
      });
      jsp.registerConnectionType('error', {
        paintStyle: {strokeStyle: '#ffc880', lineWidth: 1, dashstyle: "0 0" }
      });

      // Set canvas as container
      jsp.setContainer('canvas-body');

      // Move connection creation logic to `edge` tag.
      jsp.bind('beforeDrop', function(params) {
        self.addEdge(params.sourceId, params.targetId);
        return false;
      });

      // Selecting edges
      jsp.bind('click', function(connection, evt) {
        var edge = connection.getParameter('_edgeObject');
        self.opts.selection.update(edge, evt.metaKey);
      });
    });

    // Append rubberband listener
    rubberband('#canvas-body', 'vertex', function(selectedVertices, append) {
      // Dispatch it to end of event queue so that it is not
      // overriden by the onClick handler below.
      setTimeout(function() {
        self.opts.selection.update(selectedVertices.mapBy('_vertexObject'), append);
      }, 0);
    });

    // Set up event listeners
    $('#canvas-body')
      // Add new vertices on double click
      .on('dblclick', function(e) {
        if (e.target === this && !e.metaKey) self.addVertex(e);
      })
      // Deselect vertices on click
      .on('click', function(e) {
        if (e.target === this) self.opts.selection.clear();
      })
      .on('mousedown', function(e) {
        // Create rubberband on left click-n-drag
        if (e.button === LEFT_BUTTON) {
          $(this).trigger('rubberband', e);
        } else {
          $(this)
            .css('cursor', 'grabbing')
            .css('cursor', '-webkit-grabbing')
            .one('mouseup', function() {
              $(this).css('cursor', 'default');
            })
        }
      })
      .on('contextmenu', function(e) {
        e.preventDefault();
      });

    // Set up panning & zooming
    var _updateModel = function() {
      // Store pan position in model
      ActionUtils.timeBufferedAction(function() {
        ModelActions.setProps(opts.model.id, {
          view: {
            panzoom: $('#canvas-body').panzoom('getMatrix')
          }
        });
      }, 'model.update.panzoom', 400);
    };
    $('#canvas-body').panzoom({
      cursor: 'default',
      contain: 'invert', // Don't show what's behind canvas
      minScale: 0.14,
      maxScale: 3,
      onEnd: _updateModel,
      $zoomIn: $('#zoom-in'),
      $zoomOut: $('#zoom-out'),
      $zoomRange: $('#zoom-range'),
      onZoom: function(e, pz, scale) {
        jsp.setZoom(scale);
        _updateModel();
      },
      onChange: function(e, pz, matrix) {
        // Container dimensions need to be updated once the canvas is drawn.
        // Will trigger only once.
        if (!pz.container.width) $('#canvas-body').panzoom('resetDimensions');

        // Update minimap
        var zoom = matrix[0];
        var panOffsetLeft = matrix[4];
        var panOffsetTop = matrix[5];
        var scalingFactor = MINIMAP_SIZE / CANVAS_SIZE;
        var canvas = $('studio-canvas')[0];
        var body = $('#canvas-body')[0];
        $('#viewport').css({
          height: canvas.clientHeight * scalingFactor / zoom,
          width: canvas.clientWidth * scalingFactor / zoom,
          top: -1 * body.offsetTop * scalingFactor - panOffsetTop * scalingFactor / zoom,
          left: -1 * body.offsetLeft * scalingFactor - panOffsetLeft * scalingFactor / zoom
        });
      },
      onReset: function() {
        // Find bounding box of all vertices and set zoom and pan around it
        if (self.opts.vertices.length) {
          // Get bounding box
          var bounds = {
            left: CANVAS_SIZE,
            top: CANVAS_SIZE,
            right: 0,
            bottom: 0,
            get size() {
              return {
                width: this.right - this.left,
                height: this.bottom - this.top
              }
            },
            get center() {
              return {
                x: this.size.width/2 + this.left,
                y: this.size.height/2 + this.top,
              }
            }
          };
          self.filterByModel(self.opts.vertices).forEach(function(el) {
            bounds.left   = Math.min(bounds.left, el.view.left);
            bounds.top    = Math.min(bounds.top, el.view.top);
            bounds.right  = Math.max(bounds.right, el.view.left + el.view.width);
            bounds.bottom = Math.max(bounds.bottom, el.view.top + el.view.height);
          });

          var viewport = {
            height: this.offsetParent.clientHeight,
            width: this.offsetParent.clientWidth
          };

          // Calculate pan
          var CENTER_OFFSET = 0;
          var pan = {
            x: CANVAS_SIZE/2 - bounds.center.x + viewport.width/2 - CENTER_OFFSET,
            y: CANVAS_SIZE/2 - bounds.center.y + viewport.height/2 - CENTER_OFFSET
          }
          $(this).panzoom('pan', pan.x, pan.y);

          // Calculate zoom
          var ZOOM_OFFSET = {
            x: this.offsetParent.offsetLeft,
            y: this.offsetParent.offsetTop
          };
          var ZOOM_MARGIN = 0.02; // Provide a margin between elements and canvas walls
          var zoom = Math.min(
            viewport.height / bounds.size.height,
            viewport.width  / bounds.size.width
          );
          $(this).panzoom('zoom', zoom > 0 ? Math.min(zoom - ZOOM_MARGIN, 1) : 1, {
            focal: {
              clientX: (CANVAS_SIZE + viewport.width)/2 + ZOOM_OFFSET.x,
              clientY: (CANVAS_SIZE + viewport.height)/2 + ZOOM_OFFSET.y
            }
          });

        }
        _updateModel();
      }
    })
    // Mousewheel zooming (doesn't support Firefox)
    .on('mousewheel', function( e ) {
      // Don't scroll container
      e.preventDefault();

      // Zoom in or out?
      var delta = e.delta || e.originalEvent.wheelDelta;
      var zoomOut = delta ? delta < 0 : e.originalEvent.deltaY > 0;
      $('#canvas-body').panzoom('zoom', zoomOut, {
        increment: opts.options.canvas.scrollIncrement,
        focal: {
          clientX: e.clientX + CANVAS_SIZE/2,
          clientY: e.clientY + CANVAS_SIZE/2
        },
        animate: false
      });
    })
    // Alt-click zooming
    $('body').on('keydown', function(e) {
      if (e.target != this) return;
      if (e.keyCode === ALT_KEY || e.keyCode === ALT_KEY_FF) {
        var zoomOut = false;
        var zoomHandler = function (e) {
          // Don't zoom on right click or when clicking elements
          if (e.button === RIGHT_BUTTON || e.target != this) return;
          $('#canvas-body').panzoom('zoom', zoomOut, {
            focal: {
              clientX: e.clientX + CANVAS_SIZE/2,
              clientY: e.clientY + CANVAS_SIZE/2
            },
            animate: true
          });
        };
        var zoomOutHandler = function(e) {
          if (e.keyCode === SHIFT_KEY) {
            zoomOut = true;
            $('#canvas-body')
              .css('cursor', 'zoom-out')
              .css('cursor', '-webkit-zoom-out');
          }
        };
        var keyUpHandler = function(e) {
          if (e.keyCode === ALT_KEY  || e.keyCode === ALT_KEY_FF) {
            // Remove all listeners
            $('#canvas-body')
              .css('cursor', 'default')
              .off('mousedown', zoomHandler)
            $(this)
              .off('keydown', zoomOutHandler)
              .off('keyup', keyUpHandler);
          } else if (e.keyCode === SHIFT_KEY) {
            zoomOut = false;
            $('#canvas-body')
              .css('cursor', 'zoom-in')
              .css('cursor', '-webkit-zoom-in');
          }
        };

        // Set listeners
        $('#canvas-body')
          .css('cursor', 'zoom-in')
          .css('cursor', '-webkit-zoom-in')
          .on('mousedown', zoomHandler);
        $(this)
          .on('keydown', zoomOutHandler)
          .on('keyup', keyUpHandler);
      } else if (e.keyCode === SPACEBAR) {
        // Reset pan and zoom on spacebar press
        $('#canvas-body').panzoom('reset');
      }
    })
    // Fix contain dimensions upon browser window resize
    $(window).on('resize', function() {
      $('#canvas-body').panzoom('resetDimensions');
    });

    // Mount minimap
    $('#minimap').css({
      height: MINIMAP_SIZE,
      width: MINIMAP_SIZE
    });

    // Set key listeners
    // Select all vertices
    key('command+a', function() {
      self.opts.selection.update(opts.vertices);
    });
    // Remove selected vertices
    key('backspace, delete', function() {
      VertexActions.remove(opts.selection.filter(function(el) {
        return el.type === StudioConstants.types.T_VERTEX;
      }).mapBy('id'));
      return false; // Don't trigger default browser event
    });
    // Enter name editing mode on selected vertex
    key('enter', function() {
      if (self.opts.selection.length > 1) return;
      switch (self.opts.selection[0].type) {
        case StudioConstants.types.T_VERTEX:
          var domId = self.opts.selection[0].view.domId;
          $('#'+domId).find('editable').click();
          break;
        case StudioConstants.types.T_EDGE:
          var label = self.opts.selection[0]._jsp_connection.getOverlay('label').getElement();
          $(label).find('editable').click();
          break;
      }
    });

  });

  self.on('updated', function() {
    var selection = self.opts.selection.mapBy('view.domId');
    jsp.clearDragSelection();
    jsp.addToDragSelection(selection);

    // Update minimap elements
    var scalingFactor = MINIMAP_SIZE / CANVAS_SIZE;
    $('.minimap-element').each(function(i, el) {
      var view = JSON.parse($(el).attr('data-view'));
      $(el).css({
        top:  view.top * scalingFactor,
        left: view.left * scalingFactor,
        width: Math.max(view.width * scalingFactor, 2),
        height: Math.max(view.height * scalingFactor, 2)
      }).show();
    });
  });

  // RUN TEST
  // self.one('mount', function() {
  //   var Test = require('tests/CanvasTest')(this);
  //   ConnectionActions.connect('ws://localhost:9999', function() {
  //     Test.testAll(26,30);
  //   });
  // })
</studio-canvas>

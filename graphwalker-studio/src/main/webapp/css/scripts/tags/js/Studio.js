(function(tagger) {
  if (typeof define === 'function' && define.amd) {
    define(['riot'], function(riot) { tagger(riot); });
  } else if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
    tagger(require('riot'));
  } else {
    tagger(window.riot);
  }
})(function(riot) {
'use strict';
riot.tag('canvas-settings-subpane', '<h5>Canvas settings</h5> <ul> <li> Scroll zoom sensitivity<br> <input name="sensitivity" type="range" onchange="{ setSensitivity }"> <span id="sensValue"></span> </li> <li> Show minimap <input name="minimap" type="checkbox" onchange="{ setMinimap }"> </li> </ul>', function(opts) {

  var self = this;

  self.on('mount', function() {
    $.extend(self.sensitivity, {
      max: 1,
      min: 0.01,
      step: 0.01,
      value: opts.options.canvas && opts.options.canvas.scrollIncrement || 0.3
    });
    self.setSensitivity();

    self.minimap.checked = self.opts.options.canvas.minimap;
  });

  this.setSensitivity = function() {
    var sensValue = self.sensValue.innerHTML = self.sensitivity.value;
    $.extend(true, self.opts.options, {canvas: {scrollIncrement: sensValue}})
  }.bind(this);

  this.setMinimap = function() {
    $.extend(true, self.opts.options, {canvas: {minimap: self.minimap.checked}})
    riot.update();
  }.bind(this);


});
riot.tag('studio-canvas', '<div class="zoom-button" id="zoom-in"><span class="octicon octicon-plus"></span></div> <div class="zoom-button" id="zoom-out"><span class="octicon octicon-dash"></span></div> <input type="range" id="zoom-range" step="0.05" min="0.1" max="3"> <div id="canvas-body"> <vertex each="{ filterByModel(opts.vertices) }" selection="{ parent.opts.selection }"></vertex> <edge each="{ filterByModel(opts.edges) }" selection="{ parent.opts.selection }"></edge> </div> <div id="minimap" if="{ opts.options.canvas.minimap }"> <div class="minimap-element" each="{ filterByModel(opts.vertices) }" data-view="{ JSON.stringify(this.view) }" style="display:none;"></div> <div id="viewport"></div> </div>', 'studio-canvas { height: calc(100% - 34px); box-sizing: border-box; display: block; margin-right: 310px; border: 1px solid #5b8590; overflow: hidden; position: relative; } #canvas-body { background: #f9f9f9; background-image: url(\'grid.png\'); background-blend-mode: overlay; position: absolute; -webkit-backface-visibility: initial !important; -webkit-transform-origin: 50% 50%; } .zoom-button { text-align: center; background-color: #325262; color: white; width: 20px; height: 20px; position: absolute; top: 10px; z-index: 1; border-radius: 4px; } .zoom-button .octicon { font-size: 11px; } #zoom-range { position: absolute; top: 100px; right: -42px; z-index: 1; transform: rotate(270deg); -webkit-transform: rotate(270deg); } #zoom-in { right: 5px; } #zoom-out { right: 27px; } #minimap { border: 1px solid black; position: absolute; right: 5px; bottom: 5px; background-color: rgb(203, 203, 203); opacity: 0.6; } #minimap > #viewport { border: 2px solid red; background-color: white; position: absolute; box-sizing: border-box; } #minimap > .minimap-element { position: absolute; background-color: #1c4105; z-index: 1; }', 'class="{ highlight: !selection.length }"', function(opts) {

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

  var CANVAS_SIZE = 10000;
  var MINIMAP_SIZE = 160;

  var self = this;


  this.addVertex = function(e) {

    var vertex = {
      modelId: opts.model.id,
      view: {
        centerY: e.offsetY,
        centerX: e.offsetX
      }
    }

    VertexActions.add(vertex);
  }.bind(this);

  this.addEdge = function(sourceDomId, targetDomId) {
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
  }.bind(this);

  this.filterByModel = function(elements) {
    return elements.filter(function(el) { return el.modelId === opts.model.id });
  }.bind(this);

  self.on('mount', function() {

    $('#canvas-body').css({
      height: CANVAS_SIZE,
      width: CANVAS_SIZE,
      top: -CANVAS_SIZE/2,
      left: -CANVAS_SIZE/2
    });

    jsp.ready(function() {

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

      jsp.registerConnectionType('selected', {
        paintStyle: {strokeStyle: '#3db2e3', lineWidth: 2 }
      });
      jsp.registerConnectionType('verified', {
        paintStyle: {strokeStyle: '#325262', lineWidth: 1, dashstyle: "0 0" }
      });
      jsp.registerConnectionType('error', {
        paintStyle: {strokeStyle: '#ffc880', lineWidth: 1, dashstyle: "0 0" }
      });

      jsp.setContainer('canvas-body');

      jsp.bind('beforeDrop', function(params) {
        self.addEdge(params.sourceId, params.targetId);
        return false;
      });

      jsp.bind('click', function(connection, evt) {
        var edge = connection.getParameter('_edgeObject');
        self.opts.selection.update(edge, evt.metaKey);
      });
    });

    rubberband('#canvas-body', 'vertex', function(selectedVertices, append) {


      setTimeout(function() {
        self.opts.selection.update(selectedVertices.mapBy('_vertexObject'), append);
      }, 0);
    });

    $('#canvas-body')

      .on('dblclick', function(e) {
        if (e.target === this && !e.metaKey) self.addVertex(e);
      })

      .on('click', function(e) {
        if (e.target === this) self.opts.selection.clear();
      })
      .on('mousedown', function(e) {

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

    var _updateModel = function() {

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


        if (!pz.container.width) $('#canvas-body').panzoom('resetDimensions');

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

        if (self.opts.vertices.length) {

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

          var CENTER_OFFSET = 0;
          var pan = {
            x: CANVAS_SIZE/2 - bounds.center.x + viewport.width/2 - CENTER_OFFSET,
            y: CANVAS_SIZE/2 - bounds.center.y + viewport.height/2 - CENTER_OFFSET
          }
          $(this).panzoom('pan', pan.x, pan.y);

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

    .on('mousewheel', function( e ) {

      e.preventDefault();

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

    $('body').on('keydown', function(e) {
      if (e.target != this) return;
      if (e.keyCode === ALT_KEY || e.keyCode === ALT_KEY_FF) {
        var zoomOut = false;
        var zoomHandler = function (e) {

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

        $('#canvas-body')
          .css('cursor', 'zoom-in')
          .css('cursor', '-webkit-zoom-in')
          .on('mousedown', zoomHandler);
        $(this)
          .on('keydown', zoomOutHandler)
          .on('keyup', keyUpHandler);
      } else if (e.keyCode === SPACEBAR) {

        $('#canvas-body').panzoom('reset');
      }
    })

    $(window).on('resize', function() {
      $('#canvas-body').panzoom('resetDimensions');
    });

    $('#minimap').css({
      height: MINIMAP_SIZE,
      width: MINIMAP_SIZE
    });


    key('command+a', function() {
      self.opts.selection.update(opts.vertices);
    });

    key('backspace, delete', function() {
      VertexActions.remove(opts.selection.filter(function(el) {
        return el.type === StudioConstants.types.T_VERTEX;
      }).mapBy('id'));
      return false; // Don't trigger default browser event
    });

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








});
riot.tag('connection-subpane', '<h5>GraphWalker settings</h5> <ul> <li><input name="ws_url" __disabled="{ connected }" type="text"> <button class="connect" onclick="{ toggleConnection }">{ connected ? \'Disconnect\' : \'Connect\' }</button></li> <li><a href="" onclick="{ toggle(\'showTextarea\') }">{showTextarea ? \'Hide\' : \'Show\'} connection log</a></li> <li show="{showTextarea}"><textarea name="output" readonly="true"></textarea></li> </ul>', 'connection-subpane button.connect, [riot-tag="connection-subpane"] button.connect{ width: 75px; } connection-subpane textarea[name=\'output\'], [riot-tag="connection-subpane"] textarea[name=\'output\']{ width: 285px; min-height: 100px; resize: vertical; border: 0; outline: none; } connection-subpane input[name=\'ws_url\'], [riot-tag="connection-subpane"] input[name=\'ws_url\']{ width: 210px; }', function(opts) {

  var ConnectionActions = require('actions/ConnectionActions');

  var self = this;

  self.mixin('tagUtils');

  self.connected = false;
  self.showTextarea = false;


  self.on("mount", function() {
    self.ws_url.value = (window.debug ? 'ws://localhost:9999' : '');

    ConnectionActions.addConnectionListener({
      onopen: function(websocket) {
        self.write('connection opened');
        self.connected = true;
        self.ws_url.value = websocket.url;
        self.update();
      },
      onclose: function() {
        self.write('disconnected');
        self.connected = false;
        self.update();
      },
      onmessage: function(message) {
        self.write(JSON.stringify(message));
      }
    });
  });

  this.toggleConnection = function() {
    self.connected ? self.disconnect() : self.connect();
  }.bind(this);
  this.connect = function() {
    var url = self.ws_url.value;
    self.write('connecting to', url);

    ConnectionActions.isSocketOpen(function(isOpen) {

      if (isOpen) ConnectionActions.disconnect();
      ConnectionActions.connect(url);
    });
  }.bind(this);
  this.disconnect = function() {
    ConnectionActions.disconnect();
  }.bind(this);
  this.write = function() {
    self.output.value += '\n' + [].slice.call(arguments, 0).join(' ');
  }.bind(this);

});
riot.tag('edge', '<editable type="text" callback="{ changeName }" off="{ !selected }">{ name }</editable>', '.edge-label { background-color: white; opacity: 0.8; padding: 5px; border: 1px solid black; border-radius: 2px; min-width: 10px; min-height: 8pt; } .edge-label:empty:not(:hover):not(:focus) { opacity: 0; }', 'id="{ id }" source="{ sourceVertexId }" target="{ targetVertexId }"', function(opts) {

  var $ = require('jquery');
  var jsp = require('jsplumb');
  var Constants = require('constants/EdgeConstants');
  var EdgeActions = require('actions/EdgeActions');
  var StudioConstants = require('constants/StudioConstants');

  var self = this;
  self.defaults = {
    status: StudioConstants.status.UNVERIFIED
  };

  self.one('update', function() {


    var merged = $.extend(true, {}, self.defaults, self);
    $.extend(true, self, merged);
  });

  self.on('mount', function() {
    self.connection = jsp.connect({source: self.sourceDomId, target: self.targetDomId});
    self.connection.setParameter('_edgeObject', self);

    var labelElement = $(self.root).find('editable').detach();
    $(self.connection.getOverlay('label').getElement()).append(labelElement);

    if (self.sourceDomId === self.targetDomId)
      setTimeout(function() {jsp.revalidate(self.sourceDomId)}, 0);

    EdgeActions.setProps(self.id, {_jsp_connection: self.connection});

    self.trigger('updated');
  });

  self.on('update', function() {
    self.selected = self.opts.selection.mapBy('id').contains(self.id);
  });

  self.on('updated', function() {

    var connection = self.connection;
    var SELECTED = 'selected';
    if (connection && connection.connector) {
      connection.clearTypes();
      connection.addType(self.status.toLowerCase());
      if (self.selected) connection.addType(SELECTED);
    }
  })

  self.on('unmount', function() {
    if (self.connection.connector) jsp.detach(self.connection);
  });

  this.changeName = function(newValue) {
    var props = {name: newValue};
    EdgeActions.setProps(self.id, props);
  }.bind(this);

});
riot.tag('editable', '<span show="{ !editing }"> <yield></yield> </span>', 'editable span, [riot-tag="editable"] span{ cursor: pointer; }', 'onclick="{ click }"', function(opts) {

  var ENTER_KEY = 13;
  var ESC_KEY = 27;

  var self = this;

  self.editing = false;

  self.on('mount', function() {

    var editControl = (function() {
      switch (self.opts.type) {
        case 'text':
          return $('<input>').attr({ type: 'text', name: 'editable', autofocus: true});
      }
    })();

    editControl.on('change blur keydown', function(e) {
      switch (e.type) {
        case 'keydown':
          if (e.keyCode === ENTER_KEY || e.keyCode === ESC_KEY) this.blur();
          break;
        case 'change':

          self.opts.callback(e.target.value);
        case 'blur':
          self.editing = false;
          self.update();
      }
    });

    $(self.root).append(editControl);
    editControl.hide();

    self.editControl = editControl;
  });

  self.on('updated', function() {
    if (self.isMounted) {
      self.editControl.toggle(self.editing);
      if (self.editing) self.editControl.select();
    }
  });

  this.click = function(e) {

    e.preventUpdate = true;

    if (self.editing || opts.off) return;

    self.editing = true;
    self.editControl.val(self.root.innerText);

    self.update();
  }.bind(this);

});
riot.tag('graphwalker-pane', '<ul> <li if="{ errorMessage }"> <div class="bg-warning"><span class="octicon octicon-alert"></span> { errorMessage }</div> </li> <li if="{ successMessage }"> <div class="bg-success"><span class="octicon octicon-check"></span> { successMessage }</div> </li> <li><b>Connection status:</b><br> { opts.connected ? \'Connected\' : \'Disconnected\' }</li> <li> <button show="{ opts.connected && opts.model.id && !running }" onclick="{ startRunning }" class="green"> <span class="octicon octicon-rocket"></span> Run model </button> <button show="{ opts.connected && running }" onclick="{ stopRunning }" class="red"> <span class="octicon octicon-primitive-square"></span> Stop </button> </li> </ul>', function(opts) {

  var Actions         = require('actions/GraphWalkerActions');
  var EdgeActions     = require('actions/EdgeActions');
  var VertexActions   = require('actions/VertexActions');
  var StudioConstants = require('constants/StudioConstants');

  var self = this;

  self.running = false;

  self.on('mount', function() {
    var headerElement = $(self.root).parents('sidebar-pane').find('h4');
    self.statusIcon = $('<span>')
      .addClass('octicon octicon-primitive-dot')
      .css('transition', 'color 400ms ease-out 100ms')
      .css('color', '#cd2828')
      .appendTo(headerElement);
  });

  self.on('updated', function() {
    if (self.isMounted) {
      self.statusIcon.css({
        'color': opts.connected ? '#15da52' : '#cd2828'
      });
    }
  });

  this.startRunning = function() {
    self.running = true;
    delete self.errorMessage;
    delete self.successMessage;
    var modelId = opts.model.id;
    Actions.startRunningModel(modelId, function(success, response) {
      if (!success) {
        self.stopRunning();
        self.errorMessage = response;
        self.update();
      } else {
        if (response.next) {
          switch (response.type) {
            case StudioConstants.types.T_VERTEX:
              VertexActions.get(response.next, opts.selection.update);
              break;
            case StudioConstants.types.T_EDGE:
              EdgeActions.get(response.next, opts.selection.update);
              break;
          }
        } else {
          self.stopRunning();
          self.successMessage = response.message;
          self.update();
        }
      }
    });
  }.bind(this);

  this.stopRunning = function() {
    self.running = false;
    Actions.stopRunningModel();
  }.bind(this);


});
riot.tag('models-pane', '<ul> <li if="{ !opts.models.length }"> <button onclick="{ opts.model.new }" class="green"> <span class="octicon octicon-plus"></span> New model </button> <button onclick="{ openFileDialog }"> <span class="octicon octicon-cloud-upload"></span> Load model </button> <input type="file" name="fileUpload" show="{ false }" onchange="{ loadModel }"> </li> <li if="{ opts.models.length }"> <input type="text" name="searchInput" placeholder="Search" onkeyup="{ search }"> <button onclick="{ clearSearch }">Clear</button> </li> <li if="{ opts.models.length }"> <a href="" onclick="{ expandAll }">Expand all</a> <a href="" onclick="{ hideAll }">Collapse all</a> </li> </ul> <ul class="models"> <li each="{ model in opts.models }" class="{ active: parent.opts.model.id === model.id}"> <span onclick="{ toggleExpand }" class="octicon octicon-chevron-{ !parent.collapsed.contains(model.id) ? \'down\' : \'right\' }"></span> <a class="{ active: parent.opts.model.id === model.id}" onclick="{ openModel }"> { model.name } </a> <ul if="{ !parent.collapsed.contains(model.id) }"> <li each="{ filterByModel(parent.opts.vertices, model).filter(searchFilter) }"> <a class="vertex { selected: parent.parent.opts.selection.mapBy(\'id\').contains(id) }" onclick="{ select }">{ name }</a> </li> <li each="{ filterByModel(parent.opts.edges, model).filter(searchFilter) }"> <a class="edge { selected: parent.parent.opts.selection.mapBy(\'id\').contains(id) }" onclick="{ select }">{ name }</a> </li> </ul> </li> </ul>', 'models-pane a, [riot-tag="models-pane"] a{ color: inherit; cursor: pointer; } models-pane a.active, [riot-tag="models-pane"] a.active{ background-color: rgba(55, 157, 200, 0.4); } models-pane a.selected, [riot-tag="models-pane"] a.selected{ background-color: rgba(55, 157, 200, 0.75); } models-pane li.active, [riot-tag="models-pane"] li.active{ background-color: rgba(91, 133, 144, 0.2); } models-pane ul.models, [riot-tag="models-pane"] ul.models{ list-style: none; background-color: #f0f0f0; color: black; overflow-y: auto; max-height: 350px; border-radius: 2px; } models-pane ul.models span.octicon, [riot-tag="models-pane"] ul.models span.octicon{ padding: 5px 0px 0px 10px; } models-pane input[name=\'searchInput\'], [riot-tag="models-pane"] input[name=\'searchInput\']{ width: 238px; }', function(opts) {


  var VertexActions   = require('actions/VertexActions');
  var EdgeActions     = require('actions/EdgeActions');
  var ModelActions    = require('actions/ModelActions');
  var StudioConstants = require('constants/StudioConstants');

  var self = this;

  self.collapsed = [];
  self.searchQuery = '';

  this.filterByModel = function(elements, model) {
    return elements.filter(function(el) { return el.modelId === model.id });
  }.bind(this);

  this.toggleExpand = function(e) {
    var modelId = e.item.model.id;
    self.collapsed.toggle(modelId);
  }.bind(this);

  this.hideAll = function() {
    self.collapsed = self.opts.models.mapBy('id');
  }.bind(this);

  this.expandAll = function() {
    self.collapsed = [];
  }.bind(this);

  this.select = function(e) {
    e.preventUpdate = true; // Update is called by selection.update
    var element = e.item;
    self.opts.model.set(element.modelId);
    opts.selection.update(element);
  }.bind(this);

  this.openModel = function(e) {
    self.opts.model.set(e.item.model.id);
  }.bind(this);

  this.searchFilter = function(el) {
    return !self.searchQuery ? true : new RegExp(self.searchQuery).test(el.name);
  }.bind(this);

  this.search = function() {
    if (!self.collapsedBeforeSearch) self.collapsedBeforeSearch = self.collapsed;
    self.searchQuery = self.searchInput.value;
    self.expandAll();
  }.bind(this);

  this.clearSearch = function() {
    self.searchQuery = self.searchInput.value = '';
    if (self.collapsedBeforeSearch) {
      self.collapsed = self.collapsedBeforeSearch;
      delete self.collapsedBeforeSearch;
    }
  }.bind(this);

  this.openFileDialog = function() {
    self.fileUpload.click();
  }.bind(this);

  this.loadModel = function() {
    var fileReader = new FileReader();
    fileReader.onload = function() {
      var dataObject = JSON.parse(fileReader.result);
      opts.model.load(dataObject.model);
      dataObject.vertices.forEach(VertexActions.add);
      dataObject.edges.forEach(EdgeActions.add);
    }
    fileReader.readAsText(self.fileUpload.files[0]);
  }.bind(this);


});
riot.tag('properties-pane', '<ul> <li if="{!isMultipleSelection && element.errorMessage}"> <div class="bg-warning"><span class="octicon octicon-alert"></span> { element.errorMessage }</div> </li> <li if="{!isMultipleSelection}"><b>Name:</b><br> <editable type="text" callback="{ change(\'name\') }">{ parent.element.name || \'unnamed\' }</editable> </li> <li if="{!isMultipleSelection}"><b>ID:</b><br>{ element.id }</li> <li if="{isMultipleSelection}"> Selected { opts.selection.length } { isDifferentTypes ? \'elements\' : element.type.pluralize(isMultipleSelection) } </li> <li> <button onclick="{ removeElement }" class="red"> <span class="octicon octicon-trashcan"></span> Remove { isDifferentTypes ? \'elements\' : element.type.pluralize(isMultipleSelection) } </button> <button show="{ !isMultipleSelection && element == opts.model }" onclick="{ saveModel }"> <span class="octicon octicon-desktop-download"></span> Save model </button> </li> </ul>', function(opts) {

  var VertexActions    = require('actions/VertexActions');
  var EdgeActions      = require('actions/EdgeActions');
  var ModelActions     = require('actions/ModelActions');
  var StudioConstants  = require('constants/StudioConstants');

  var self = this;

  self.on('update', function() {
    self.element = opts.selection[0] || opts.model || {};
    self.isMultipleSelection = opts.selection.length > 1;
    self.isDifferentTypes = !self.isMultipleSelection ? false :
      !opts.selection.mapBy('type').every(function(el, i, array) {
        return i > 0 ? el === array[i-1] : true;
      });
  });

  this.saveModel = function() {

    var counter = 0;
    var vertices, edges;
    VertexActions.getAll(function(resp) {
      vertices = resp;
      if (++counter == 2) save();
    });
    EdgeActions.getAll(function(resp) {
      edges = resp.map(function(edge) {
        delete edge._jsp_connection;
        return edge;
      });
      if (++counter == 2) save();
    });
    function save() {
      var data = {
        model: opts.model,
        vertices: vertices,
        edges: edges
      };
      var blob = new Blob([JSON.stringify(data)], {type: 'octet/stream'});
      var url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
      window.focus();
      window.URL.revokeObjectURL(url);
    }
  }.bind(this);

  this.change = function(prop) {
    return function(newValue) {
      var props = {};
      props[prop] = newValue;
      switch (self.element.type) {
        case StudioConstants.types.T_VERTEX:
          VertexActions.setProps(self.element.id, props);
          break;
        case StudioConstants.types.T_EDGE:
          EdgeActions.setProps(self.element.id, props);
          break;
        case StudioConstants.types.T_MODEL:
          ModelActions.setProps(self.element.id, props);
          break;
      }
    };
  }.bind(this);

  this.removeElement = function() {
    if (self.isDifferentTypes) {

      var _selection = opts.selection.slice();

      EdgeActions.remove(_selection.filter(function(el) {
        return el.type === StudioConstants.types.T_EDGE;
      }).mapBy('id'));

      VertexActions.remove(_selection.filter(function(el) {
        return el.type === StudioConstants.types.T_VERTEX;
      }).mapBy('id'));
    } else {
      switch (self.element.type) {
        case StudioConstants.types.T_VERTEX:
          VertexActions.remove(opts.selection.mapBy('id'));
          break;
        case StudioConstants.types.T_EDGE:
          EdgeActions.remove(opts.selection.mapBy('id'));
          break;
        case StudioConstants.types.T_MODEL:
          ModelActions.remove(self.element.id);
          break;
      }
    }
  }.bind(this);

});
riot.tag('settings-pane', '<div class="pane-body"> <connection-subpane ></connection-subpane> <canvas-settings-subpane options="{ opts.options }"></canvas-settings-subpane> </div>', function(opts) {

});
riot.tag('sidebar-pane', '<h4 onclick="{ toggle(\'expanded\') }"> <span if="{ opts.icon }" class="icon octicon octicon-{ opts.icon }"></span> { opts.heading } <span class="minimize octicon octicon-diff-{ expanded ? \'removed\' : \'added\'}"></span> </h4> <div class="pane-body" show="{ expanded }"> <yield></yield> </div>', 'sidebar-pane, [riot-tag="sidebar-pane"]{ display: block; background-color: #325262; color: white; margin: 2px; padding: 8px; } sidebar-pane h4, [riot-tag="sidebar-pane"] h4{ background-color: #5b8590; margin: -5px; padding: 20px 15px; height: 20px; cursor: default; } sidebar-pane h4 .octicon, [riot-tag="sidebar-pane"] h4 .octicon{ padding-right: 5px; } sidebar-pane .pane-body, [riot-tag="sidebar-pane"] .pane-body{ margin-top: 15px; } sidebar-pane .pane-body > * > ul, [riot-tag="sidebar-pane"] .pane-body > * > ul{ list-style: none; padding: 0; margin: 0 auto; } sidebar-pane .pane-body > * > ul > li, [riot-tag="sidebar-pane"] .pane-body > * > ul > li{ padding: 0 0 10px 0; } sidebar-pane .pane-body a, [riot-tag="sidebar-pane"] .pane-body a{ color: inherit; } sidebar-pane .minimize, [riot-tag="sidebar-pane"] .minimize{ float: right; }', function(opts) {

  var self = this;

  self.mixin('tagUtils');

  self.expanded = true;

  self.one('update', function() {
    self.expanded = !self.opts.collapsed;
  });

});
riot.tag('studio-sidebar', '<div id="sidebar"> <sidebar-pane heading="Properties" icon="list-unordered" if="{ opts.model.id }"> <properties-pane model="{ parent.opts.model }" selection="{ parent.opts.selection }"></properties-pane> </sidebar-pane> <sidebar-pane heading="Models" icon="file-directory"> <models-pane model="{ parent.opts.model }" selection="{ parent.opts.selection }" vertices="{ parent.opts.vertices }" edges="{ parent.opts.edges }" models="{ parent.opts.models }"></models-pane> </sidebar-pane> <sidebar-pane heading="GraphWalker" icon="git-branch" collapsed="{ true }"> <graphwalker-pane connected="{ parent.connectionOpen }" model="{ parent.opts.model }" selection="{ parent.opts.selection }"></graphwalker-pane> </sidebar-pane> <sidebar-pane heading="Settings" icon="gear" collapsed="{ true }"> <settings-pane options="{ parent.opts.options }"></settings-pane> </sidebar-pane> </div>', '#sidebar { float: right; width: 310px; height: 100%; background-color: #f0f0f0; overflow-y: overlay; }', function(opts) {

  var ConnectionActions = require('actions/ConnectionActions');

  var self = this;

  self.connectionOpen = false;

  var _toggle = function(toggle) {
    self.connectionOpen = toggle === undefined ?  !self.connectionOpen : toggle;
    self.update();
  };
  ConnectionActions.addConnectionListener({
    onopen: _toggle.bind(null, true),
    onclose: _toggle.bind(null, false)
  });


});
riot.tag('studio', '<studio-sidebar selection="{ selection }" model="{ model }" options="{ opts }" vertices="{ vertices }" edges="{ edges }" models="{ models }"></studio-sidebar> <studio-tabs tabs="{ tabs }" model="{ model }"></studio-tabs> <studio-canvas selection="{ selection }" model="{ model }" show="{ tabs.length }" options="{ opts }" vertices="{ vertices }" edges="{ edges }"></studio-canvas>', 'studio { height: 98%; display: block; background-color: #dedede; margin-bottom: 10px; }', function(opts) {

  var jsp               = require('jsplumb');
  var EdgeActions       = require('actions/EdgeActions');
  var RiotControl       = require('app/RiotControl');
  var ModelActions      = require('actions/ModelActions');
  var VertexActions     = require('actions/VertexActions');
  var StudioConstants   = require('constants/StudioConstants');
  var ConnectionActions = require('actions/ConnectionActions');

  var self = this;


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


  var updateElements = function(collection) {
    var _sel = self.selection;
    self.selection.update(_sel.map(function(el) {
      return collection.getBy('id', el.id)[0] || el;
    }));
  };


  self.selection = [];
  Object.defineProperty(self, 'selection', { writable: false }); // Prevent from overwriting object
  self.selection.clear = function(preventUpdate) {
    this.constructor.prototype.clear.apply(this);
    if (!preventUpdate) self.update();
  };
  self.selection.update = function(elements, toggle, preventUpdate) {

    if (!elements || elements.length === 0) {

      if (this.length === 0) return;

      this.clear(true);
    } else {
      if (!Array.isArray(elements)) elements = [elements]; // Wrap single element into array

      if (toggle) {


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

  self.tabs = [];
  Object.defineProperty(self, 'tabs', { writable: false }); // Prevent from overwriting object
  self.tabs.open = function(modelId, preventUpdate) {
    if (!this.contains(modelId)) {

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

    if (!self.model.id || self.model.id === modelId) {

      var next = index - 1;
      next = next < 0 ? 1 : next;
      self.model.set(this[next]);
    }
    this.splice(index, 1);
    self.update();
  }.bind(self.tabs);

  var _modelHelperFunctions = {

    set: function(model) {
      self.model = model;
    },

    new: function() {
      ModelActions.add({}, function(model) {
        self.model.set(model.id);
      });
    },

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

      this._modelId = '';
      this.update();

      if (modelId) {
        this._modelId = modelId;
        self.tabs.open(modelId, true);
        this.selection.clear();

        if (self.model.view && self.model.view.panzoom) {
          $('#canvas-body').panzoom('setMatrix', self.model.view.panzoom);
        } else {
          $('#canvas-body').panzoom('reset', { animate: false });
        }
      }
    }
  });

  self.on('mount', function() {
    if (opts.autoConnect && opts.autoConnect.enabled) {
      ConnectionActions.connect(opts.autoConnect.url);
    }
  });

  RiotControl.on(StudioConstants.calls.CLEAR_SELECTION, function() {
    self.selection.clear();
  });


});
riot.tag('studio-tabs', '<ul> <li each="{ opts.tabs.getObjects() }"> <div onclick="{ selectTab }" class="{ selected: parent.opts.model.id === id}"> { name } <span onclick="{ parent.closeTab }" class="octicon octicon-x"></span> </div> </li> <li><div id="add">&nbsp;<span onclick="{ openTab }" class="octicon octicon-plus"></span></div></li> </ul>', 'studio-tabs ul, [riot-tag="studio-tabs"] ul{ background-color: #f0f0f0; list-style: none; padding: 0; margin: 0; } studio-tabs li, [riot-tag="studio-tabs"] li{ display: inline-block; } studio-tabs div, [riot-tag="studio-tabs"] div{ height: 20px; width: 150px; border: 1px solid black; border-bottom: 0; padding: 5px; text-align: left; vertical-align: middle; line-height: 20px; background-color: #40697e; cursor: default; border-radius: 6px 6px 0 0; } studio-tabs span, [riot-tag="studio-tabs"] span{ float: right; color: rgba(0, 0, 0, 0.18) } studio-tabs span:hover, [riot-tag="studio-tabs"] span:hover{ color: black; cursor: default; background-color: rgba(0, 0, 0, 0.21); } studio-tabs .octicon, [riot-tag="studio-tabs"] .octicon{ border-radius: 50%; width: 15px; height: 15px; text-align: center; padding: 3px; } studio-tabs div#add, [riot-tag="studio-tabs"] div#add{ border: 0px; width: 100%; margin-left: -5px; border-top-left-radius: 0; } studio-tabs div.selected, [riot-tag="studio-tabs"] div.selected{ border-top: 4px solid #5b8590; background-color: #f0f0f0; }', function(opts) {

  var ModelActions = require('actions/ModelActions');

  var self = this;

  ModelActions.addChangeListener(function(models) {

    opts.tabs.forEach(function(modelId) {
      if (!models.mapBy('id').contains(modelId)) self.opts.tabs.close(modelId);
    });
    self.update();
  });

  this.openTab = function(e) {
    self.opts.model.new();
    e.preventUpdate = true; // Update is called indirectly above
  }.bind(this);

  this.closeTab = function(e) {
    self.opts.tabs.close(e.item.id);
    e.preventUpdate = true; // Update is called indirectly above

    if (e) e.stopPropagation();
  }.bind(this);

  this.selectTab = function(e) {
    self.opts.model.set(e.item.id);
    e.preventUpdate = true; // Update is called indirectly above
  }.bind(this);

});
riot.tag('vertex', '<div class="label-div"> <editable type="text" class="label" off="{ !selected }" callback="{ changeName }" >{ name }</editable> </div>', 'vertex { background-clip: padding-box; border: 1px solid #325262; position: absolute !important; display: table !important; border-radius: 15px; cursor: default; background-color: rgba(192, 215, 221, 0.85); box-sizing: border-box; } vertex:focus { outline: none; } vertex.selected { border: 2px solid #3db2e3; cursor: move; } vertex.rubberband-hover { border: 2px solid #21cfdf; } vertex.unverified { border: 1px dashed #325262; } vertex.error { background-color: rgba(255, 200, 128, 0.85); } .label-div { display: table-cell; vertical-align: middle; text-align: center; padding: 10px; } .label { margin: 0; display: inline-block; min-width: 20px; min-height: 10pt; } .label:hover, .label:focus { background-color: rgba(210, 245, 248, 0.75); background-clip: content-box; outline: none; } .label::selection { background-color: #00c7c0; } .jsplumb-drag-hover { border: 1px solid #21cfdf; } vertex input { width: 90px; }', 'id="{ view.domId }" class="{ selected: selected } { status.toLowerCase() }" tabindex="1" vertex-id="{ id }"', function(opts) {

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


    var merged = $.extend(true, {}, self.defaults, self);
    $.extend(true, self, merged);

    if (!self.view.top || !self.view.left) {

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

      VertexActions.setProps(self.id, self.view);
    }

    if (!self.view.css) {

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

    $root.hide();

    jsp.makeSource(self.root);
    jsp.makeTarget(self.root);

    jsp.draggable(self.root, {
      containment: true,
      snapThreshold: 10,
      grid: [GRID_SIZE,GRID_SIZE],
      filter: ".ui-resizable-handle",
      start: function(params) {



        var isElementBeingDragged = params.e;
        if (!isElementBeingDragged) return;


        self.root.addEventListener('click', function handler(e) {
          e.stopPropagation();
          this.removeEventListener('click', handler, true);
        }, true);
      },
      stop: function(params) {
        var updatePositionInModel = function() {

          VertexActions.setProps(self.id, {view: {left: params.pos[0], top: params.pos[1]}});
        };
        ActionUtils.bufferedAction(updatePositionInModel, 'jsp.draggable.stop', params.selection.length);
      }
    });

    $root.resizable({
      grid: [GRID_SIZE,GRID_SIZE],
      resize: function(e, ui) {


        jsp.revalidate(ui.element.get(0));
      },
      stop: function(e, ui) {

        VertexActions.setProps(self.id, ui.size);
      }
    });

    $root.on('focus click', function(e) {

      var toggle = e.type === 'click' ? e.metaKey : false;
      self.opts.selection.update(self, toggle);
    });


    self.handleEvent = function(evt) {
      switch(evt.type) {
        case 'mousedown':

          evt.stopPropagation();

          evt.preventDefault();
          self.root.addEventListener('mouseleave', self, true);
          self.root.addEventListener('mouseup', self, true);
          break;

        case 'mouseup':
          self.root.removeEventListener('mouseleave', self, true);
          self.root.removeEventListener('mouseup', self, true);
          break;

        case 'mouseleave':

          if (evt.target != self.root) break;

          self.root.removeEventListener('mouseleave', self, true);
          self.root.removeEventListener('mouseup', self, true);

          self.root.removeEventListener('mousedown', self, true);



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

          self.root.dispatchEvent(new MouseEvent('mousedown', _e));

          self.root.addEventListener('mousedown', self, true);
          break;
      }
    };
    self.root.addEventListener('mousedown', self, true);

    setTimeout(function() {



      jsp.revalidate(self.root);
    }, 0);

    self.trigger('updated');
  });

  self.on('update', function() {
    self.selected = opts.selection.mapBy('id').contains(self.id);
    self.resizable = opts.selection.length === 1;
  });

  self.on('updated', function() {
    if ($root) {

      $root.show().css(self.view.css);

      self.root['_vertexObject'] = self;

      var selected = self.selected;
      var resizable = selected && self.resizable;

      

      jsp.setSourceEnabled(self.root, !selected);

      jsp.setDraggable(self.root, selected);

      $root.resizable(resizable ? 'enable' : 'disable');
      $root.children('.ui-resizable-handle').toggle(resizable);

      var modifyEventListener = selected ? self.root.removeEventListener : self.root.addEventListener;
      modifyEventListener.call(self.root, 'mousedown', self, true);
    }
  });

  self.on('unmount', function() {
    jsp.remove(self.root);
  });

  this.changeName = function(newValue) {
    var props = {name: newValue};
    VertexActions.setProps(self.id, props);
  }.bind(this);

});

});
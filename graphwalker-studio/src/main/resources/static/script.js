
// Hash array that holds all graphs/models.
var graphs = [];
var currentModelId;
var pauseExecution = false;
var stepExecution = false;
var isTestRunning = false;
var keys = {};
var issues;
var currentElement;
var currentExecutingElementId;
var mouseoverElement;
var rightClickedElement;
var rightClickedRenderedPosition;
var breakPoints = [];
$('#location').val('ws://localhost:9999');

function onConnect() {
  console.log('onConnect');

  onDisconnect();

  var wsUri = $.trim($('#location').val());
  try {
    testWebSocket(wsUri);
  } catch (err) {
    document.getElementById('issues').innerHTML = err.message;
  }
}

function onDisconnect() {
  console.log('onDisconnect');
  if (websocket) {
    // http://stackoverflow.com/questions/25779831/how-to-catch-websocket-connection-to-ws-xxxnn-failed-connection-closed-be
    // https://jsfiddle.net/lamarant/ry0ty52n/
    websocket.close(3001);
  }
}

function onNewTest() {
  console.log('onNewTest');
  removeTest();
  emptyInitialControlStates();
  defaultUI();
}

function onLoadTest() {
  console.log('onLoadTest');

  removeTest();

  $('<input type="file" class="ui-helper-hidden-accessible" />')
    .appendTo('body')
    .focus()
    .trigger('click')
    .remove()
    .change(function(evt) {
      var files = evt.target.files; // FileList object


      // files is a FileList of File objects. List some properties.
      for (var i = 0, f; f = files[i]; i++) {
        var fileExt = f.name.split('.').pop();

        switch (fileExt) {
          case 'graphml':
            var fr = new FileReader();
            fr.onload = function(e) {
              var convertGraphml = {
                command: 'convertGraphml',
                graphml: e.target.result
              };
              doSend(JSON.stringify(convertGraphml));
            };
            fr.readAsText(f);
            break;

          case 'json':
            var fr = new FileReader();
            fr.onload = function(e) {
              readGraphFromJSON(JSON.parse(e.target.result));
              var tabs = $('#tabs');
              tabs.show();
              for (var modelId in graphs) {
                if (!graphs.hasOwnProperty(modelId)) {
                  continue;
                }
                var index = $('#tabs').find('a[href="#A-' + modelId + '"]').parent().index();
                tabs.tabs('option', 'active', index);
                graphs[modelId].resize();
                graphs[modelId].fit();
              }
              defaultUI();
            };
            fr.readAsText(f);
            break;

          default:
            console.error('Unsupported file extension/format: ' + fileExt);
        }
      }
    });
}

function onSaveTest() {
  console.log('onSaveTest');
  var link = document.createElement('a');
  link.setAttribute('download', graphs.name + '.json');
  link.href = makeJsonGraphFile();
  document.body.appendChild(link);

  // wait for the link to be added to the document
  window.requestAnimationFrame(function() {
    var event = new MouseEvent('click');
    link.dispatchEvent(event);
    document.body.removeChild(link);
  });
}

function removeTest() {
  console.log('removeTest');

  $('#tabs > ul > li').each(function() {
    $(this).remove();
  });

  $('#tabs > div').each(function() {
    var id = $(this).attr('id').substr(2);
    $(this).remove();
    $('#A-' + id).remove();
    removeModel(id);
  });

  var tabs = $('#tabs');
  tabs.tabs('refresh');
  tabs.hide();
}

function makeJsonGraphFile() {
  console.log('makeJsonGraphFile');
  var jsonFile = null;
  var data = new Blob([JSON.stringify(generateJsonGraph())], {
    type: 'text/plain'
  });

  // If we are replacing a previously generated file we need to
  // manually revoke the object URL to avoid memory leaks.
  if (jsonFile !== null) {
    window.URL.revokeObjectURL(jsonFile);
  }

  jsonFile = window.URL.createObjectURL(data);

  return jsonFile;
}

function generateJsonGraph() {
  var jsonGraphs = {
    name: graphs.name,
    models: []
  };
  for (var modelId in graphs) {
    if (!graphs.hasOwnProperty(modelId)) {
      continue;
    }

    var actions = [];
    if (graphs[modelId].actions) {
      actions.push(graphs[modelId].actions);
    }
    var requirements = [];
    if (graphs[modelId].requirements) {
      requirements = graphs[modelId].requirements.split(',');
    }

    var model = {
      name: graphs[modelId].name,
      id: modelId,
      generator: graphs[modelId].generator,
      actions: actions,
      vertices: [],
      edges: []
    };

    if (graphs[modelId].startElementId !== undefined) {
      model.startElementId = graphs[modelId].startElementId;
    }


    /**
     * Iterate ove all nodes in the graph, and create a json
     * representation of the vertex
     */
    graphs[modelId].nodes().each(function(index, node) {

      if (node.data().startVertex === true) {
        return true;
      }

      var actions = [];
      if (node.data().actions) {
        actions.push(node.data().actions);
      }

      var requirements = [];
      if (node.data().requirements) {
        requirements = node.data().requirements.split(',');
      }

      var properties = {};
      if (node.data().properties) {
        properties = node.data().properties;
      }
      properties['x'] = node.position().x;
      properties['y'] = node.position().y;

      var vertex = {
        id: node.data().id,
        name: node.data().label,
        sharedState: node.data().sharedState,
        actions: actions,
        requirements: requirements,
        properties: properties
      };
      model.vertices.push(vertex);
    });


    /**
     * Iterate over all edges in the graph, and create a json
     * representation of the edge
     */
    graphs[modelId].edges().each(function(index, edge) {

      if (edge.data().source === 'Start') {
        edge.data().source = null;
      }

      var actions = [];
      if (edge.data().actions) {
        actions.push(edge.data().actions);
      }

      var requirements = [];
      if (edge.data().requirements) {
        requirements = edge.data().requirements.split(',');
      }

      var properties = [];
      if (edge.data().properties) {
        properties = edge.data().properties;
      }

      var newEdge = {
        id: edge.data().id,
        name: edge.data().label,
        guard: edge.data().guard,
        weight: edge.data().weight,
        actions: actions,
        requirements: requirements,
        properties: properties,
        sourceVertexId: edge.data().source,
        targetVertexId: edge.data().target
      };
      model.edges.push(newEdge);

    });

    jsonGraphs.models.push(model);
  }

  return jsonGraphs;
}

function onPausePlayExecution(element) {
  console.log('pausePlayExecution: pauseExecution: ' + pauseExecution + ', clicked: ' + currentModelId);
  stepExecution = false;

  if (pauseExecution) {
    document.getElementById('runTest').disabled = true;
    document.getElementById('resetTest').disabled = true;
    document.getElementById('pausePlayExecution').disabled = false;
    document.getElementById('stepExecution').disabled = true;
    document.getElementById('pausePlayExecution').innerHTML = 'Pause';
    pauseExecution = false;

    var hasNext = {
      command: 'hasNext'
    };
    doSend(JSON.stringify(hasNext));
  } else {
    document.getElementById('runTest').disabled = true;
    document.getElementById('resetTest').disabled = false;
    document.getElementById('pausePlayExecution').disabled = false;
    document.getElementById('stepExecution').disabled = false;
    document.getElementById('pausePlayExecution').innerHTML = 'Run';
    pauseExecution = true;
  }
}

function onStepExecution() {
  console.log('onStepExecution: ' + currentModelId);
  document.getElementById('runTest').disabled = true;
  document.getElementById('resetTest').disabled = false;
  document.getElementById('pausePlayExecution').disabled = false;
  document.getElementById('stepExecution').disabled = false;
  stepExecution = true;

  var hasNext = {
    command: 'hasNext'
  };
  doSend(JSON.stringify(hasNext));
}

// Run the execution of the state machine
function onRunTest() {
  console.log('onRunTest: ' + currentModelId);

  // Reset any previous runs
  onResetTest();
  isTestRunning = true;

  $('.ui-panel').panel('close');

  document.getElementById('runTest').disabled = true;
  document.getElementById('resetTest').disabled = true;
  document.getElementById('pausePlayExecution').disabled = false;
  document.getElementById('stepExecution').disabled = true;
  document.getElementById('addModel').disabled = true;
  stepExecution = false;
  pauseExecution = false;

  var start = {
    command: 'start',
  };

  start.gw = generateJsonGraph();
  doSend(JSON.stringify(start));
}

// Reset the state machine to it's initial state
function onResetTest() {
  console.log('onResetTest: ' + currentModelId);
  isTestRunning = false;
  defaultUI();

  document.getElementById('issues').innerHTML = 'Ready';

  for (var modelId in graphs) {
    if (!graphs.hasOwnProperty(modelId)) {
      continue;
    }

    graphs[modelId].nodes().unselect();
    graphs[modelId].edges().unselect();
  }
  setElementsColor();
}

function onAddModel() {
  console.log('onAddModel');
  enableModelControls();

  var id = generateUUID();
  var graph = createTab(id, 'New model');
  graph.name = 'New model';
  var tabs = $('#tabs');
  var index = tabs.find('a[href="#A-' + id + '"]').parent().index();
  tabs.show().tabs('option', 'active', index);
}

function onDoLayout() {
  console.log('onDoLayout');
  if (graphs[currentModelId] !== undefined) {
    graphs[currentModelId].layout().stop();
    var layout = graphs[currentModelId].makeLayout({
      name: 'dagre',
      animate: true,
      edgeSep: 4,
      minLen: function() {
        return 2;
      }
    });
    layout.run();
  }
}


window.graphwalker = {
  onConnect: onConnect,
  onDisconnect: onDisconnect,
  onNewTest: onNewTest,
  onLoadTest: onLoadTest,
  onSaveTest: onSaveTest,
  removeTest: removeTest,
  makeJsonGraphFile: makeJsonGraphFile,
  generateJsonGraph: generateJsonGraph,
  onPausePlayExecution: onPausePlayExecution,
  onStepExecution: onStepExecution,
  onRunTest: onRunTest,
  onResetTest: onResetTest,
  onAddModel: onAddModel,
  onDoLayout: onDoLayout
}

  window.onConnect = onConnect;
  window.onDisconnect = onDisconnect;
  window.onNewTest = onNewTest;
  window.onLoadTest = onLoadTest;
  window.onSaveTest = onSaveTest;
  window.removeTest = removeTest;
  window.makeJsonGraphFile = makeJsonGraphFile;
  window.generateJsonGraph = generateJsonGraph;
  window.onPausePlayExecution = onPausePlayExecution;
  window.onStepExecution = onStepExecution;
  window.onRunTest = onRunTest;
  window.onResetTest = onResetTest;
  window.onAddModel = onAddModel;
  window.onDoLayout = onDoLayout;

/*
 ************************************************************************
 * CREATE SOME CUSTOM EVENTS THAT HANDLES MODEL EXECUTION
 ************************************************************************
 */
var playbackEvent = new CustomEvent('playbackEvent', {});
document.addEventListener('playbackEvent', function() {
  console.log('playbackEvent');

  var getModel = {
    command: 'getModel'
  };
  doSend(JSON.stringify(getModel));
});

var startEvent = new CustomEvent('startEvent', {});
document.addEventListener('startEvent', function() {
  console.log('startEvent: ' + currentModelId);

  // Change some UI elements
  document.getElementById('runTest').disabled = true;
  document.getElementById('resetTest').disabled = true;
  document.getElementById('pausePlayExecution').disabled = false;
  document.getElementById('stepExecution').disabled = true;

  var hasNext = {
    command: 'hasNext'
  };
  doSend(JSON.stringify(hasNext));
});

var hasNextEvent = new CustomEvent('hasNextEvent', {});
document.addEventListener('hasNextEvent', function() {
  console.log('hasNextEvent: pauseExecution: ' + pauseExecution +
    ', stepExecution: ' + stepExecution + ' : modelId ' + currentModelId);
  if (pauseExecution) {
    if (!stepExecution) {
      return;
    }
  }
  var getNext = {
    command: 'getNext'
  };
  doSend(JSON.stringify(getNext));
});

var getNextEvent = new CustomEvent('getNextEvent', {
  "modelId": "",
  "elementId": "",
  "name": ""
});
document.addEventListener('getNextEvent', function(e) {
  console.log('getNextEvent: ' + e.id + ': ' + e.name + 'pauseExecution: ' + pauseExecution +
    ', stepExecution: ' + stepExecution + ' : modelId ' + currentModelId);

  if (breakPoints.some(function(e) {
      return e.data().id === currentExecutingElementId
    })) {
    onPausePlayExecution(currentExecutingElementId);
    return;
  }

  if (stepExecution) {
    stepExecution = false;
    return;
  }

  var hasNext = {
    command: 'hasNext'
  };
  setTimeout(function() {
    doSend(JSON.stringify(hasNext));
  }, $.trim($('#executionSpeedSlider').val()));
});

var getModelEvent = new CustomEvent('getModelEvent', {});
document.addEventListener('getModelEvent', function(e) {
  console.log('getModelEvent');

  var updateAllElements = {
    command: 'updateAllElements'
  };
  doSend(JSON.stringify(updateAllElements));
});

function removeModel(modelId) {
  console.log('Remove model with id: ' + modelId);
  delete graphs[modelId];
}


document.addEventListener('DOMContentLoaded', function() {
  var tabs = $('#tabs');
  var modelRequirements = $('#modelRequirements');
  var modelActions = $('#modelActions');
  var modelName = $('#modelName');
  var generator = $('#generator');

  tabs.delegate('span.ui-icon-close', 'click', function() {
    var id = $(this).closest('li').remove().attr('aria-controls').substr(2);
    $('#A-' + id).remove();
    tabs.tabs('refresh');
    if (tabs.find('li').length < 1) {
      tabs.hide();
    }
    removeModel(id);
  });

  tabs.tabs({
    activate: function(event, ui) {
      currentModelId = ui.newPanel.attr('id').substr(2);
      console.log('tabs activate: ' + currentModelId);
      graphs[currentModelId].resize();
      modelName.val(graphs[currentModelId].name);
      generator.val(graphs[currentModelId].generator);
      modelActions.val(graphs[currentModelId].actions);
      modelRequirements.val(graphs[currentModelId].requirements);
    }
  });

  // Hide the tab component. It will get visible when the graphs are loaded.
  tabs.hide();

  $(document).keyup(function(e) {
    console.log('key up: ' + e.which);
    delete keys[e.which];
  });

  $(document).keydown(function(e) {
    console.log('key down: ' + e.which);
    keys[e.which] = true;

    if (keys[46]) { // Delete key is pressed
      if (graphs[currentModelId] !== undefined) {
        graphs[currentModelId].remove(':selected');
      }
    }
  });

  modelName.on('input', function() {
    if (graphs[currentModelId]) {
      graphs[currentModelId].name = $.trim(modelName.val());
      var tabs = $('#tabs');
      var selectedTab = tabs.tabs('option', 'selected');
      tabs.find('ul li a').eq(selectedTab).text($.trim(modelName.val()));
    }
  });

  generator.on('input', function() {
    if (graphs[currentModelId]) {
      graphs[currentModelId].generator = $.trim(generator.val());
    }
  });

  modelActions.on('input', function() {
    if (graphs[currentModelId]) {
      graphs[currentModelId].actions = $.trim(modelActions.val());
    }
  });

  modelRequirements.on('input', function() {
    if (graphs[currentModelId]) {
      graphs[currentModelId].requirements = $.trim(modelRequirements.val());
    }
  });
});


function createTab(modelId, modelName) {
  console.log('createTab: ' + modelId + ', ' + modelName);
  enableModelControls();

  var tabs = $('#tabs').tabs();
  var ul = tabs.find('ul');

  // ID tokens must begin with a letter ([A-Za-z])
  // https://www.w3.org/TR/html401/types.html#type-name
  var href = '#A-' + modelId;
  $('<li><a href="' + href + '">' + modelName + '</a><span class="ui-icon ui-icon-close"></span></li>').appendTo(ul);
  $('<div id="A-' + modelId + '"></div>').appendTo(tabs);
  tabs.tabs('refresh');

  $('<style>').prop('type', 'text/css').html('\
                    ' + href + ' {\
                        background: floralwhite;\
                        position: relative;\
                        height:100%;\
                        overflow-y: hidden;\
                    }').appendTo('head');

  return createGraph(modelId);
}

function createGraph(currentModelId) {
  console.log('createGraph - ' + currentModelId);
  var graph = cytoscape({
    id: currentModelId,
    container: document.querySelector('#A-' + currentModelId),
    currentElement: null,
    boxSelectionEnabled: true,
    wheelSensitivity: '0', // Values 0, 0.5 and 1 has the same effect...
    ready: function() {
      console.log('Cytoscape is ready...');
    },
    style: cytoscape.stylesheet()
      .selector('core')
      .css({
        //'active-bg-size': 0 // remove the grey circle when panning
      })
      .selector('node')
      .css({
        'content': 'data(name)',
        'text-wrap': 'wrap',
        'text-valign': 'center',
        'text-halign': 'center',
        'shape': 'roundrectangle',
        'width': 'label',
        'height': 'label',
        'color': 'black',
        'background-color': 'data(color)',
        'line-color': 'data(color)',
        'padding-left': '10',
        'padding-right': '10',
        'padding-top': '10',
        'padding-bottom': '10'
      })
      .selector('edge')
      .css({
        'content': 'data(name)',
        'text-wrap': 'wrap',
        'curve-style': 'bezier',
        'text-rotation': 'autorotate',
        'target-arrow-shape': 'triangle',
        'width': '4',
        'line-color': 'data(color)',
        'target-arrow-color': 'data(color)',
        'background-color': 'data(color)'
      })
      .selector(':selected')
      .css({
        'border-width': 4,
        'border-color': 'black',
        'line-color': 'black',
        'target-arrow-color': 'black'
      })
  });

  var srcNode = null;
  graph.on('tapstart', 'node', function() {
    if (keys[69]) { // e key is pressed
      console.log('tapstart with e key pressed on node: ' + this.id());
      srcNode = this;
      graph.autoungrabify(true);
    }
  });

  var dstNode = null;
  graph.on('tapend', 'node', function() {
    if (keys[69]) { // e key is pressed
      console.log('tapend with e key pressed on node: ' + this.id());
      dstNode = this;
      graph.autoungrabify(false);

      if (srcNode !== undefined && dstNode !== undefined) {
        var id = generateUUID();
        graph.add({
          group: 'edges',
          data: {
            id: id,
            source: srcNode.id(),
            target: dstNode.id(),
            label: 'e_NewEdge',
            name: formatElementName({
              name: 'e_NewEdge'
            }),
            color: 'LightSteelBlue'
          }
        });
        console.log('  Added edge: ' + id);
      }
    }
  });

  var tappedBefore;
  var tappedTimeout;
  graph.on('tap', function(event) {
    var tappedNow = event.cyTarget;
    if (tappedTimeout && tappedBefore) {
      clearTimeout(tappedTimeout);
    }
    if (tappedBefore === tappedNow) {
      tappedNow.trigger('doubleTap');
      tappedBefore = null;
    } else {
      tappedTimeout = setTimeout(function() {
        tappedBefore = null;
      }, 300);
      tappedBefore = tappedNow;
    }

    if (keys[86]) { // v key is pressed
      console.log('tap and v key pressed');
      var id = generateUUID();
      graph.add({
        group: 'nodes',
        data: {
          id: id,
          label: 'v_NewVertex',
          name: formatElementName({
            name: 'v_NewVertex'
          }),
          color: 'LightSteelBlue'
        },
        renderedPosition: {
          x: event.cyRenderedPosition.x,
          y: event.cyRenderedPosition.y
        }
      });
      console.log('  Added vertex: ' + id);
    }

    currentElement = null;

    $('#label').val('').prop('disabled', true);
    $('#elementId').val('').prop('disabled', true);
    $('#sharedStateName').val('').prop('disabled', true);
    $('#guard').val('').prop('disabled', true);
    $('#weight').val('').prop('disabled', true);
    $('#actions').val('').prop('disabled', true);
    $('#requirements').val('').prop('disabled', true);
    $('#checkboxStartElement').checkboxradio('disable');
    $('#checkboxStartElement').prop('checked', false).checkboxradio('refresh');
  });

  graph.on('tap', 'node', function() {
    currentElement = this;
    $('#label').textinput('enable').val(this.data().label);
    $('#elementId').textinput('enable').val(this.data().id);
    $('#sharedStateName').textinput('enable').val(this.data().sharedState);
    $('#actions').textinput('enable').val(this.data().actions);
    $('#requirements').textinput('enable').val(this.data().requirements);

    $('#checkboxStartElement').checkboxradio('enable');
    if (graph.startElementId === this.id()) {
      $('#checkboxStartElement').prop('checked', true).checkboxradio('refresh');
    }
  });

  graph.on('tap', 'edge', function() {
    currentElement = this;
    $('#label').textinput('enable').val(this.data().label);
    $('#elementId').textinput('enable').val(this.data().id);
    $('#guard').textinput('enable').val(this.data().guard);
    $('#weight').textinput('enable').val(this.data().weight);
    $('#actions').textinput('enable').val(this.data().actions);
    $('#requirements').textinput('enable').val(this.data().requirements);

    $('#checkboxStartElement').checkboxradio('enable');
    if (graph.startElementId === this.id()) {
      $('#checkboxStartElement').prop('checked', true).checkboxradio('refresh');
    }
  });

  $('#A-' + currentModelId).mousedown(function(event) {
    //context.destroy('#A-' + currentModelId);
    switch (event.which) {
      case 1:
        //alert('Left Mouse button pressed.');
        break;
      case 2:
        //alert('Middle Mouse button pressed.');
        break;
      case 3:
        //alert('Right Mouse button pressed.');
        if (mouseoverElement !== undefined) {
          console.log("Mouse over: " + mouseoverElement.data().id);
          rightClickedElement = mouseoverElement;
          var isBreakPointEnabled = breakPoints.some(function(e) {
            return e.data().id === rightClickedElement.data().id
          });

          var breakpointStr;
          if (isBreakPointEnabled) {
            breakpointStr = 'Disable breakpoint';
          } else {
            breakpointStr = 'Enable breakpoint';
          }

          var startElementtStr;
          if (rightClickedElement.data().id === getStartingElementId()) {
            startElementtStr = 'Disable start element';
          } else {
            startElementtStr = 'Enable start element';
          }

          context.attach('#A-' + currentModelId, [{
            header: 'Name: ' + mouseoverElement.data().name,
          }, {
            divider: true
          }, {
            text: startElementtStr,
            action: function(event) {
              if (rightClickedElement.data().id === getStartingElementId()) {
                graph.startElementId = undefined;
              } else {
                for (var modelId in graphs) {
                  graphs[modelId].startElementId = undefined;
                }
                graph.startElementId = rightClickedElement.data().id;
              }
              setElementsColor();
            }
          }, {
            text: breakpointStr,
            action: function(event) {
              if (isBreakPointEnabled) {
                console.log("Removing break point at: " + rightClickedElement.data().id);
                breakPoints.splice(rightClickedElement, 1);
              } else {
                console.log("Adding break point at: " + rightClickedElement.data().id);
                breakPoints.push(rightClickedElement);
              }
              setElementsColor();
            }
          }, {
            divider: true
          }, {
            text: 'Remove',
            action: function(event) {
              console.log("Removing element: " + rightClickedElement.data().id);
              graph.remove(rightClickedElement);
            }
          }]);
        } else {
          context.attach('#A-' + currentModelId, [{
            text: 'Add vertex',
            action: function(event) {
              console.log("Adding element");
              var id = generateUUID();
              graph.add({
                group: 'nodes',
                data: {
                  id: id,
                  label: 'v_NewVertex',
                  name: formatElementName({
                    name: 'v_NewVertex'
                  }),
                  color: 'LightSteelBlue'
                },
                renderedPosition: rightClickedRenderedPosition
              });
            }
          }, {
            divider: true
          }, {
            text: 'Run',
            action: function(event) {
              onRunTest();
            }
          }, {
            text: 'Pause',
            action: function(event) {
              onPausePlayExecution();
            }
          }, {
            text: 'Step',
            action: function(event) {
              onStepExecution();
            }
          }, {
            text: 'Reset',
            action: function(event) {
              onResetTest();
            }
          }]);
        }
        break;

      default:
        alert('You have a strange Mouse!');
    }
  });

  graph.on('mouseover', function(event) {
    mouseoverElement = undefined;
    rightClickedRenderedPosition = {
      x: event.cyRenderedPosition.x,
      y: event.cyRenderedPosition.y
    };
  });

  graph.on('mouseover', 'node', function() {
    mouseoverElement = this;
  });

  graph.on('mouseover', 'edge', function() {
    mouseoverElement = this;
  });

  graph.on('mouseout', function() {
    mouseoverElement = undefined;
  });

  graph.on('cxttap', 'node', function() {});

  $('#label').on('input', function() {
    if (currentElement) {
      currentElement.data('label', $.trim($('#label').val()));
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        weight: currentElement.data().weight,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#elementId').on('input', function() {
    if (currentElement) {
      currentElement.data('id', $.trim($('#elementId').val()));
    }
  });

  $('#sharedStateName').on('input', function() {
    if (currentElement) {
      currentElement.data('sharedState', $.trim($('#sharedStateName').val()));
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        weight: currentElement.data().weight,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#guard').on('input', function() {
    if (currentElement) {
      currentElement.data('guard', $.trim($('#guard').val()));
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        guard: currentElement.data().guard,
        weight: currentElement.data().weight,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#weight').on('input', function() {
    if (currentElement) {
      currentElement.data('weight', $.trim($('#weight').val()));
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        guard: currentElement.data().guard,
        weight: currentElement.data().weight,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#actions').on('input', function() {
    if (currentElement) {
      currentElement.data('actions', $.trim($('#actions').val()));
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        weight: currentElement.data().weight,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#requirements').on('input', function() {
    if (currentElement) {
      currentElement.data('requirements', $.trim($('#requirements').val()));
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        weight: currentElement.data().weight,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#checkboxStartElement').change(function() {
    if ($(this).is(':checked')) {
      if (currentElement) {
        for (var modelId in graphs) {
          graphs[modelId].startElementId = undefined;
        }
        graph.startElementId = currentElement.id();
      }
    } else {
      if (currentElement === graph.startElementId) {
        graph.startElementId = undefined;
      }
    }
    setElementsColor();
  });

  graph.on('doubleTap', function() {
    $('#nav-panel').panel('open');
  });
  var uipanel = $('.ui-panel');
  uipanel.panel({
    close: function() {
      console.log('Resize the model, because the side panel has been closed');
      graph.resize();
    }
  });
  uipanel.panel({
    open: function() {
      console.log('Resize the model, because the side panel has been opened');
      graph.resize();
    }
  });

  // Set default values.
  graph.generator = 'random(edge_coverage(100))';

  graphs[currentModelId] = graph;
  return graph;
}

function readGraphFromJSON(jsonGraphs) {
  var jsonModels = jsonGraphs.models;
  var graphs = [];

  for (var modelIndex = 0; modelIndex < jsonModels.length; modelIndex++) {
    var jsonModel = jsonModels[modelIndex];

    var id, name;
    if (jsonModel.hasOwnProperty('id')) {
      id = jsonModel.id;
    } else {
      id = generateUUID();
    }
    if (jsonModel.hasOwnProperty('name')) {
      name = jsonModel.name;
    } else {
      name = 'Model: ' + modelIndex;
    }
    var graph = createTab(id, name);

    graph.generator = jsonModel.generator;
    graph.name = name;

    if (jsonModel.hasOwnProperty('startElementId')) {
      graph.startElementId = jsonModel.startElementId;
    }
    if (jsonModel.hasOwnProperty('actions')) {
      graph.actions = jsonModel.actions.join('');
    }

    var jsonVertices = jsonModel.vertices;
    for (var i = 0; i < jsonVertices.length; i++) {
      var jsonVertex = jsonVertices[i];
      var x = 0,
        y = 0;
      if (jsonVertex.properties !== undefined) {
        if (jsonVertex.properties.x !== undefined) {
          x = jsonVertex.properties.x;
        }
        if (jsonVertex.properties.y !== undefined) {
          y = jsonVertex.properties.y;
        }
      } else {
        jsonVertex.properties = {};
      }

      var vertexActions = '';
      var vertexRequirements = '';

      if (jsonVertex.hasOwnProperty('actions')) {
        vertexActions = jsonVertex.actions.join('');
      }
      if (jsonVertex.hasOwnProperty('requirements')) {
        vertexRequirements = jsonVertex.requirements.join();
      }

      graph.add({
        group: 'nodes',
        data: {
          id: jsonVertex.id,
          label: jsonVertex.name,
          name: formatElementName({
            name: jsonVertex.name,
            sharedState: jsonVertex.sharedState,
            actions: jsonVertex.actions,
            requirements: jsonVertex.requirements
          }),
          sharedState: jsonVertex.sharedState,
          actions: vertexActions,
          requirements: vertexRequirements,
          properties: jsonVertex.properties,
          color: 'LightSteelBlue'
        },
        position: {
          x: x,
          y: y
        }
      });
    }
    var jsonEdges = jsonModel.edges;
    for (i = 0; i < jsonEdges.length; i++) {
      var jsonEdge = jsonEdges[i];

      // If source vertex is undefined, assume start vertex
      if (jsonEdge.sourceVertexId === undefined || jsonEdge.sourceVertexId === null) {
        jsonEdge.sourceVertexId = 'Start';

        graph.add({
          group: 'nodes',
          data: {
            id: jsonEdge.sourceVertexId,
            name: 'Start',
            startVertex: true,
            color: 'LightGreen'
          },
          position: {
            x: 0,
            y: 0
          }
        });
      }

      var edgeActions = '';
      var edgeRequirements = '';

      if (jsonEdge.hasOwnProperty('actions')) {
        edgeActions = jsonEdge.actions.join('');
      }
      if (jsonEdge.hasOwnProperty('requirements')) {
        edgeRequirements = jsonEdge.requirements.join();
      }

      graph.add({
        group: 'edges',
        data: {
          id: jsonEdge.id,
          source: jsonEdge.sourceVertexId,
          target: jsonEdge.targetVertexId,
          label: jsonEdge.name,
          name: formatElementName({
            name: jsonEdge.name,
            guard: jsonEdge.guard,
            weight: jsonEdge.weight,
            actions: jsonEdge.actions
          }),
          guard: jsonEdge.guard,
          weight: jsonEdge.weight,
          actions: edgeActions,
          requirements: edgeRequirements,
          properties: jsonEdge.properties,
          color: 'LightSteelBlue'
        }
      });
    }
    graphs[graph.id] = graph;
  }
  setElementsColor();
  return graphs;
}

function getStartingElementId() {
  for (var modelId in graphs) {
    if (graphs[modelId].startElementId !== undefined) {
      return graphs[modelId].startElementId;
    }
  }
  return undefined;
}

function setElementsColor() {
  for (var modelId in graphs) {
    if (!isTestRunning) {
      graphs[modelId].edges().data('color', 'LightSteelBlue');
      graphs[modelId].nodes().data('color', 'LightSteelBlue');
    }

    graphs[modelId].nodes().filterFn(function(ele) {
      return ele.data('startVertex') === true;
    }).data('color', 'LightGreen');

    if (!isTestRunning) {
      graphs[modelId].nodes().filterFn(function(ele) {
        return ele.data('sharedState') !== undefined &&
          ele.data('sharedState') != null &&
          ele.data('sharedState').length > 0;
      }).data('color', 'LightSalmon');

      graphs[modelId].edges().filterFn(function(ele) {
        return ele.data('id') === graphs[modelId].startElementId;
      }).data('color', 'LightGreen');

      graphs[modelId].nodes().filterFn(function(ele) {
        return ele.data('id') === graphs[modelId].startElementId;
      }).data('color', 'LightGreen');
    }

    graphs[modelId].nodes().filterFn(function(ele) {
      return breakPoints.some(function(e) {
        return e.data().id === ele.data('id')
      });
    }).data('color', 'Red');

    graphs[modelId].edges().filterFn(function(ele) {
      return breakPoints.some(function(e) {
        return e.data().id === ele.data('id')
      });
    }).data('color', 'Red');
  }
}

function defaultUI() {
  console.log('defaultUI');
  if (Object.keys(graphs).length > 0 && currentModelId !== undefined) {
    document.getElementById('runTest').disabled = false;
    document.getElementById('resetTest').disabled = true;
    document.getElementById('stepExecution').disabled = true;
    document.getElementById('pausePlayExecution').innerHTML = 'Pause';
    document.getElementById('pausePlayExecution').disabled = true;
    document.getElementById('addModel').disabled = false;
  } else {
    document.getElementById('runTest').disabled = false;
    document.getElementById('resetTest').disabled = false;
    document.getElementById('stepExecution').disabled = false;
    document.getElementById('pausePlayExecution').innerHTML = 'Pause';
    document.getElementById('pausePlayExecution').disabled = false;
    document.getElementById('addModel').disabled = false;
  }
}

function generateUUID() {
  /*jslint bitwise: true */
  console.log('generateUUID');
  var d = new Date().getTime();
  var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = (d + Math.random() * 16) % 16 | 0;
    d = Math.floor(d / 16);
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
  console.log('  UUID: ' + uuid);
  /*jslint bitwise: false */
  return uuid;
}

var simpleFormat = true;

function formatElementName(jsonObj) {
  var str = '';
  if (simpleFormat) {
    if (jsonObj.name) {
      return jsonObj.name;
    }
  } else {
    if (jsonObj.name) {
      str += 'Name: ' + jsonObj.name + '\n';
    }
    if (jsonObj.sharedState) {
      str += 'Shared state name: ' + jsonObj.sharedState + '\n';
    }
    if (jsonObj.guard) {
      str += 'Guard: ' + jsonObj.guard + '\n';
    }
    if (jsonObj.weight) {
      str += 'Weight: ' + parseFloat(jsonObj.weight) + '\n';
    }
    if (jsonObj.actions) {
      str += 'Actions: ' + jsonObj.actions + '\n';
    }
    if (jsonObj.requirements) {
      str += 'Requirements: ' + jsonObj.requirements + '\n';
    }
    return str.slice(0, -1);
  }
  return '';
}


/*************************************************************************
 *
 * WEBSOCKET CLIENT TO GRAPHWALKER
 *
 *************************************************************************/
var websocket;
var studioMode;

function testWebSocket(wsUri) {
  console.log('testWebSocket: ' + wsUri);

  try {
    websocket = new WebSocket(wsUri);
  } catch (err) {
    document.getElementById('issues').style.backgroundColor = "lightred";
    document.getElementById('issues').innerHTML = err.message;
  }

  websocket.onopen = function(evt) {
    onOpen(evt);
  };
  websocket.onclose = function(evt) {
    onClose(evt);
  };
  websocket.onmessage = function(evt) {
    onMessage(evt);
  };
  websocket.onerror = function(evt) {
    onError(evt);
  };
}

function onOpen(evt) {
  console.log('onOpen: ' + evt.data);
  document.getElementById('issues').style.backgroundColor = "green";
  document.getElementById('issues').innerHTML = 'Connected';
  var mode = {
    command: 'mode'
  };
  doSend(JSON.stringify(mode));
}

function onClose(evt) {
  console.log('onClose');
  if (evt.code == 3001) {
    console.log('ws closed');
    document.getElementById('issues').style.backgroundColor = "black";
    document.getElementById('issues').innerHTML = 'Connection closed';
  } else {
    console.log('ws connection error');
    document.getElementById('issues').style.backgroundColor = "red";
    document.getElementById('issues').innerHTML = 'Connection error while connecting to: ' + $.trim($('#location').val());
  }
}

function onMessage(event) {
  console.log('onMessage: ' + event.data);
  var message = JSON.parse(event.data);

  switch (message.command) {
    case 'mode':
      if (message.success) {
        studioMode = message.mode;
        switch (message.mode) {
          case 'EDITOR':
            document.getElementById('issues').innerHTML = 'Connected as ' + message.mode + ', ' + message.version;
            document.getElementById('issues').style.backgroundColor = "green";
            break;
          case 'PLAYBACK':
            document.getElementById('issues').innerHTML = 'Connected as ' + message.mode + ', ' + message.version;
            document.getElementById('issues').style.backgroundColor = "green";
            document.dispatchEvent(playbackEvent);
            break;
          default:
            document.getElementById('issues').innerHTML = 'Not connected';
            document.getElementById('issues').style.backgroundColor = "lightred";
        }
      } else {
        defaultUI();
      }
      break;
    case 'hasNext':
      if (message.success) {
        console.log('Command hasNext: ' + message.hasNext);
        if (message.hasNext) {
          hasNextEvent.fullfilled = message.hasNext;
          document.dispatchEvent(hasNextEvent);
        } else {
          defaultUI();
          document.getElementById('runTest').disabled = true;
          document.getElementById('resetTest').disabled = false;
        }
      } else {
        defaultUI();
      }
      break;
    case 'getNext':
      if (message.success) {
        console.log('Command getNext ok');
        currentExecutingElementId = message.elementId;
        document.dispatchEvent(getNextEvent, message.modelId, message.elementId, message.name);
      } else {
        defaultUI();
      }
      break;
    case 'start':
      if (message.success) {
        document.getElementById('issues').innerHTML = 'No issues';
        console.log('Command start ok');
        document.dispatchEvent(startEvent);
      } else {
        defaultUI();
      }
      break;
    case 'getmodel':
      if (message.success) {
        document.getElementById('issues').innerHTML = 'No issues';
        console.log('Command getModel ok');

        removeTest();

        readGraphFromJSON(JSON.parse(message.models));
        var tabs = $('#tabs');
        tabs.show();
        for (var modelId in graphs) {
          if (!graphs.hasOwnProperty(modelId)) {
            continue;
          }
          var index = $('#tabs').find('a[href="#A-' + modelId + '"]').parent().index();
          tabs.tabs('option', 'active', index);
          graphs[modelId].resize();
          graphs[modelId].fit();
        }
      }
      defaultUI();
      document.dispatchEvent(getModelEvent);
      break;
    case 'updateallelements':
      if (message.success) {
        for (var index in message.elements) {
          if (message.elements[index].visitedCount > 0) {
            graphs[message.elements[index].modelId].$('#' + message.elements[index].elementId).data('color', 'lightgreen');
          }
        }
      }
      break;
    case 'issues':
      document.getElementById('issues').innerHTML = message.issues;
      defaultUI();
      break;
    case 'noIssues':
      document.getElementById('issues').innerHTML = 'No issues';
      break;
    case 'visitedElement':
      console.log('Command visitedElement. Will color green on (modelId, elementId): ' +
        message.modelId + ', ' + message.elementId);
      var str = 'Steps: ' + message.totalCount + ', Fulfilment: ' +
        (message.stopConditionFulfillment * 100).toFixed(0) + '%';
      if (!jQuery.isEmptyObject(message.data)) {
        str += ', Data: ' + JSON.stringify(message.data);
      }
      document.getElementById('issues').innerHTML = str;

      currentModelId = message.modelId;
      graphs[currentModelId].nodes().unselect();
      graphs[currentModelId].edges().unselect();

      var tabs = $('#tabs');
      var index = tabs.find('a[href="#A-' + currentModelId + '"]').parent().index();
      tabs.tabs('option', 'active', index);

      if (!breakPoints.some(function(e) {
          return e.data().id === message.elementId
        })) {
        graphs[currentModelId].$('#' + message.elementId).data('color', 'lightgreen');
      }
      graphs[currentModelId].$('#' + message.elementId).select();
      break;
    case 'convertGraphml':
      if (message.success) {
        document.getElementById('issues').innerHTML = 'No issues';
        console.log('Command getModel ok');

        removeTest();

        readGraphFromJSON(JSON.parse(message.models));

        var tabs = $('#tabs');
        tabs.show();
        for (var modelId in graphs) {
          if (!graphs.hasOwnProperty(modelId)) {
            continue;
          }
          var index = $('#tabs').find('a[href="#A-' + modelId + '"]').parent().index();
          tabs.tabs('option', 'active', index);
          graphs[modelId].resize();
          graphs[modelId].fit();
        }
      }
      defaultUI();
      document.dispatchEvent(getModelEvent);
      break;
    default:
      break;
  }
}

function onError(evt) {
  console.error('onError');
  document.getElementById('issues').style.backgroundColor = "lightred";
  if (websocket.readyState == 1) {
    console.error('ws normal error: ' + evt.type);
    document.getElementById('issues').innerHTML = evt.type;
  } else {
    document.getElementById('issues').innerHTML = 'Error while connecting';
  }
}

function doSend(message) {
  console.log('doSend: ' + message);

  // Wait until the state of the socket is not ready and send the message when it is...
  waitForSocketConnection(websocket, function() {
    websocket.send(message);
  });
}

// Make the function wait until the connection is made...
function waitForSocketConnection(socket, callback) {
  setTimeout(
    function() {
      if (socket.readyState === 1) {
        console.log("Connection is made")
        if (callback != null) {
          callback();
        }
        return;
      } else {
        waitForSocketConnection(socket, callback);
      }
    }, 5); // wait 5 milisecond for the connection...
}

function enableModelControls() {
  $('#modelName').prop('disabled', false);
  $('#generator-builder').prop('disabled', false);
  $('#generator').prop('disabled', false);
  $('#modelActions').prop('disabled', false);
  $('#modelRequirements').prop('disabled', false);
}

function emptyInitialControlStates() {
  $('#modelName').val('');
  $('#modelName').prop('disabled', true);

  $('#generator-builder').val('');
  $('#generator-builder').prop('disabled', true);

  $('#generator').val('');
  $('#generator').prop('disabled', true);

  $('#modelActions').val('');
  $('#modelActions').prop('disabled', true);

  $('#modelRequirements').val('');
  $('#modelRequirements').prop('disabled', true);

  $('#label').val('');
  $('#label').val('').prop('disabled', true);

  $('#elementId').val('');
  $('#elementId').val('').prop('disabled', true);

  $('#sharedStateName').val('');
  $('#sharedStateName').val('').prop('disabled', true);

  $('#guard').val('');
  $('#guard').val('').prop('disabled', true);

  $('#weight').val('');
  $('#weight').val('').prop('disabled', true);

  $('#actions').val('');
  $('#actions').val('').prop('disabled', true);

  $('#requirements').val('');
  $('#requirements').val('').prop('disabled', true);
}

$(document).ready(function() {
  emptyInitialControlStates();
  onConnect();
  console.log(context);
  setTimeout(function () {
    context.init({
      fadeSpeed: 100,
      filter: function($obj) {},
      above: 'auto',
      preventDoubleContext: true,
      compress: false
    });
  }, 1);

  var generators = [
    "random",
    "quick_random",
    "a_star"
  ];

  var stop_conditions = [
    "edge_coverage",
    "vertex_coverage",
    "reached_vertex",
    "reached_edge",
    "time_duration",
    "never",
    "dependency_edge_coverage"
  ];

  var current_state = "generator";
  var result = "";
  var availableTags = generators;
  var autocompleteDisabled = false;

  $("#generator-builder").on('keypress', function(e) {
    if (e.which == 13 && $("#generator-builder").autocomplete("option", "disabled")) {
      e.preventDefault();
      result += $.trim($("#generator-builder").val()) + ")";
      $("#generator-builder").val("");
      current_state = "stop_condition_closed";
      availableTags = [];
      availableTags.push(")");
      availableTags.push("OR");
      availableTags.push("AND");
      $("#generator").val(result);
      $("#generator-builder").autocomplete("option", "disabled", false);
    }
  });


  $("#generator-builder").autocomplete({
    minLength: 0,
    source: function(request, resolve) {
      resolve(availableTags);
    },
    messages: {
      noResults: '',
      results: function() {}
    },
    select: function(e, ui) {
      switch (current_state) {
        case "generator":
          result += ui.item.value + "(";
          if (ui.item.value == "a_star") {
            availableTags = [];
            availableTags.push("reached_vertex");
            availableTags.push("reached_edge");
          } else {
            availableTags = stop_conditions;
          }
          current_state = "stop_condition";
          break;

        case "stop_condition":
          switch (ui.item.value) {
            case "edge_coverage":
            case "vertex_coverage":
              result += ui.item.value + "(";
              current_state = "number";
              $("#generator-builder").autocomplete("option", "disabled", true);
              break;

            case "reached_edge":
              result += ui.item.value + "(";
              current_state = "edge_or_vertex_name";
              availableTags = [];
              graphs[currentModelId].edges().each(function(index, edge) {
                availableTags.push(edge.data().label);
              });
              break;

            case "reached_vertex":
              result += ui.item.value + "(";
              current_state = "edge_or_vertex_name";
              availableTags = [];
              graphs[currentModelId].nodes().each(function(index, node) {
                if (node.data().startVertex === true) {
                  return true;
                }
                availableTags.push(node.data().label);
              });
              break;

            case "time_duration":
              result += ui.item.value + "(";
              current_state = "number";
              $("#generator-builder").autocomplete("option", "disabled", true);
              break;

            case "never":
              result += ui.item.value + ") ";
              current_state = "generator";
              availableTags = generators;
              break;
          }
          break;

        case "edge_or_vertex_name":
          result += ui.item.value + ")";
          availableTags = [];
          availableTags.push(")");
          availableTags.push("OR");
          availableTags.push("AND");
          current_state = "stop_condition_closed";
          break;

        case "stop_condition_closed":
          switch (ui.item.value) {
            case "OR":
            case "AND":
              result += " " + ui.item.value + " ";
              availableTags = stop_conditions;
              current_state = "stop_condition";
              break;

            default:
              result += ") ";
              availableTags = generators;
              current_state = "generator";
          }
          break;
      }
      this.value = "";
      $("#generator").val(result);
      return false;
    }
  });

});

// Hash array that holds all graphs/models.
var graphs =[];
var currentModelId = undefined;
var pauseExecution = false;
var stepExecution = false;
var keys = {};
var executionSpeed = 0;

window.onload = function() {
  document.getElementById('loading-mask').style.display='none';
}

function onLoadModel() {
  $("<input type='file' class='ui-helper-hidden-accessible' />").appendTo("body").focus().trigger('click').remove();
}

function onSaveModel() {

}

function onPausePlayExecution(element) {
  console.log("pausePlayExecution: " + element.innerHTML + ", pauseExecution: " + pauseExecution + ", clicked: " + currentModelId);
  stepExecution = false;

  if (pauseExecution) {
    document.getElementById("runModel").disabled = true;
    document.getElementById("resetModel").disabled = true;
    document.getElementById("pausePlayExecution").disabled = false;
    document.getElementById("stepExecution").disabled = true;
    document.getElementById("pausePlayExecution").innerHTML = "Pause";
    pauseExecution = false;

    var hasNext = {
      command: "hasNext"
    };
    doSend(JSON.stringify(hasNext));
  } else {
    document.getElementById("runModel").disabled = true;
    document.getElementById("resetModel").disabled = false;
    document.getElementById("pausePlayExecution").disabled = false;
    document.getElementById("stepExecution").disabled = false;
    document.getElementById("pausePlayExecution").innerHTML = "Run";
    pauseExecution = true;
  }
};

function onStepExecution() {
  console.log("onStepExecution: " + currentModelId);
  document.getElementById("runModel").disabled = true;
  document.getElementById("resetModel").disabled = false;
  document.getElementById("pausePlayExecution").disabled = false;
  document.getElementById("stepExecution").disabled = false;
  stepExecution = true;

  var hasNext = {
    command: "hasNext"
  };
  doSend(JSON.stringify(hasNext));
};

// Run the execution of the state machine
function onRunModel() {
  console.log("onRunModel: " + currentModelId);
  $( ".ui-panel" ).panel( "close" );

  document.getElementById("runModel").disabled = true;
  document.getElementById("resetModel").disabled = true;
  document.getElementById("pausePlayExecution").disabled = false;
  document.getElementById("stepExecution").disabled = true;
  document.getElementById("addModel").disabled = true;
  stepExecution = false;
  pauseExecution = false;

  var start = {
    command: "start",
    gw3: {
      name: "GraphWalker Studio",
      models: []
    }
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
    }

    if (graphs[modelId].startElementId !== undefined) {
      model.startElementId = graphs[modelId].startElementId;
    }
    graphs[modelId].nodes().each( function( index, node) {

      actions = [];
      if (node.data().actions) {
        actions.push(node.data().actions);
      }
      requirements = [];
      if (node.data().requirements) {
        requirements = node.data().requirements.split(',');
      }

      var vertex = {
        id: node.data().id,
        name: node.data().label,
        sharedState: node.data().sharedState,
        actions: actions,
        requirements: requirements,
        properties: node.data().properties
      };
      model.vertices.push(vertex);
    });
    graphs[modelId].edges().each( function( index, edge) {

      actions = [];
      if (edge.data().actions) {
        actions.push(edge.data().actions);
      }
      requirements = [];
      if (edge.data().requirements) {
        requirements = edge.data().requirements.split(',');
      }

      var edge = {
        id: edge.data().id,
        name: edge.data().label,
        guard: edge.data().guard,
        actions: actions,
        requirements: requirements,
        properties: edge.data().properties,
        sourceVertexId: edge.data().source,
        targetVertexId: edge.data().target
      };
      model.edges.push(edge);
    });
    start.gw3.models.push(model);
  }

  doSend(JSON.stringify(start));
};

// Reset the state machine to it's initial state
function onResetModel() {
  console.log("onResetModel: " + currentModelId);
  defaultUI();

  issues.innerHTML = "Ready";

  for (var modelId in graphs) {
    if (!graphs.hasOwnProperty(modelId)) {
      continue;
    }

    graphs[modelId].nodes().unselect();
    graphs[modelId].edges().unselect();
    graphs[modelId].nodes().data('color', 'LightSteelBlue');
    graphs[modelId].edges().data('color', 'LightSteelBlue');
    graphs[modelId].nodes().filterFn(function( ele ){
      return ele.data('startVertex') == true;
    }).data('color', 'LightGreen');
  }

};

function onAddModel() {
  console.log("onAddModel");
  var id = generateUUID();
  var graph = createTab(id, "New model");
  graph.name = "New model";

  $("#tabs").show();
  var index = $('#tabs a[href="#A-' + id + '"]').parent().index();
  $("#tabs").tabs("option", "active", index);
};

function onDoLayout() {
  console.log("onDoLayout");
  if (graphs[currentModelId] !== undefined) {
    graphs[currentModelId].layout().stop();
    var layout = graphs[currentModelId].makeLayout({
      name: 'dagre',
      animate: true,
      minLen: function( edge ) {
        return 2;
      }
    });
    layout.run();
  }
};



/*
 ************************************************************************
 * CREATE SOME CUSTOM EVENTS THAT HANDLES MODEL EXECUTION
 ************************************************************************
 */
var startEvent = new CustomEvent("startEvent", {});
document.addEventListener("startEvent", function (e) {
  console.log("startEvent: " + currentModelId);

  // Change some UI elements
  document.getElementById("runModel").disabled = true;
  document.getElementById("resetModel").disabled = true;
  document.getElementById("pausePlayExecution").disabled = false;
  document.getElementById("stepExecution").disabled = true;

  var hasNext = {
    command: "hasNext"
  };
  doSend(JSON.stringify(hasNext));
});

var hasNextEvent = new CustomEvent("hasNextEvent", {});
document.addEventListener("hasNextEvent", function (e) {
  console.log("hasNextEvent: pauseExecution: " + pauseExecution + ", stepExecution: " + stepExecution + " : modelId " + currentModelId);
  if (pauseExecution) {
    if (!stepExecution) {
      return;
    }
  }
  var getNext = {
    command: "getNext"
  };
  doSend(JSON.stringify(getNext));
});

var getNextEvent = new CustomEvent("getNextEvent", {"modelId": "", "elementId": "", "name": ""});
document.addEventListener("getNextEvent", function (e) {
  console.log("getNextEvent: " + e.id + ": " + e.name + "pauseExecution: " + pauseExecution + ", stepExecution: " + stepExecution + " : modelId " + currentModelId);

  if (stepExecution) {
    stepExecution = false;
    return;
  }

  var hasNext = {
    command: "hasNext"
  };
  setTimeout(function () {
    doSend(JSON.stringify(hasNext));
  }, $('#executionSpeedSlider').val());
});

function removeModel(modelId) {
  console.log("Remove model with id: " + modelId);
  delete graphs[modelId];
}


document.addEventListener('DOMContentLoaded', function () {

  $("#tabs").delegate("span.ui-icon-close", "click", function () {
    var id = $(this).closest("li").remove().attr("aria-controls").substr(2);
    $("#A-" + id).remove();
    $("#tabs").tabs("refresh");
    if ($("#tabs").find("li").length < 1) {
      $("#tabs").hide();
    }
    removeModel(id);
  });

  $('#tabs').tabs({
    activate: function (event, ui) {
      currentModelId = ui.newPanel.attr('id').substr(2);
      console.log("tabs activate: " + currentModelId);
      graphs[currentModelId].resize();
      $('#modelName').val(graphs[currentModelId].name);
      $('#generator').val(graphs[currentModelId].generator);
      $('#modelActions').val(graphs[currentModelId].actions);
      $('#modelRequirements').val(graphs[currentModelId].requirements);
    }
  });

  // Hide the tab component. It will get visible when the graps are loaded.
  $("#tabs").hide();

  $(document).keyup(function(e) {
    console.log("key up: " + e.which);
    delete keys[e.which];
  });

  $(document).keydown(function(e) {
    console.log("key down: " + e.which);
    keys[e.which] = true;

    if (keys[46]) {  // Delete key is pressed
      if (graphs[currentModelId] !== undefined) {
        graphs[currentModelId].remove(':selected');
      }
    }
  });

  $('#modelName').on('input',function(e){
    if (graphs[currentModelId]) {
      graphs[currentModelId].name = $('#modelName').val();
      var selectedTab = $("#tabs").tabs( "option", "selected" );
      $("#tabs ul li a").eq(selectedTab).text($('#modelName').val());
    }
  });

  $('#generator').on('input',function(e){
    if (graphs[currentModelId]) {
      graphs[currentModelId].generator = $('#generator').val();
    }
  });

  $('#modelActions').on('input',function(e){
    if (graphs[currentModelId]) {
      graphs[currentModelId].actions = $('#modelActions').val();
    }
  });

  $('#modelRequirements').on('input',function(e){
    if (graphs[currentModelId]) {
      graphs[currentModelId].requirements = $('#modelRequirements').val();
    }
  });


  /**
   * Place the gw3 files in:
   * graphwalker-studio/src/main/resources/static/
   **/
  //readGraphsFromFile("Login.gw3");
  //readGraphsFromFile("UC01.gw3");
  //readGraphsFromFile("petClinic.gw3");
});


function createTab(modelId, modelName) {
  console.log("createTab: " + modelId + ", " + modelName);

  var tabs = $("#tabs").tabs();
  var ul = tabs.find("ul");

  // ID tokens must begin with a letter ([A-Za-z])
  // https://www.w3.org/TR/html401/types.html#type-name
  var href = "#A-" + modelId;
  $("<li><a href='" + href + "'>" + modelName + "</a><span class='ui-icon ui-icon-close'></span></li>").appendTo(ul);
  $("<div id='A-" + modelId + "'></div>").appendTo(tabs);
  tabs.tabs("refresh");

  $("<style>").prop("type", "text/css").html("\
                    " + href + " {\
                        background: floralwhite;\
                        position: relative;\
                        height:100%;\
                        overflow-y: hidden;\
                    }").appendTo("head");

  return createGraph(modelId);
}

function createGraph(currentModelId) {
  console.log("createGraph - " + currentModelId);
  var mouseX, mouseY;

  var graph = cytoscape({
    id: currentModelId,
    container: document.querySelector('#A-' + currentModelId),

    currentElement: null,


    'boxSelectionEnabled': true,
    'wheelSensitivity': '0', // Values 0, 0.5 and 1 has the same effect...

    ready: function(evt) {
      console.log("Cytoscape is ready...")
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
//                        'curve-style' : 'unbundled-bezier',
//                        'edge-text-rotation': 'autorotate',
        'target-arrow-shape': 'triangle',
        'width': '4',
        'line-color': 'data(color)',
        'target-arrow-color': 'data(color)',
        'background-color': 'data(color)'
      })

      .selector(':selected')
      .css({
        'background-color': 'MediumSlateBlue ',
        'line-color': 'MediumSlateBlue ',
        'target-arrow-color': 'MediumSlateBlue '
      })

  });

  var srcNode = null;
  graph.on("tapstart", 'node', function(event) {
    if (keys[69]) {  // e key is pressed
      console.log("tapstart with e key pressed on node: " + this.id());
      srcNode = this;
      graph.autoungrabify(true);
    }
  });

  var dstNode = null;
  graph.on("tapend", 'node', function(event) {
    if (keys[69]) {  // e key is pressed
      console.log("tapend with e key pressed on node: " + this.id());
      dstNode = this;
      graph.autoungrabify(false);

      if (srcNode !== undefined && dstNode !== undefined) {
        var id = generateUUID();
        graph.add({
          group: "edges",
          data: {
            id: id,
            source: srcNode.id(),
            target: dstNode.id(),
            label: "e_NewEdge",
            name: formatElementName({
              name: "e_NewEdge"
            }),
            color: 'LightSteelBlue'
          }
        });
        console.log("  Added edge: " + id);
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
    if(tappedBefore === tappedNow) {
      tappedNow.trigger('doubleTap');
      tappedBefore = null;
    } else {
      tappedTimeout = setTimeout(function(){ tappedBefore = null; }, 300);
      tappedBefore = tappedNow;
    }

    if (keys[86]) {  // v key is pressed
      console.log("tap and v key pressed");
      var id = generateUUID();
      graph.add({
        group: "nodes",
        data: {
          id: id,
          label: "v_NewVertex",
          name: formatElementName({
            name: "v_NewVertex"
          }),
          color: 'LightSteelBlue'
        },
        renderedPosition: {
          x: event.cyRenderedPosition.x,
          y: event.cyRenderedPosition.y
        }
      });
      console.log("  Added vertex: " + id);
    }

    currentElement = null;

    $('#label').val("");
    $('#label').textinput('disable');

    $('#sharedStateName').val("");
    $('#sharedStateName').textinput('disable');

    $('#guard').val("");
    $('#guard').textinput('disable');

    $('#actions').val("");
    $('#actions').textinput('disable');

    $('#requirements').val("");
    $('#requirements').textinput('disable');

    $('#checkboxStartElement').attr("checked", false).checkboxradio("refresh");
    $('#checkboxStartElement').checkboxradio('disable');

  });

  graph.on('tap', 'node', function(event) {
    currentElement = this;
    $('#label').textinput('enable');
    $('#label').val(this.data().label);

    $('#sharedStateName').textinput('enable');
    $('#sharedStateName').val(this.data().sharedState);

    $('#actions').textinput('enable');
    $('#actions').val(this.data().actions);

    $('#requirements').textinput('enable');
    $('#requirements').val(this.data().requirements);

    $('#checkboxStartElement').checkboxradio('enable');
    if (graph.startElementId == this.id()) {
      $('#checkboxStartElement').prop('checked', true).checkboxradio('refresh');
    }
  });

  graph.on('tap', 'edge', function(event) {
    currentElement = this;
    $('#label').textinput('enable');
    $('#label').val(this.data().label);

    $('#guard').textinput('enable');
    $('#guard').val(this.data().guard);

    $('#actions').textinput('enable');
    $('#actions').val(this.data().actions);

    $('#requirements').textinput('enable');
    $('#requirements').val(this.data().requirements);

    $('#checkboxStartElement').checkboxradio('enable');
    if (graph.startElementId == this.id()) {
      $('#checkboxStartElement').prop('checked', true).checkboxradio('refresh');
    }
  });

  $('#label').on('input',function(e){
    if (currentElement) {
      currentElement.data('label', $('#label').val());
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#sharedStateName').on('input',function(e){
    if (currentElement) {
      currentElement.data('sharedState', $('#sharedStateName').val());
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#guard').on('input',function(e){
    if (currentElement) {
      currentElement.data('guard', $('#guard').val());
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        guard: currentElement.data().guard,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#actions').on('input',function(e){
    if (currentElement) {
      currentElement.data('actions', $('#actions').val());
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#requirements').on('input',function(e){
    if (currentElement) {
      currentElement.data('requirements', $('#requirements').val());
      currentElement.data('name', formatElementName({
        name: currentElement.data().label,
        sharedState: currentElement.data().sharedState,
        guard: currentElement.data().guard,
        actions: currentElement.data().actions,
        requirements: currentElement.data().requirements
      }));
    }
  });

  $('#checkboxStartElement').change(function() {
    if ($(this).is(":checked")) {
      if (currentElement) {
        graph.startElementId = currentElement.id();
      }
    } else {
      if (currentElement == graph.data().startElementId) {
        graph.startElementId = undefined;
      }
    }
  });

  graph.on('doubleTap', function(event) {
    $( "#nav-panel" ).panel( "open" );
  });

  $( ".ui-panel" ).panel({
    close: function( event, ui ) {
      console.log("Resize the model, because the side panel has been closed");
      graph.resize();
    }
  });

  $( ".ui-panel" ).panel({
    open: function( event, ui ) {
      console.log("Resize the model, because the side panel has been opened");
      graph.resize();
    }
  });

  graphs[currentModelId] = graph;
  return graph;
}

function readGraphsFromFile(fileName) {
  console.log("readGraphFromFile - " + fileName);

  // Assign handlers immediately after making the request,
  // and remember the jqxhr object for this request
  var jqxhr = $.getJSON(fileName, function () {
      console.log("readGraphsFromFile: success");
    })
    .done(function (jsonGraphs) {
      console.log("readGraphsFromFile: done");
      readGraphFromJSON(jsonGraphs);
    })
    .fail(function () {
      console.log("readGraphsFromFile: error");
    })
    .always(function () {
      console.log("readGraphsFromFile: first complete");
      $("#tabs").show();
      for (var modelId in graphs) {
        if (!graphs.hasOwnProperty(modelId)) {
          continue;
        }
        console.log("readGraphsFromFile: resize graph: " + modelId);

        var index = $('#tabs a[href="#A-' + modelId + '"]').parent().index();
        $("#tabs").tabs("option", "active", index);
        graphs[modelId].resize();
        graphs[modelId].fit();
      }
      defaultUI();
    });
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
      name = "Model: " + modelIndex;
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
      var x = 0, y = 0;
      if (jsonVertex.properties != undefined) {
        if (jsonVertex.properties["x"] != undefined) {
          x = jsonVertex.properties["x"];
        }
        if (jsonVertex.properties["y"] != undefined) {
          y = jsonVertex.properties["y"];
        }
      } else {
        jsonVertex.properties = {};
      }

      var actions = undefined;
      var requirements = undefined;
      if (jsonVertex.hasOwnProperty('actions')) {
        actions = jsonVertex.actions.join('');
      }
      if (jsonVertex.hasOwnProperty('requirements')) {
        requirements = jsonVertex.requirements.join();
      }

      graph.add({
        group: "nodes",
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
          actions: actions,
          requirements: requirements,
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
      if (jsonEdge.sourceVertexId === undefined) {
        jsonEdge.sourceVertexId = "Start";

        graph.add({
          group: "nodes",
          data: {
            id: jsonEdge.sourceVertexId,
            name: "Start",
            startVertex: true,
            color: 'LightGreen'
          },
          position: {
            x: 0,
            y: 0
          }
        });
      }

      var actions = undefined;
      var requirements = undefined;
      if (jsonEdge.hasOwnProperty('actions')) {
        actions = jsonEdge.actions.join('');
      }
      if (jsonEdge.hasOwnProperty('requirements')) {
        requirements = jsonEdge.requirements.join();
      }

      graph.add({
        group: "edges",
        data: {
          id: jsonEdge.id,
          source: jsonEdge.sourceVertexId,
          target: jsonEdge.targetVertexId,
          label: jsonEdge.name,
          name: formatElementName({
            name: jsonEdge.name,
            guard: jsonEdge.guard,
            actions: jsonEdge.actions
          }),
          guard: jsonEdge.guard,
          actions: actions,
          requirements: requirements,
          properties: jsonEdge.properties,
          color: 'LightSteelBlue'
        }
      });
    }

    graphs[graph.id] = graph;
  }
  return graphs;
}

function defaultUI() {
  console.log("defaultUI");

  if (Object.keys(graphs).length > 0 && currentModelId !== undefined) {
    document.getElementById("runModel").disabled = false;
    document.getElementById("resetModel").disabled = true;
    document.getElementById("stepExecution").disabled = true;
    document.getElementById("pausePlayExecution").innerHTML = "Pause";
    document.getElementById("pausePlayExecution").disabled = true;
    document.getElementById("addModel").disabled = false;
  } else {
    document.getElementById("runModel").disabled = false;
    document.getElementById("resetModel").disabled = false;
    document.getElementById("stepExecution").disabled = false;
    document.getElementById("pausePlayExecution").innerHTML = "Pause";
    document.getElementById("pausePlayExecution").disabled = false;
    document.getElementById("addModel").disabled = false;
  }
}

function generateUUID() {
  console.log("generateUUID");
  var d = new Date().getTime();
  var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = (d + Math.random() * 16) % 16 | 0;
    d = Math.floor(d / 16);
    return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
  console.log("  UUID: " + uuid);
  return uuid;
}

function formatElementName(jsonObj) {
  var str = "";
  if (jsonObj.name) {
    str += 'Name: ' + jsonObj.name + '\n';
  }
  if (jsonObj.sharedState) {
    str += 'Shared state name: ' + jsonObj.sharedState + '\n';
  }
  if (jsonObj.guard) {
    str += 'Guard: ' + jsonObj.guard + '\n';
  }
  if (jsonObj.actions) {
    str += 'Actions: ' + jsonObj.actions + '\n';
  }
  if (jsonObj.requirements) {
    str += 'Requirements: ' + jsonObj.requirements + '\n';
  }
  return str.slice(0, -1);
}


/*************************************************************************
 *
 * WEBSOCKET CLIENT TO GRAPHWALKER
 *
 *************************************************************************/
var wsUri = "ws://localhost:9999";
var websocket;
var messageState = testWebSocket();
function testWebSocket() {
  websocket = new WebSocket(wsUri);
  websocket.onopen = function (evt) {
    onOpen(evt);
  };
  websocket.onclose = function (evt) {
    onClose(evt);
  };
  websocket.onmessage = function (evt) {
    onMessage(evt);
  };
  websocket.onerror = function (evt) {
    onError(evt);
  };
}
function onOpen(evt) {
  console.log('onOpen: ' + evt.data);
}
function onClose(evt) {
  console.log('onClose: ' + evt.data);
}
function onMessage(evt) {
  console.log("onMessage: " + evt.data);
  var msg = JSON.parse(event.data);

  switch (msg.command) {
    case "hasNext":
      if (msg.success) {
        console.log("Command hasNext: " + msg.hasNext);
        if (msg.hasNext) {
          hasNextEvent.fullfilled = msg.hasNext;
          document.dispatchEvent(hasNextEvent);
        } else {
          defaultUI();
          document.getElementById("runModel").disabled = true;
          document.getElementById("resetModel").disabled = false;
        }
      } else {
        defaultUI();
      }
      break;
    case "getNext":
      if (msg.success) {
        console.log("Command getNext ok");
        document.dispatchEvent(getNextEvent, msg.modelId, msg.elementId, msg.name);
      } else {
        defaultUI();
      }
      break;
    case "start":
      if (msg.success) {
        issues.innerHTML = "No issues";
        console.log("Command start ok");
        document.dispatchEvent(startEvent);
      } else {
        defaultUI();
      }
      break;
    case "issues":
      issues.innerHTML = msg.issues;
      break;
    case "noIssues":
      issues.innerHTML = "No issues";
      break;
    case "visitedElement":
      console.log("Command visitedElement. Will color green on (modelId, elementId): " + msg.modelId + ", " + msg.elementId);
      issues.innerHTML = "Steps: " + msg.totalCount + ", Done: " + (msg.stopConditionFulfillment * 100).toFixed(0) + "%, data: " + JSON.stringify(msg.data);

      currentModelId = msg.modelId;
      graphs[currentModelId].nodes().unselect();
      graphs[currentModelId].edges().unselect();

      var index = $('#tabs a[href="#A-' + currentModelId + '"]').parent().index();
      $("#tabs").tabs("option", "active", index);

      graphs[currentModelId].$('#'+msg.elementId).data('color', 'lightgreen');
      graphs[currentModelId].$('#'+msg.elementId).select();
      break;
    default:
      break;
  }
}
function onError(evt) {
  console.error('Error: ' + evt.data);
}
function doSend(message) {
  console.log('Sending msgs: ' + message);
  websocket.send(message);
}

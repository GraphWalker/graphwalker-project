var graphwalker =
/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ function(module, exports, __webpack_require__) {

	'use strict';

	__webpack_require__(1);

	window.onload = function () {
	  document.getElementById('loading-mask').style.display = 'none';
	};

	module.exports = {
	  studio: __webpack_require__(5)
	};

/***/ },
/* 1 */
/***/ function(module, exports, __webpack_require__) {

	// style-loader: Adds some css to the DOM by adding a <style> tag

	// load the styles
	var content = __webpack_require__(2);
	if(typeof content === 'string') content = [[module.id, content, '']];
	// add the styles to the DOM
	var update = __webpack_require__(4)(content, {});
	if(content.locals) module.exports = content.locals;
	// Hot Module Replacement
	if(false) {
		// When the styles change, update the <style> tags
		if(!content.locals) {
			module.hot.accept("!!./../node_modules/css-loader/index.js!./style.css", function() {
				var newContent = require("!!./../node_modules/css-loader/index.js!./style.css");
				if(typeof newContent === 'string') newContent = [[module.id, newContent, '']];
				update(newContent);
			});
		}
		// When the module is disposed, remove the <style> tags
		module.hot.dispose(function() { update(); });
	}

/***/ },
/* 2 */
/***/ function(module, exports, __webpack_require__) {

	exports = module.exports = __webpack_require__(3)();
	// imports


	// module
	exports.push([module.id, "body {\n  font: 14px helvetica neue, helvetica, arial, sans-serif;\n  margin: 0;\n  padding: 0;\n}\n\n#tabs {\n  position: absolute;\n  top: 0;\n  bottom:0;\n  left: 0;\n  right: 0;\n  overflow:hidden;\n}\n\n#tabs div {\n  height: 100%;\n}\n\n#tabs li .ui-icon-close {\n  float: left;\n  margin: 0.4em 0.2em 0 0;\n  cursor: pointer;\n}\n\n.ui-tabs .ui-tabs-panel {\n  padding: 0;\n}\n", ""]);

	// exports


/***/ },
/* 3 */
/***/ function(module, exports) {

	/*
		MIT License http://www.opensource.org/licenses/mit-license.php
		Author Tobias Koppers @sokra
	*/
	// css base code, injected by the css-loader
	module.exports = function() {
		var list = [];

		// return the list of modules as css string
		list.toString = function toString() {
			var result = [];
			for(var i = 0; i < this.length; i++) {
				var item = this[i];
				if(item[2]) {
					result.push("@media " + item[2] + "{" + item[1] + "}");
				} else {
					result.push(item[1]);
				}
			}
			return result.join("");
		};

		// import a list of modules into the list
		list.i = function(modules, mediaQuery) {
			if(typeof modules === "string")
				modules = [[null, modules, ""]];
			var alreadyImportedModules = {};
			for(var i = 0; i < this.length; i++) {
				var id = this[i][0];
				if(typeof id === "number")
					alreadyImportedModules[id] = true;
			}
			for(i = 0; i < modules.length; i++) {
				var item = modules[i];
				// skip already imported module
				// this implementation is not 100% perfect for weird media query combinations
				//  when a module is imported multiple times with different media queries.
				//  I hope this will never occur (Hey this way we have smaller bundles)
				if(typeof item[0] !== "number" || !alreadyImportedModules[item[0]]) {
					if(mediaQuery && !item[2]) {
						item[2] = mediaQuery;
					} else if(mediaQuery) {
						item[2] = "(" + item[2] + ") and (" + mediaQuery + ")";
					}
					list.push(item);
				}
			}
		};
		return list;
	};


/***/ },
/* 4 */
/***/ function(module, exports, __webpack_require__) {

	/*
		MIT License http://www.opensource.org/licenses/mit-license.php
		Author Tobias Koppers @sokra
	*/
	var stylesInDom = {},
		memoize = function(fn) {
			var memo;
			return function () {
				if (typeof memo === "undefined") memo = fn.apply(this, arguments);
				return memo;
			};
		},
		isOldIE = memoize(function() {
			return /msie [6-9]\b/.test(window.navigator.userAgent.toLowerCase());
		}),
		getHeadElement = memoize(function () {
			return document.head || document.getElementsByTagName("head")[0];
		}),
		singletonElement = null,
		singletonCounter = 0,
		styleElementsInsertedAtTop = [];

	module.exports = function(list, options) {
		if(false) {
			if(typeof document !== "object") throw new Error("The style-loader cannot be used in a non-browser environment");
		}

		options = options || {};
		// Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
		// tags it will allow on a page
		if (typeof options.singleton === "undefined") options.singleton = isOldIE();

		// By default, add <style> tags to the bottom of <head>.
		if (typeof options.insertAt === "undefined") options.insertAt = "bottom";

		var styles = listToStyles(list);
		addStylesToDom(styles, options);

		return function update(newList) {
			var mayRemove = [];
			for(var i = 0; i < styles.length; i++) {
				var item = styles[i];
				var domStyle = stylesInDom[item.id];
				domStyle.refs--;
				mayRemove.push(domStyle);
			}
			if(newList) {
				var newStyles = listToStyles(newList);
				addStylesToDom(newStyles, options);
			}
			for(var i = 0; i < mayRemove.length; i++) {
				var domStyle = mayRemove[i];
				if(domStyle.refs === 0) {
					for(var j = 0; j < domStyle.parts.length; j++)
						domStyle.parts[j]();
					delete stylesInDom[domStyle.id];
				}
			}
		};
	}

	function addStylesToDom(styles, options) {
		for(var i = 0; i < styles.length; i++) {
			var item = styles[i];
			var domStyle = stylesInDom[item.id];
			if(domStyle) {
				domStyle.refs++;
				for(var j = 0; j < domStyle.parts.length; j++) {
					domStyle.parts[j](item.parts[j]);
				}
				for(; j < item.parts.length; j++) {
					domStyle.parts.push(addStyle(item.parts[j], options));
				}
			} else {
				var parts = [];
				for(var j = 0; j < item.parts.length; j++) {
					parts.push(addStyle(item.parts[j], options));
				}
				stylesInDom[item.id] = {id: item.id, refs: 1, parts: parts};
			}
		}
	}

	function listToStyles(list) {
		var styles = [];
		var newStyles = {};
		for(var i = 0; i < list.length; i++) {
			var item = list[i];
			var id = item[0];
			var css = item[1];
			var media = item[2];
			var sourceMap = item[3];
			var part = {css: css, media: media, sourceMap: sourceMap};
			if(!newStyles[id])
				styles.push(newStyles[id] = {id: id, parts: [part]});
			else
				newStyles[id].parts.push(part);
		}
		return styles;
	}

	function insertStyleElement(options, styleElement) {
		var head = getHeadElement();
		var lastStyleElementInsertedAtTop = styleElementsInsertedAtTop[styleElementsInsertedAtTop.length - 1];
		if (options.insertAt === "top") {
			if(!lastStyleElementInsertedAtTop) {
				head.insertBefore(styleElement, head.firstChild);
			} else if(lastStyleElementInsertedAtTop.nextSibling) {
				head.insertBefore(styleElement, lastStyleElementInsertedAtTop.nextSibling);
			} else {
				head.appendChild(styleElement);
			}
			styleElementsInsertedAtTop.push(styleElement);
		} else if (options.insertAt === "bottom") {
			head.appendChild(styleElement);
		} else {
			throw new Error("Invalid value for parameter 'insertAt'. Must be 'top' or 'bottom'.");
		}
	}

	function removeStyleElement(styleElement) {
		styleElement.parentNode.removeChild(styleElement);
		var idx = styleElementsInsertedAtTop.indexOf(styleElement);
		if(idx >= 0) {
			styleElementsInsertedAtTop.splice(idx, 1);
		}
	}

	function createStyleElement(options) {
		var styleElement = document.createElement("style");
		styleElement.type = "text/css";
		insertStyleElement(options, styleElement);
		return styleElement;
	}

	function createLinkElement(options) {
		var linkElement = document.createElement("link");
		linkElement.rel = "stylesheet";
		insertStyleElement(options, linkElement);
		return linkElement;
	}

	function addStyle(obj, options) {
		var styleElement, update, remove;

		if (options.singleton) {
			var styleIndex = singletonCounter++;
			styleElement = singletonElement || (singletonElement = createStyleElement(options));
			update = applyToSingletonTag.bind(null, styleElement, styleIndex, false);
			remove = applyToSingletonTag.bind(null, styleElement, styleIndex, true);
		} else if(obj.sourceMap &&
			typeof URL === "function" &&
			typeof URL.createObjectURL === "function" &&
			typeof URL.revokeObjectURL === "function" &&
			typeof Blob === "function" &&
			typeof btoa === "function") {
			styleElement = createLinkElement(options);
			update = updateLink.bind(null, styleElement);
			remove = function() {
				removeStyleElement(styleElement);
				if(styleElement.href)
					URL.revokeObjectURL(styleElement.href);
			};
		} else {
			styleElement = createStyleElement(options);
			update = applyToTag.bind(null, styleElement);
			remove = function() {
				removeStyleElement(styleElement);
			};
		}

		update(obj);

		return function updateStyle(newObj) {
			if(newObj) {
				if(newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap)
					return;
				update(obj = newObj);
			} else {
				remove();
			}
		};
	}

	var replaceText = (function () {
		var textStore = [];

		return function (index, replacement) {
			textStore[index] = replacement;
			return textStore.filter(Boolean).join('\n');
		};
	})();

	function applyToSingletonTag(styleElement, index, remove, obj) {
		var css = remove ? "" : obj.css;

		if (styleElement.styleSheet) {
			styleElement.styleSheet.cssText = replaceText(index, css);
		} else {
			var cssNode = document.createTextNode(css);
			var childNodes = styleElement.childNodes;
			if (childNodes[index]) styleElement.removeChild(childNodes[index]);
			if (childNodes.length) {
				styleElement.insertBefore(cssNode, childNodes[index]);
			} else {
				styleElement.appendChild(cssNode);
			}
		}
	}

	function applyToTag(styleElement, obj) {
		var css = obj.css;
		var media = obj.media;

		if(media) {
			styleElement.setAttribute("media", media)
		}

		if(styleElement.styleSheet) {
			styleElement.styleSheet.cssText = css;
		} else {
			while(styleElement.firstChild) {
				styleElement.removeChild(styleElement.firstChild);
			}
			styleElement.appendChild(document.createTextNode(css));
		}
	}

	function updateLink(linkElement, obj) {
		var css = obj.css;
		var sourceMap = obj.sourceMap;

		if(sourceMap) {
			// http://stackoverflow.com/a/26603875
			css += "\n/*# sourceMappingURL=data:application/json;base64," + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + " */";
		}

		var blob = new Blob([css], { type: "text/css" });

		var oldSrc = linkElement.href;

		linkElement.href = URL.createObjectURL(blob);

		if(oldSrc)
			URL.revokeObjectURL(oldSrc);
	}


/***/ },
/* 5 */
/***/ function(module, exports, __webpack_require__) {

	'use strict';

	Object.defineProperty(exports, "__esModule", {
	  value: true
	});
	exports.onLoadModel = onLoadModel;
	exports.onSaveModel = onSaveModel;
	exports.onPausePlayExecution = onPausePlayExecution;
	exports.onStepExecution = onStepExecution;
	exports.onRunModel = onRunModel;
	exports.onResetModel = onResetModel;
	exports.onAddModel = onAddModel;
	exports.onDoLayout = onDoLayout;
	var cytoscape = __webpack_require__(6);

	// Hash array that holds all graphs/models.
	var graphs = [];
	var currentModelId;
	var pauseExecution = false;
	var stepExecution = false;
	var keys = {};
	var issues;
	var currentElement;

	function onLoadModel() {
	  $('<input type="file" class="ui-helper-hidden-accessible" />').appendTo('body').focus().trigger('click').remove();
	}

	function onSaveModel() {
	  console.log('onSaveModel called');
	}

	function onPausePlayExecution(element) {
	  console.log('pausePlayExecution: ' + element.innerHTML + ', pauseExecution: ' + pauseExecution + ', clicked: ' + currentModelId);
	  stepExecution = false;

	  if (pauseExecution) {
	    document.getElementById('runModel').disabled = true;
	    document.getElementById('resetModel').disabled = true;
	    document.getElementById('pausePlayExecution').disabled = false;
	    document.getElementById('stepExecution').disabled = true;
	    document.getElementById('pausePlayExecution').innerHTML = 'Pause';
	    pauseExecution = false;

	    var hasNext = {
	      command: 'hasNext'
	    };
	    doSend(JSON.stringify(hasNext));
	  } else {
	    document.getElementById('runModel').disabled = true;
	    document.getElementById('resetModel').disabled = false;
	    document.getElementById('pausePlayExecution').disabled = false;
	    document.getElementById('stepExecution').disabled = false;
	    document.getElementById('pausePlayExecution').innerHTML = 'Run';
	    pauseExecution = true;
	  }
	}

	function onStepExecution() {
	  console.log('onStepExecution: ' + currentModelId);
	  document.getElementById('runModel').disabled = true;
	  document.getElementById('resetModel').disabled = false;
	  document.getElementById('pausePlayExecution').disabled = false;
	  document.getElementById('stepExecution').disabled = false;
	  stepExecution = true;

	  var hasNext = {
	    command: 'hasNext'
	  };
	  doSend(JSON.stringify(hasNext));
	}

	// Run the execution of the state machine
	function onRunModel() {
	  console.log('onRunModel: ' + currentModelId);
	  $('.ui-panel').panel('close');

	  document.getElementById('runModel').disabled = true;
	  document.getElementById('resetModel').disabled = true;
	  document.getElementById('pausePlayExecution').disabled = false;
	  document.getElementById('stepExecution').disabled = true;
	  document.getElementById('addModel').disabled = true;
	  stepExecution = false;
	  pauseExecution = false;

	  var start = {
	    command: 'start',
	    gw3: {
	      name: 'GraphWalker Studio',
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
	    };

	    if (graphs[modelId].startElementId !== undefined) {
	      model.startElementId = graphs[modelId].startElementId;
	    }
	    graphs[modelId].nodes().each(function (index, node) {

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
	    graphs[modelId].edges().each(function (index, edge) {
	      actions = [];
	      if (edge.data().actions) {
	        actions.push(edge.data().actions);
	      }
	      requirements = [];
	      if (edge.data().requirements) {
	        requirements = edge.data().requirements.split(',');
	      }

	      var newEdge = {
	        id: edge.data().id,
	        name: edge.data().label,
	        guard: edge.data().guard,
	        actions: actions,
	        requirements: requirements,
	        properties: edge.data().properties,
	        sourceVertexId: edge.data().source,
	        targetVertexId: edge.data().target
	      };
	      model.edges.push(newEdge);
	    });
	    start.gw3.models.push(model);
	  }

	  doSend(JSON.stringify(start));
	}

	// Reset the state machine to it's initial state
	function onResetModel() {
	  console.log('onResetModel: ' + currentModelId);
	  defaultUI();

	  issues.innerHTML = 'Ready';

	  for (var modelId in graphs) {
	    if (!graphs.hasOwnProperty(modelId)) {
	      continue;
	    }

	    graphs[modelId].nodes().unselect();
	    graphs[modelId].edges().unselect();
	    graphs[modelId].nodes().data('color', 'LightSteelBlue');
	    graphs[modelId].edges().data('color', 'LightSteelBlue');
	    graphs[modelId].nodes().filterFn(function (ele) {
	      return ele.data('startVertex') === true;
	    }).data('color', 'LightGreen');
	  }
	}

	function onAddModel() {
	  console.log('onAddModel');
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
	      minLen: function minLen() {
	        return 2;
	      }
	    });
	    layout.run();
	  }
	}

	/*
	 ************************************************************************
	 * CREATE SOME CUSTOM EVENTS THAT HANDLES MODEL EXECUTION
	 ************************************************************************
	 */
	var startEvent = new CustomEvent('startEvent', {});
	document.addEventListener('startEvent', function () {
	  console.log('startEvent: ' + currentModelId);

	  // Change some UI elements
	  document.getElementById('runModel').disabled = true;
	  document.getElementById('resetModel').disabled = true;
	  document.getElementById('pausePlayExecution').disabled = false;
	  document.getElementById('stepExecution').disabled = true;

	  var hasNext = {
	    command: 'hasNext'
	  };
	  doSend(JSON.stringify(hasNext));
	});

	var hasNextEvent = new CustomEvent('hasNextEvent', {});
	document.addEventListener('hasNextEvent', function () {
	  console.log('hasNextEvent: pauseExecution: ' + pauseExecution + ', stepExecution: ' + stepExecution + ' : modelId ' + currentModelId);
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

	var getNextEvent = new CustomEvent('getNextEvent', { "modelId": "", "elementId": "", "name": "" });
	document.addEventListener('getNextEvent', function (e) {
	  console.log('getNextEvent: ' + e.id + ': ' + e.name + 'pauseExecution: ' + pauseExecution + ', stepExecution: ' + stepExecution + ' : modelId ' + currentModelId);

	  if (stepExecution) {
	    stepExecution = false;
	    return;
	  }

	  var hasNext = {
	    command: 'hasNext'
	  };
	  setTimeout(function () {
	    doSend(JSON.stringify(hasNext));
	  }, $('#executionSpeedSlider').val());
	});

	function removeModel(modelId) {
	  console.log('Remove model with id: ' + modelId);
	  delete graphs[modelId];
	}

	document.addEventListener('DOMContentLoaded', function () {
	  var tabs = $('#tabs');
	  var modelRequirements = $('#modelRequirements');
	  var modelActions = $('#modelActions');
	  var modelName = $('#modelName');
	  var generator = $('#generator');

	  tabs.delegate('span.ui-icon-close', 'click', function () {
	    var id = $(this).closest('li').remove().attr('aria-controls').substr(2);
	    $('#A-' + id).remove();
	    tabs.tabs('refresh');
	    if (tabs.find('li').length < 1) {
	      tabs.hide();
	    }
	    removeModel(id);
	  });

	  tabs.tabs({
	    activate: function activate(event, ui) {
	      currentModelId = ui.newPanel.attr('id').substr(2);
	      console.log('tabs activate: ' + currentModelId);
	      graphs[currentModelId].resize();
	      modelName.val(graphs[currentModelId].name);
	      generator.val(graphs[currentModelId].generator);
	      modelActions.val(graphs[currentModelId].actions);
	      modelRequirements.val(graphs[currentModelId].requirements);
	    }
	  });

	  // Hide the tab component. It will get visible when the graps are loaded.
	  tabs.hide();

	  $(document).keyup(function (e) {
	    console.log('key up: ' + e.which);
	    delete keys[e.which];
	  });

	  $(document).keydown(function (e) {
	    console.log('key down: ' + e.which);
	    keys[e.which] = true;

	    if (keys[46]) {
	      // Delete key is pressed
	      if (graphs[currentModelId] !== undefined) {
	        graphs[currentModelId].remove(':selected');
	      }
	    }
	  });

	  modelName.on('input', function () {
	    if (graphs[currentModelId]) {
	      graphs[currentModelId].name = modelName.val();
	      var tabs = $('#tabs');
	      var selectedTab = tabs.tabs('option', 'selected');
	      tabs.find('ul li a').eq(selectedTab).text(modelName.val());
	    }
	  });

	  generator.on('input', function () {
	    if (graphs[currentModelId]) {
	      graphs[currentModelId].generator = generator.val();
	    }
	  });

	  modelActions.on('input', function () {
	    if (graphs[currentModelId]) {
	      graphs[currentModelId].actions = modelActions.val();
	    }
	  });

	  modelRequirements.on('input', function () {
	    if (graphs[currentModelId]) {
	      graphs[currentModelId].requirements = modelRequirements.val();
	    }
	  });

	  /**
	   * Place the gw3 files in:
	   * graphwalker-studio/src/main/resources/static/
	   **/
	  //readGraphsFromFile('Login.gw3');
	  //readGraphsFromFile('UC01.gw3');
	  //readGraphsFromFile('petClinic.gw3');
	});

	function createTab(modelId, modelName) {
	  console.log('createTab: ' + modelId + ', ' + modelName);

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
	    ready: function ready() {
	      console.log('Cytoscape is ready...');
	    },
	    style: cytoscape.stylesheet().selector('core').css({
	      //'active-bg-size': 0 // remove the grey circle when panning
	    }).selector('node').css({
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
	    }).selector('edge').css({
	      'content': 'data(name)',
	      'text-wrap': 'wrap',
	      //                        'curve-style' : 'unbundled-bezier',
	      //                        'edge-text-rotation': 'autorotate',
	      'target-arrow-shape': 'triangle',
	      'width': '4',
	      'line-color': 'data(color)',
	      'target-arrow-color': 'data(color)',
	      'background-color': 'data(color)'
	    }).selector(':selected').css({
	      'background-color': 'MediumSlateBlue ',
	      'line-color': 'MediumSlateBlue ',
	      'target-arrow-color': 'MediumSlateBlue '
	    })
	  });

	  var srcNode = null;
	  graph.on('tapstart', 'node', function () {
	    if (keys[69]) {
	      // e key is pressed
	      console.log('tapstart with e key pressed on node: ' + this.id());
	      srcNode = this;
	      graph.autoungrabify(true);
	    }
	  });

	  var dstNode = null;
	  graph.on('tapend', 'node', function () {
	    if (keys[69]) {
	      // e key is pressed
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
	  graph.on('tap', function (event) {
	    var tappedNow = event.cyTarget;
	    if (tappedTimeout && tappedBefore) {
	      clearTimeout(tappedTimeout);
	    }
	    if (tappedBefore === tappedNow) {
	      tappedNow.trigger('doubleTap');
	      tappedBefore = null;
	    } else {
	      tappedTimeout = setTimeout(function () {
	        tappedBefore = null;
	      }, 300);
	      tappedBefore = tappedNow;
	    }

	    if (keys[86]) {
	      // v key is pressed
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

	    $('#label').val('').textinput('disable');
	    $('#sharedStateName').val('').textinput('disable');
	    $('#guard').val('').textinput('disable');
	    $('#actions').val('').textinput('disable');
	    $('#requirements').val('').textinput('disable');
	    $('#checkboxStartElement').attr('checked', false).checkboxradio('refresh').checkboxradio('disable');
	  });

	  graph.on('tap', 'node', function () {
	    currentElement = this;
	    $('#label').textinput('enable').val(this.data().label);
	    $('#sharedStateName').textinput('enable').val(this.data().sharedState);
	    $('#actions').textinput('enable').val(this.data().actions);
	    $('#requirements').textinput('enable').val(this.data().requirements);

	    var checkboxStartElement = $('#checkboxStartElement');
	    checkboxStartElement.checkboxradio('enable');
	    if (graph.startElementId === this.id()) {
	      checkboxStartElement.prop('checked', true).checkboxradio('refresh');
	    }
	  });

	  graph.on('tap', 'edge', function () {
	    currentElement = this;
	    $('#label').textinput('enable').val(this.data().label);
	    $('#guard').textinput('enable').val(this.data().guard);
	    $('#actions').textinput('enable').val(this.data().actions);
	    $('#requirements').textinput('enable').val(this.data().requirements);

	    var checkboxStartElement = $('#checkboxStartElement');
	    checkboxStartElement.checkboxradio('enable');
	    if (graph.startElementId === this.id()) {
	      checkboxStartElement.prop('checked', true).checkboxradio('refresh');
	    }
	  });

	  $('#label').on('input', function () {
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

	  $('#sharedStateName').on('input', function () {
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

	  $('#guard').on('input', function () {
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

	  $('#actions').on('input', function () {
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

	  $('#requirements').on('input', function () {
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

	  $('#checkboxStartElement').change(function () {
	    if ($(this).is(':checked')) {
	      if (currentElement) {
	        graph.startElementId = currentElement.id();
	      }
	    } else {
	      if (currentElement === graph.data().startElementId) {
	        graph.startElementId = undefined;
	      }
	    }
	  });

	  graph.on('doubleTap', function () {
	    $('#nav-panel').panel('open');
	  });
	  var uipanel = $('.ui-panel');
	  uipanel.panel({
	    close: function close() {
	      console.log('Resize the model, because the side panel has been closed');
	      graph.resize();
	    }
	  });
	  uipanel.panel({
	    open: function open() {
	      console.log('Resize the model, because the side panel has been opened');
	      graph.resize();
	    }
	  });

	  graphs[currentModelId] = graph;
	  return graph;
	}

	function readGraphsFromFile(fileName) {
	  console.log('readGraphFromFile - ' + fileName);
	  // Assign handlers immediately after making the request,
	  // and remember the jqxhr object for this request
	  var tabs = $('#tabs');
	  $.getJSON(fileName, function () {
	    console.log('readGraphsFromFile: success');
	  }).done(function (jsonGraphs) {
	    console.log('readGraphsFromFile: done');
	    readGraphFromJSON(jsonGraphs);
	  }).fail(function () {
	    console.log('readGraphsFromFile: error');
	  }).always(function () {
	    console.log('readGraphsFromFile: first complete');
	    tabs.show();
	    for (var modelId in graphs) {
	      if (!graphs.hasOwnProperty(modelId)) {
	        continue;
	      }
	      console.log('readGraphsFromFile: resize graph: ' + modelId);
	      var index = $('#tabs').find('a[href="#A-' + modelId + '"]').parent().index();
	      tabs.tabs('option', 'active', index);
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

	      var vertexActions;
	      var vertexRequirements;
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
	      if (jsonEdge.sourceVertexId === undefined) {
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

	      var edgeActions;
	      var edgeRequirements;
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
	            actions: jsonEdge.actions
	          }),
	          guard: jsonEdge.guard,
	          actions: edgeActions,
	          requirements: edgeRequirements,
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
	  console.log('defaultUI');
	  if (Object.keys(graphs).length > 0 && currentModelId !== undefined) {
	    document.getElementById('runModel').disabled = false;
	    document.getElementById('resetModel').disabled = true;
	    document.getElementById('stepExecution').disabled = true;
	    document.getElementById('pausePlayExecution').innerHTML = 'Pause';
	    document.getElementById('pausePlayExecution').disabled = true;
	    document.getElementById('addModel').disabled = false;
	  } else {
	    document.getElementById('runModel').disabled = false;
	    document.getElementById('resetModel').disabled = false;
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
	  var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
	    var r = (d + Math.random() * 16) % 16 | 0;
	    d = Math.floor(d / 16);
	    return (c === 'x' ? r : r & 0x3 | 0x8).toString(16);
	  });
	  console.log('  UUID: ' + uuid);
	  /*jslint bitwise: false */
	  return uuid;
	}

	function formatElementName(jsonObj) {
	  var str = '';
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
	var wsUri = 'ws://localhost:9999';
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

	function onMessage(event) {
	  console.log('onMessage: ' + event.data);
	  var message = JSON.parse(event.data);

	  switch (message.command) {
	    case 'hasNext':
	      if (message.success) {
	        console.log('Command hasNext: ' + message.hasNext);
	        if (message.hasNext) {
	          hasNextEvent.fullfilled = message.hasNext;
	          document.dispatchEvent(hasNextEvent);
	        } else {
	          defaultUI();
	          document.getElementById('runModel').disabled = true;
	          document.getElementById('resetModel').disabled = false;
	        }
	      } else {
	        defaultUI();
	      }
	      break;
	    case 'getNext':
	      if (message.success) {
	        console.log('Command getNext ok');
	        document.dispatchEvent(getNextEvent, message.modelId, message.elementId, message.name);
	      } else {
	        defaultUI();
	      }
	      break;
	    case 'start':
	      if (message.success) {
	        issues.innerHTML = 'No issues';
	        console.log('Command start ok');
	        document.dispatchEvent(startEvent);
	      } else {
	        defaultUI();
	      }
	      break;
	    case 'issues':
	      issues.innerHTML = message.issues;
	      break;
	    case 'noIssues':
	      issues.innerHTML = 'No issues';
	      break;
	    case 'visitedElement':
	      console.log('Command visitedElement. Will color green on (modelId, elementId): ' + message.modelId + ', ' + message.elementId);
	      issues.innerHTML = 'Steps: ' + message.totalCount + ', Done: ' + (message.stopConditionFulfillment * 100).toFixed(0) + '%, data: ' + JSON.stringify(message.data);

	      currentModelId = message.modelId;
	      graphs[currentModelId].nodes().unselect();
	      graphs[currentModelId].edges().unselect();

	      var tabs = $('#tabs');
	      var index = tabs.find('a[href="#A-' + currentModelId + '"]').parent().index();
	      tabs.tabs('option', 'active', index);

	      graphs[currentModelId].$('#' + message.elementId).data('color', 'lightgreen');
	      graphs[currentModelId].$('#' + message.elementId).select();
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

/***/ },
/* 6 */
/***/ function(module, exports) {

	module.exports = cytoscape;

/***/ }
/******/ ]);
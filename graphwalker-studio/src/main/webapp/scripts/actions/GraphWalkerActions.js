define(['./ConnectionActions', './ModelActions', './VertexActions', 'constants/GraphWalkerConstants', 'constants/StudioConstants'],
function() {
  'use strict';

  var $               = require('jquery');
  var ModelActions    = require('actions/ModelActions');
  var connection      = require('actions/ConnectionActions');
  var Constants       = require('constants/GraphWalkerConstants');
  var RiotControl     = require('app/RiotControl');
  var EdgeActions     = require('actions/EdgeActions');
  var VertexActions   = require('actions/VertexActions');
  var StudioConstants = require('constants/StudioConstants');

  var METHODS = Constants.methods;
  var STATUS  = StudioConstants.status;

  var RUN_DELAY = 500;
  
  var modelId = "";

  var _sendRequestRaw = function(request, callback) {
    connection.send(JSON.stringify(request))
    return true;
  };

  var _sendRequest = function(request, onsuccess, onerror) {
    _sendRequestRaw(request, function(response) {
      if (response.success) {
        onsuccess && onsuccess(response);
      } else {
        onerror && onerror(response);
      }
    });
  };

  var _getNextElement = function(callback) {
    // Prepare server request
    var request = {
      command: METHODS.NEXT,
    };
    _sendRequest(request,
      // On success
      function(response) {
        callback(true, response.body);
        if (response.body.next) {
          setTimeout(function () {
            _getNextElement(callback);
          }, RUN_DELAY);
        }
      },
      // On error
      function(response) {
        callback(false, response.body.error);
      }
    );
  };

  return {
    addModel: function(newModel) {
      // Prepare server request
      var request = {
        command: METHODS.ADDMODEL,
        id: newModel.id
      };
      modelId = newModel.id;
      _sendRequest(request,
        // On success
        function(response) {
          ModelActions.setProps(newModel, {id: response.body.id, status: STATUS.VERIFIED});
        },
        // On error
        function(response) {
          ModelActions.setProps(newModel, {status: STATUS.ERROR});
        }
      );
    },
    addVertex: function(newVertex) {
      // Prepare server request
      var request = {
        command: METHODS.ADDVERTEX,
        modelId: newVertex.modelId,
        vertexId: newVertex.id
      };
      _sendRequest(request,
        // On success
        function(response) {
          VertexActions.setProps(newVertex, {id: response.body.id, status: STATUS.VERIFIED});
        },
        // On error
        function(response) {
          VertexActions.setProps(newVertex, {status: STATUS.ERROR});
        }
      );
    },
    updateVertex: function(vertexId, props) {
      // Prepare server request
      var request = {
        command: METHODS.UPDATEVERTEX,
        modelId: modelId,
        vertex: {
          id: vertexId,
          name: props['name'],
          properties: props
        }
      };
      // Mark as unverified
      VertexActions.setProps(vertexId, {status: STATUS.UNVERIFIED});
      _sendRequest(request,
        // On success
        function(response) {
          VertexActions.setProps(vertexId, {errorMessage: null, status: STATUS.VERIFIED});
        },
        // On error
        function(response) {
          VertexActions.setProps(vertexId, {errorMessage: response.body.error, status: STATUS.ERROR});
        }
      );
    },
    removeVertex: function(vertexId) {
      // Prepare server request
      var request = {
        command: METHODS.REMOVEVERTEX,
        modelId: modelId,
        vertexId: vertexId
      };
      _sendRequest(request,
          // On success
          function(response) {
            VertexActions.setProps(vertexId, {errorMessage: null, status: STATUS.VERIFIED});
          },
          // On error
          function(response) {
            VertexActions.setProps(vertexId, {errorMessage: response.body.error, status: STATUS.ERROR});
          }
      );
    },
    addEdge: function(newEdge) {
      // Prepare server request
      var request = {
        command: METHODS.ADDEDGE,
        modelId: newEdge.modelId,
        edgeId: newEdge.id,
        sourceVertexId: newEdge.sourceVertexId,
        targetVertexId:    newEdge.targetVertexId
      };
      _sendRequest(request,
        // On success
        function(response) {
          EdgeActions.setProps(newEdge, {id: response.body.id, status: STATUS.VERIFIED});
        },
        // On error
        function(response) {
          EdgeActions.setProps(newEdge, {status: STATUS.ERROR});
        }
      );
    },
    updateEdge: function(edgeId, props) {
      // Prepare server request
      var request = {
        command: METHODS.UPDATEEDGE,
        modelId: modelId,
        edge: {
          id: edgeId,
          name: props['name'],
          properties: props
        }
      };
      // Mark as unverified
      EdgeActions.setProps(edgeId, {status: STATUS.UNVERIFIED});
      _sendRequest(request,
        // On success
        function(response) {
          EdgeActions.setProps(edgeId, {errorMessage: null, status: STATUS.VERIFIED});
        },
        // On error
        function(response) {
          EdgeActions.setProps(edgeId, {errorMessage: response.body.error, status: STATUS.ERROR});
        }
      );
    },
    removeEdge: function(edgeId) {
      // Prepare server request
      var request = {
        command: METHODS.REMOVEEDGE,
        modelId: modelId,
        edgeId: edgeId
      };
      // Mark as unverified
      EdgeActions.setProps(edgeId, {status: STATUS.UNVERIFIED});
      _sendRequest(request,
          // On success
          function(response) {
            EdgeActions.setProps(edgeId, {errorMessage: null, status: STATUS.VERIFIED});
          },
          // On error
          function(response) {
            EdgeActions.setProps(edgeId, {errorMessage: response.body.error, status: STATUS.ERROR});
          }
      );
    },
    startRunningModel: function(modelId, callback) {
      // Prepare server request
      var request = {
        command: METHODS.START,
        id: modelId
      };
      var _this = this;
      _sendRequest(request,
        // On success
        function(response) {
          _getNextElement(callback);
        },
        // On error
        function(response) {
          callback(false, response.body.error);
        }
      );
    },
    stopRunningModel: function() {
      _sendRequest({command: METHODS.STOP});
    }
  }
});

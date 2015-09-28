define(['riot', 'jquery', 'store/VertexStore', 'store/EdgeStore', 'store/ConnectionStore',
 'store/ModelStore', 'store/GraphWalkerStore', 'tag/Studio', 'utils/mixins'],
function(riot, $) {

  'use strict';

  var tagUtils = {
    // Toggles boolean variable
    toggle: function(variable) {
      return function() {
        this[variable] = !this[variable];
      };
    }
  };

  // Mount the studio tag and return the mounted instance
  var init = function(opts) {
    var defaults = {
      autoConnect: {
        enabled: false,
        url: ''
      },
      canvas: {
        scrollIncrement: 1,
        minimap: true
      }
    };
    var mergedOpts = $.extend(true, {}, defaults, opts);
    riot.mixin('tagUtils', tagUtils);
    return riot.mount('studio', mergedOpts);
  }

  return {
    init: init
  }
});

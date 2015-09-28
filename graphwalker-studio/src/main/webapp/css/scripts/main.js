window.debug = (window.location.hostname == 'localhost' ? true : false)
window.debug.disableCache = false;

// RequireJS config
var config = {
  baseUrl: 'lib',
  paths: {
    app      : '../scripts',
    actions  : '../scripts/actions',
    constants: '../scripts/constants',
    store    : '../scripts/stores',
    tag      : '../scripts/tags/js',
    tests    : '../scripts/tests',
    utils    : '../scripts/utils'
  },
  map: {
    '*': {
      // 'riot': 'riot_wip', // DEV: use latest Riot version
      'jquery-ui': 'jquery-ui/jquery-ui'
    }
  },
  shim: {
    // Merge all the tag shims into tag/Studio
    'tag/Studio': (function() {
      var tagShims = {
        'tag/Canvas'        : ['riot', 'jquery', 'app/RiotControl', 'actions/VertexActions',
                               'actions/EdgeActions', 'jsplumb', 'constants/StudioConstants',
                               'tests/CanvasTest', 'utils/rubberband'],
        'tag/Vertex'        : ['riot', 'jquery', 'constants/VertexConstants', 'jsplumb', 'jquery-ui',
                               'actions/VertexActions', 'constants/StudioConstants', 'actions/Utils'],
        'tag/Edge'          : ['riot', 'jquery', 'constants/EdgeConstants', 'jsplumb', 'actions/EdgeActions'],
        'tag/Sidebar'       : ['riot'],
        'tag/PropertiesPane': ['riot', 'actions/VertexActions', 'actions/EdgeActions',
                               'constants/StudioConstants', 'utils/mixins'],
        'tag/ConnectionPane': ['riot', 'actions/ConnectionActions'],
        'tag/TabBar'        : ['riot', 'actions/ModelActions'],
        'tag/Studio'        : ['actions/VertexActions', 'constants/StudioConstants', 'panzoom', 'keymaster']
      };
      var compiledShim = [];
      for (var prop in tagShims) {
        if (tagShims[prop].constructor !== Array) continue;
        // Filter out duplicates and concatenate
        compiledShim = compiledShim.concat(tagShims[prop].filter(
          function(el) { return compiledShim.indexOf(el) == -1 }));
      }
      return compiledShim;
    })()
  }
};

// Prevent browser caching
if (window.debug.disableCache) config.urlArgs = "bust=" +  (new Date()).getTime();
requirejs.config(config);

requirejs(['app/StudioApp'], function(StudioApp) {
  if (window.debug) {
    window.StudioApp = StudioApp;
  } else {
    // disable log output if not in debug mode
    console.log = function() {};
  }

  StudioApp.init({
    autoConnect: {
      enabled: false,
      url: 'ws://localhost:9999'
    }
  });
});

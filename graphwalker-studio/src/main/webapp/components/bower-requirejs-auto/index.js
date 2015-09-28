(function (root) {

  var opts = {
    mains: [],
    base: '',
    bowerPath: 'bower_components'
  };

  var basedUrl = function (path) {
    return opts.base + '/' + path;
  };

  root.bowerRequireJsAuto = function () {

    opts = getOpts();

    var url = basedUrl('bower.json');

    get(url, function (xhr) {
      var config = JSON.parse(xhr.responseText);
      handleConfig(config);
    });

  };

  var getOpts = function () {

    var scripts = getScripts();
    var script;
    for (var i = 0, m = scripts.length; i < m; ++i) {
      script = scripts[i];

      var then = script.attributes['data-then'];
      if (then) {
        var thenVal = then.value;
        opts.mains.push(thenVal);
      }

      var base = script.attributes['data-base'];
      if (base) {
        var baseVal = base.value;
        opts.base = baseVal;
      }

      var bowerPath = script.attributes['data-bower-path'];
      if (bowerPath) {
        var bowerPathVal = bowerPath.value;
        opts.bowerPath = bowerPathVal;
      }

    }

    return opts;
  };


  var loadMain = function () {

    var mains = opts.mains;

    if (mains) {
      for (var i = 0, m = mains.length; i < m; ++i) {
        var main = mains[i];

        require([main]);
      }
    }
  };

  var getScripts = function () {
    return document.getElementsByTagName('script');
  };

  var get = function (url, done, opts) {
    opts = opts || {};

    var xhr = new XMLHttpRequest();

    xhr.open('GET', url, false);

    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4 && xhr.status < 400) {
        done(xhr);
      }
    };

    xhr.send(null);
  };

  var handleConfig = function (config) {
    var deps = config.dependencies;
    if (deps) {
      var i = 0;
      var count = 0;
      var depName;
      for (depName in deps) {
        ++count;
      }
      var eachDepCallback = function () {
        ++i;
        if (i == count) {
          loadMain();
        }
      };
      var dep;
      for (depName in deps) {
        dep = deps[depName];
        eachDep(dep, depName, eachDepCallback);
      }
    }
  };

  var isArray = function (val) {
    return Object.prototype.toString.call(val) === '[object Array]';
  };

  var toArray = function (main) {
    if (isArray(main)) {
      return main;
    }
    else {
      return [main];
    }
  };

  var eachDep = function (dep, depName, callback) {
    var dir = basedUrl(opts.bowerPath + '/' + depName);
    get(dir + '/.bower.json', function (xhr) {
      var config = JSON.parse(xhr.responseText);

      var main = config.main;

      if (! main) {
        main = 'index.js';
      }

      var mainItems = toArray(main);

      var mainItem;
      for (var i = 0, m = mainItems.length; i < m; ++i) {
        mainItem = mainItems[i];

        if (! mainItem.match(/\.js$/)) {
          continue;
        }

        mainItem = mainItem.replace(/\.js$/, '');

        var path = dir + '/' + mainItem;

        var paths = {};
        paths[depName] = path;

        var requireConfig = {
          paths: paths
        };

        require.config(requireConfig);

        callback();
      }

    });
  };


  root.bowerRequireJsAuto();

})(this);

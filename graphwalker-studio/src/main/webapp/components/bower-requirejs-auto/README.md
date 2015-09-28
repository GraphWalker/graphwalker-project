bower-requirejs-auto
====================

Automatically configures RequireJS paths for Bower components in the browser at runtime. Just get started as fast as possible with Bower and RequireJS!

Recommended for development use only.

Simpler than hard-coding all your Bower component paths into HTML, and faster than tools like  [yeoman/bower-requirejs](https://github.com/yeoman/bower-requirejs) (or [yeoman/grunt-bower-requirejs](https://github.com/yeoman/grunt-bower-requirejs)) which require their own configuration and repeated execution.

## Install

```sh
bower install --save-dev bower-requirejs-auto
```

## Use

Add `bower-requirejs-auto/index.js` to your page, and specify options via the following attributes on its `<script>` tag:
* `data-then`: (*required*) a main module to load after automatic configuration is complete, like RequireJS's `data-main`.
* `data-base`: (*optional, default*: `''` ) a relative path to your "base" directory containing `bower.json` and `bower_components`.
* `data-bower-path`: (*optional, default*: `'bower_components'`) the path of your `bower_components` directory, relative to your "base" directory.

See [example](example). In summary:

```html
<!-- index.html -->
<script src="bower_components/requirejs/require.js"></script>
<script src="bower_components/bower-requirejs-auto/index.js" data-then="main"></script>
```

```js
// main.js
require([ /* ... */ ], function ( /* ... */ ) {
  // ...
});
```

var path = require('path');

module.exports = {
  entry: "./index.js",
  output: {
    library: "graphwalker",
    libraryTarget: 'var',
    path: __dirname + '/../resources/static',
    filename: "bundle.js"
  },
  externals: {
    "jquery": "jQuery",
    "cytoscape": "cytoscape"
  },
  module: {
    preLoaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: "jshint-loader"
      }
    ],
    loaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: "babel-loader"
      }, {
        test: /\.css$/,
        loader: "style!css"
      }
    ]
  },
  jshint: {
    camelcase: true,
    emitErrors: false,
    failOnHint: false
  }
};

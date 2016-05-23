var path = require('path');

module.exports = {
  entry: './index.js',
  output: {
    library: 'graphwalker',
    libraryTarget: 'var',
    path: __dirname + '/../resources/static',
    filename: 'bundle.js'
  },
  externals: {
    'jquery': 'jQuery',
    'cytoscape': 'cytoscape',
    'react': 'React',
    'react-dom': 'ReactDOM'
  },
  module: {
    preLoaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'eslint-loader'
      }
    ],
    loaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel',
        query:
        {
          presets:['react']
        }
      }, {
        test: /\.css$/,
        loader: 'style!css'
      }
    ]
  },
  eslint: {
    configFile: '.eslintrc'
  }
};

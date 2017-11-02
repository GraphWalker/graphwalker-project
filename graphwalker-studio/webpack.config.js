var webpack = require('webpack');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  entry: {
    pages: __dirname + '/src/main/js/index.js',
    vendors: ['react', 'react-dom', 'antd']
  },
  output: {
    path: __dirname,
    filename: './src/main/resources/static/bundle.js'
  },
  module: {
    loaders: [
      { test: /\.css$/, loader: ExtractTextPlugin.extract({fallback:'style-loader', use:['css-loader']}) },
      { test: /\.less$/, loader: ExtractTextPlugin.extract('css-loader!less-loader') },
      { test: /\.js[x]?$/, exclude: /node_modules/, loader: 'babel-loader' },
      { test: /\.(png|jpg)$/, loader: 'url-loader?limit=8192&name=img/[name].[ext]' },
      { test: /\.(woff|woff2|eot|ttf|svg)(\?.*$|$)/, loader: 'url-loader' }
    ]
  },
  plugins: [
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendors',
      filename: './src/main/resources/static/vendors.js'
    }),
    new ExtractTextPlugin({
      filename: './src/main/resources/static/bundle.css',
      allChunks: true
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      }
    })
  ]
};

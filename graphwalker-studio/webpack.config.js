const path = require('path');

module.exports = {
  mode: 'development',
  entry: path.resolve(__dirname, 'src/main/js/index.js'),
  output: {
    filename: 'studio.js',
    path: path.resolve(__dirname, 'target/classes/static')
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: ['babel-loader']
      }, {
        test: /^(?!.*\.inline\.svg$).*\.svg$/,
        loader: 'svg-url-loader',
        options: {
          limit: 10000,
          name: '[path][name].[ext]',
        }
      }, {
        test: /\.inline.svg$/,
        loader: 'react-svg-loader',
      }, {
        test: /\.(png|woff|woff2|eot|ttf)$/,
        use: ['url-loader?limit=100000']
      }, {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      }
    ]
  },
  devServer: {
    contentBase: path.join(__dirname, 'src/main/resources/static'),
    compress: true,
    port: 9000
  }
};

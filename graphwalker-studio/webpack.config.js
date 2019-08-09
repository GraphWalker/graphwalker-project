const path = require('path');

module.exports = {
  mode: 'production',
  entry: path.resolve(__dirname, 'src/main/js/index.js'),
  output: {
    filename: 'studio.js',
    path: path.resolve(__dirname, 'src/main/resources/static')
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: ['babel-loader']
      }, {
        test: /\.(png|woff|woff2|eot|ttf|svg)$/,
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

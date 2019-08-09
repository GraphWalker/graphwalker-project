const path = require('path');

module.exports = {
  mode: 'production',
  entry: path.resolve(__dirname, 'src/main/js/index.js'),
  output: {
    filename: 'studio.js',
    path: path.resolve(__dirname, 'src/main/resources/static')
  },
  devServer: {
    contentBase: path.join(__dirname, 'src/main/resources/static'),
    compress: true,
    port: 9000
  }
};

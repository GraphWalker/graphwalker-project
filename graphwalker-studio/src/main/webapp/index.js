require('./css/style.css');
import React from 'react';
import ReactDOM from 'react-dom';

import Editor from './scripts/Editor';

window.onload = function() {
  ReactDOM.render(React.createElement(Editor, null), document.getElementById('loading-mask'));
  document.getElementById('loading-mask').style.display='none';
};

module.exports = {
  studio: require('./scripts/script.js'),
  editor: require('./scripts/Editor.js')
};

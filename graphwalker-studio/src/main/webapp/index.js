require('./css/style.css');

window.onload = function() {
  document.getElementById('loading-mask').style.display='none';
};

module.exports = {
  studio: require('./scripts/script.js')
};

require(['lodash'], function (_) {

  var msg = _.template('<%=msg%>', {msg: 'Yay!'});
  alert(msg);

});

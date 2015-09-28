define(function() {
  'use strict';

  return {
    // Creates an enum object => {ENUM: 'ENUM'}
    Enum: function(constantsList) {
      var constants = {};
      for (var i = 0; i < constantsList.length; i++) {
        var constant = constantsList[i];
        constants[constant] = constant;
      }
      return Object.freeze(constants);
    }

  };
});

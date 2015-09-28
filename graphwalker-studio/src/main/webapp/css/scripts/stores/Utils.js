define(function() {
  'use strict';

  return {
    // Get element either by object or by ID
    getElement: function(collection, query) {
      if (query !== null && typeof query === 'object') query = query.id;    // Extract ID if object
      return collection.filter(function(el) { return el.id === query })[0]; // Search by ID
    }
  }
});

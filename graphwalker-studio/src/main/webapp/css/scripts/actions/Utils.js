define(function() {
  'use strict';
  
  return {
    /**
    * Captures a series of actions and dispatches them all at a pre-set point in the future.
    * Remember to bind functions before calling bufferedAction if they rely on value of `this`.
    */
    bufferedAction: (function() {
      var _bufferedActionCache = [];
      var dispatch = function() {
        for (var i = 0, j = this.actions.length; i < j; i++) {
          this.actions[i]();
        }
      };
      return function(action, uniqueId, bufferUntil) {
        // Get buffered instance or create new one
        var bufferedAction = _bufferedActionCache.filter(function(el) { return el.id === uniqueId })[0];
        if (!bufferedAction) {
          bufferedAction = {
            id           : uniqueId,
            actions      : [],
            bufferUntil  : bufferUntil,
            bufferCounter: 0,
            dispatch     : dispatch
          };
          _bufferedActionCache.push(bufferedAction);
        }

        // Push the received action onto the action stack
        bufferedAction.actions.push(action);

        // Increment counter and check if we are ready to dispatch
        if (++bufferedAction.bufferCounter === bufferedAction.bufferUntil) {
          bufferedAction.dispatch();                   // Dispatch and
          _bufferedActionCache.remove(bufferedAction); // remove instance
        }
      }
    })(),
    /**
     * Runs an action if no new actions have come in during a time interval.
     */
    timeBufferedAction: (function() {
      var _bufferedActionCache = [];
      return function(action, uniqueId, bufferUntil) {
        // Get buffered instance or create new one
        var bufferedAction = _bufferedActionCache.filter(function(el) { return el.id === uniqueId })[0];
        if (!bufferedAction) {
          bufferedAction = {
            id           : uniqueId,
            action       : action
          };
          _bufferedActionCache.push(bufferedAction);
        }

        // Update timer
        bufferedAction.timer = bufferUntil;

        // Stop previous timeout
        if (bufferedAction.timeout) clearTimeout(bufferedAction.timeout);

        // Set timeout
        bufferedAction.timeout = setTimeout(bufferedAction.action, bufferedAction.timer);

      }
    })()
  }
});

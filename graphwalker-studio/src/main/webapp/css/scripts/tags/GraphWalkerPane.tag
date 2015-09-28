<graphwalker-pane>
  <ul>
    <li if={ errorMessage }>
      <div class="bg-warning"><span class="octicon octicon-alert"></span> { errorMessage }</div>
    </li>
    <li if={ successMessage }>
      <div class="bg-success"><span class="octicon octicon-check"></span> { successMessage }</div>
    </li>
    <li><b>Connection status:</b><br> { opts.connected ? 'Connected' : 'Disconnected' }</li>
    <li>
      <button show={ opts.connected && opts.model.id && !running } onclick={ startRunning } class="green">
        <span class="octicon octicon-rocket"></span>
        Run model
      </button>
      <button show={ opts.connected && running } onclick={ stopRunning } class="red">
        <span class="octicon octicon-primitive-square"></span>
        Stop
      </button>
    </li>
  </ul>

  var Actions         = require('actions/GraphWalkerActions');
  var EdgeActions     = require('actions/EdgeActions');
  var VertexActions   = require('actions/VertexActions');
  var StudioConstants = require('constants/StudioConstants');

  var self = this;

  self.running = false;

  self.on('mount', function() {
    var headerElement = $(self.root).parents('sidebar-pane').find('h4');
    self.statusIcon = $('<span>')
      .addClass('octicon octicon-primitive-dot')
      .css('transition', 'color 400ms ease-out 100ms')
      .css('color', '#cd2828')
      .appendTo(headerElement);
  });

  self.on('updated', function() {
    if (self.isMounted) {
      self.statusIcon.css({
        'color': opts.connected ? '#15da52' : '#cd2828'
      });
    }
  });

  startRunning() {
    self.running = true;
    delete self.errorMessage;
    delete self.successMessage;
    var modelId = opts.model.id;
    Actions.startRunningModel(modelId, function(success, response) {
      if (!success) {
        self.stopRunning();
        self.errorMessage = response;
        self.update();
      } else {
        if (response.next) {
          switch (response.type) {
            case StudioConstants.types.T_VERTEX:
              VertexActions.get(response.next, opts.selection.update);
              break;
            case StudioConstants.types.T_EDGE:
              EdgeActions.get(response.next, opts.selection.update);
              break;
          }
        } else {
          self.stopRunning();
          self.successMessage = response.message;
          self.update();
        }
      }
    });
  }

  stopRunning() {
    self.running = false;
    Actions.stopRunningModel();
  }

</graphwalker-pane>

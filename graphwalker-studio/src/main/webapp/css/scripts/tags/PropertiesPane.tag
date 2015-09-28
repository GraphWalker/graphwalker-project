<properties-pane>
  <ul>
    <li if={!isMultipleSelection && element.errorMessage}>
      <div class="bg-warning"><span class="octicon octicon-alert"></span> { element.errorMessage }</div>
    </li>
    <li if={!isMultipleSelection}><b>Name:</b><br>
      <editable type='text' callback={ change('name') }>{ parent.element.name || 'unnamed' }</editable>
    </li>
    <li if={!isMultipleSelection}><b>ID:</b><br>{ element.id }</li>
    <li if={isMultipleSelection}>
      Selected { opts.selection.length }
       { isDifferentTypes ? 'elements' : element.type.pluralize(isMultipleSelection) }
    </li>
    <li>
      <button onclick={ removeElement } class="red">
        <span class="octicon octicon-trashcan"></span>
        Remove { isDifferentTypes ? 'elements' : element.type.pluralize(isMultipleSelection) }
      </button>
      <button show={ !isMultipleSelection && element == opts.model } onclick={ saveModel }>
        <span class="octicon octicon-desktop-download"></span>
        Save model
      </button>
    </li>
  </ul>

  var VertexActions    = require('actions/VertexActions');
  var EdgeActions      = require('actions/EdgeActions');
  var ModelActions     = require('actions/ModelActions');
  var StudioConstants  = require('constants/StudioConstants');

  var self = this;

  self.on('update', function() {
    self.element = opts.selection[0] || opts.model || {};
    self.isMultipleSelection = opts.selection.length > 1;
    self.isDifferentTypes = !self.isMultipleSelection ? false :
      !opts.selection.mapBy('type').every(function(el, i, array) {
        return i > 0 ? el === array[i-1] : true;
      });
  });

  saveModel() {
    // TODO Use promises instead of counter
    var counter = 0;
    var vertices, edges;
    VertexActions.getAll(function(resp) {
      vertices = resp;
      if (++counter == 2) save();
    });
    EdgeActions.getAll(function(resp) {
      edges = resp.map(function(edge) {
        delete edge._jsp_connection;
        return edge;
      });
      if (++counter == 2) save();
    });
    function save() {
      var data = {
        model: opts.model,
        vertices: vertices,
        edges: edges
      };
      var blob = new Blob([JSON.stringify(data)], {type: 'octet/stream'});
      var url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
      window.focus();
      window.URL.revokeObjectURL(url);
    }
  }

  change(prop) {
    return function(newValue) {
      var props = {};
      props[prop] = newValue;
      switch (self.element.type) {
        case StudioConstants.types.T_VERTEX:
          VertexActions.setProps(self.element.id, props);
          break;
        case StudioConstants.types.T_EDGE:
          EdgeActions.setProps(self.element.id, props);
          break;
        case StudioConstants.types.T_MODEL:
          ModelActions.setProps(self.element.id, props);
          break;
      }
    };
  }

  removeElement() {
    if (self.isDifferentTypes) {
      // Selection is going to change after each remove action, make a copy of it.
      var _selection = opts.selection.slice();

      EdgeActions.remove(_selection.filter(function(el) {
        return el.type === StudioConstants.types.T_EDGE;
      }).mapBy('id'));

      VertexActions.remove(_selection.filter(function(el) {
        return el.type === StudioConstants.types.T_VERTEX;
      }).mapBy('id'));
    } else {
      switch (self.element.type) {
        case StudioConstants.types.T_VERTEX:
          VertexActions.remove(opts.selection.mapBy('id'));
          break;
        case StudioConstants.types.T_EDGE:
          EdgeActions.remove(opts.selection.mapBy('id'));
          break;
        case StudioConstants.types.T_MODEL:
          ModelActions.remove(self.element.id);
          break;
      }
    }
  }
</properties-pane>

<editable onclick={ click }>
  <span show={ !editing }>
    <yield/>
  </span>

  <style scoped>
    span {
      cursor: pointer;
    }
  </style>

  var ENTER_KEY = 13;
  var ESC_KEY = 27;

  var self = this;

  self.editing = false;

  self.on('mount', function() {
    // Create editable element
    var editControl = (function() {
      switch (self.opts.type) {
        case 'text':
          return $('<input>').attr({ type: 'text', name: 'editable', autofocus: true});
      }
    })();

    editControl.on('change blur keydown', function(e) {
      switch (e.type) {
        case 'keydown':
          if (e.keyCode === ENTER_KEY || e.keyCode === ESC_KEY) this.blur();
          break;
        case 'change':
          // Call callback with new value
          self.opts.callback(e.target.value);
        case 'blur':
          self.editing = false;
          self.update();
      }
    });

    $(self.root).append(editControl);
    editControl.hide();

    self.editControl = editControl;
  });

  self.on('updated', function() {
    if (self.isMounted) {
      self.editControl.toggle(self.editing);
      if (self.editing) self.editControl.select();
    }
  });

  click(e) {
    // HACK: otherwise we get TypeError due to bug in riot/#1094
    e.preventUpdate = true;

    // Don't do anything when already editing
    if (self.editing || opts.off) return;

    // Otherwise switch to edit mode
    self.editing = true;
    self.editControl.val(self.root.innerText);

    self.update();
  }
</editable>

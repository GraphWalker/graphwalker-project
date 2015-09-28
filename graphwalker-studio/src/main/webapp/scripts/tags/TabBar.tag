<studio-tabs>
  <ul>
    <li each={ opts.tabs.getObjects() }>
      <div onclick={ selectTab } class="{ selected: parent.opts.model.id === id}">
        { name }
        <span onclick={ parent.closeTab } class="octicon octicon-x"></span>
      </div>
    </li>

    <li><div id="add">&nbsp;<span onclick={ openTab } class="octicon octicon-plus"></span></div></li>
  </ul>

  <style scoped>
    ul {
      background-color: #f0f0f0;
      list-style: none;
      padding: 0;
      margin: 0;
    }
    li {
      display: inline-block;
    }
    div {
      height: 20px;
      width: 150px;
      border: 1px solid black;
      border-bottom: 0;
      padding: 5px;
      text-align: left;
      vertical-align: middle;
      line-height: 20px;
      background-color: #40697e;
      cursor: default;
      border-radius: 6px 6px 0 0;
    }
    span {
      float: right;
      color: rgba(0, 0, 0, 0.18)
    }
    span:hover {
      color: black;
      cursor: default;
      background-color: rgba(0, 0, 0, 0.21);
    }
    .octicon {
      border-radius: 50%;
      width: 15px;
      height: 15px;
      text-align: center;
      padding: 3px;
    }
    div#add {
      border: 0px;
      width: 100%;
      margin-left: -5px;
      border-top-left-radius: 0;
    }
    div.selected {
      border-top: 4px solid #5b8590;
      background-color: #f0f0f0;
    }
  </style>

  var ModelActions = require('actions/ModelActions');

  var self = this;

  ModelActions.addChangeListener(function(models) {
    // Close tabs belonging to recently removed models
    opts.tabs.forEach(function(modelId) {
      if (!models.mapBy('id').contains(modelId)) self.opts.tabs.close(modelId);
    });
    self.update();
  });

  openTab(e) {
    self.opts.model.new();
    e.preventUpdate = true; // Update is called indirectly above
  };

  closeTab(e) {
    self.opts.tabs.close(e.item.id);
    e.preventUpdate = true; // Update is called indirectly above

    // Don't trigger selectTab
    if (e) e.stopPropagation();
  }

  selectTab(e) {
    self.opts.model.set(e.item.id);
    e.preventUpdate = true; // Update is called indirectly above
  }
</studio-tabs>

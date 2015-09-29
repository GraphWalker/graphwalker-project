<studio-tabs class="studio-tabs">
    <ul>
        <li each={ opts.tabs.getObjects() }>
            <div onclick={ selectTab } class="{ selected: parent.opts.model.id === id}">
                <span class="studio-label">{ name }</span>
                <span class="studio-icon octicon octicon-x" onclick={ parent.closeTab }></span>
            </div>
        </li>
        <li>
            <div id="add"><span onclick={ openTab } class="octicon octicon-plus"></span></div>
        </li>
    </ul>

    <script>
        var ModelActions = require('actions/ModelActions');

        var self = this;

        ModelActions.addChangeListener(function (models) {
            // Close tabs belonging to recently removed models
            opts.tabs.forEach(function (modelId) {
                if (!models.mapBy('id').contains(modelId)) self.opts.tabs.close(modelId);
            });
            self.update();
        });

        self.openTab = function(e) {
            self.opts.model.new();
            e.preventUpdate = true; // Update is called indirectly above
        };

        self.closeTab = function(e) {
            self.opts.tabs.close(e.item.id);
            e.preventUpdate = true; // Update is called indirectly above

            // Don't trigger selectTab
            if (e) e.stopPropagation();
        };

        self.selectTab = function(e) {
            self.opts.model.set(e.item.id);
            e.preventUpdate = true; // Update is called indirectly above
        };
    </script>
</studio-tabs>

<sidebar-pane class="sidebar-pane">
    <h4 onclick={ toggle('expanded') }>
    <span if={ opts.icon } class="icon octicon octicon-{ opts.icon }"></span>
    { opts.heading }
    <span class="minimize octicon octicon-diff-{ expanded ? 'removed' : 'added'}"></span>
    </h4>
    <div class="pane-body" show={ expanded }>
        <yield/>
    </div>

    <script>
        var self = this;

        self.mixin('tagUtils');

        self.expanded = true;

        self.one('update', function () {
            self.expanded = !self.opts.collapsed;
        });
    </script>
</sidebar-pane>

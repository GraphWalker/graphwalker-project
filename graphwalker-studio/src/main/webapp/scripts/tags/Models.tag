<models-pane>
    <ul>
        <li if={ !opts.models.length }>
            <button onclick={ opts.model.new } class="green">
                <span class="octicon octicon-plus"></span>
                New model
            </button>
            <button onclick={ openFileDialog }>
                <span class="octicon octicon-cloud-upload"></span>
                Load model
            </button>
            <input type="file" name="fileUpload" show={ false } onchange={ loadModel }/>
        </li>
        <li if={ opts.models.length }>
            <input type="text" name="searchInput" placeholder="Search" onkeyup={ search }>
            <button onclick={ clearSearch }>Clear</button>
        </li>
        <li if={ opts.models.length }>
            <a href="" onclick={ expandAll }>Expand all</a>
            <a href="" onclick={ hideAll }>Collapse all</a>
        </li>
    </ul>
    <ul class="models">
        <li each={ model in opts.models } class="{ active: parent.opts.model.id === model.id}">
      <span onclick={ toggleExpand }
            class="octicon octicon-chevron-{ !parent.collapsed.contains(model.id) ? 'down' : 'right' }"></span>
            <a class="{ active: parent.opts.model.id === model.id}" onclick={ openModel }>
                { model.name }
            </a>
            <ul if={ !parent.collapsed.contains(model.id) }>
                <li each={ filterByModel(parent.opts.vertices, model).filter(searchFilter) }>
                    <a class="vertex { selected: parent.parent.opts.selection.mapBy('id').contains(id) }"
                       onclick={ select }>{ name }</a>
                </li>
                <li each={ filterByModel(parent.opts.edges, model).filter(searchFilter) }>
                    <a class="edge { selected: parent.parent.opts.selection.mapBy('id').contains(id) }"
                       onclick={ select }>{ name }</a>
                </li>
            </ul>
        </li>
    </ul>

    <style scoped>
        a {
            color: inherit;
            cursor: pointer;
        }

        a.active {
            background-color: rgba(55, 157, 200, 0.4);
        }

        a.selected {
            background-color: rgba(55, 157, 200, 0.75);
        }

        li.active {
            background-color: rgba(91, 133, 144, 0.2);
        }

        ul.models {
            list-style: none;
            background-color: #f0f0f0;
            color: black;
            overflow-y: auto;
            max-height: 350px;
            border-radius: 2px;
        }

        ul.models span.octicon {
            padding: 5px 0px 0px 10px;
        }

        input[name='searchInput'] {
            width: 238px;
        }
    </style>

    <script>
        var VertexActions = require('actions/VertexActions');
        var EdgeActions = require('actions/EdgeActions');
        var ModelActions = require('actions/ModelActions');
        var StudioConstants = require('constants/StudioConstants');

        var self = this;

        // State
        self.collapsed = [];
        self.searchQuery = '';

        self.filterByModel = function(elements, model) {
            return elements.filter(function (el) {
                return el.modelId === model.id
            });
        };

        self.toggleExpand =function(e) {
            var modelId = e.item.model.id;
            self.collapsed.toggle(modelId);
        };

        self.hideAll = function() {
            self.collapsed = self.opts.models.mapBy('id');
        };

        self.expandAll = function() {
            self.collapsed = [];
        };

        self.select = function(e) {
            e.preventUpdate = true; // Update is called by selection.update
            var element = e.item;
            self.opts.model.set(element.modelId);
            opts.selection.update(element);
        };

        self.openModel = function(e) {
            self.opts.model.set(e.item.model.id);
        };

        self.searchFilter = function(el) {
            return !self.searchQuery ? true : new RegExp(self.searchQuery).test(el.name);
        };

        self.search = function() {
            if (!self.collapsedBeforeSearch) self.collapsedBeforeSearch = self.collapsed;
            self.searchQuery = self.searchInput.value;
            self.expandAll();
        };

        self.clearSearch = function() {
            self.searchQuery = self.searchInput.value = '';
            if (self.collapsedBeforeSearch) {
                self.collapsed = self.collapsedBeforeSearch;
                delete self.collapsedBeforeSearch;
            }
        };

        self.openFileDialog =function() {
            self.fileUpload.click();
        };

        self.loadModel = function() {
            var fileReader = new FileReader();
            fileReader.onload = function () {
                var dataObject = JSON.parse(fileReader.result);
                opts.model.load(dataObject.model);
                dataObject.vertices.forEach(VertexActions.add);
                dataObject.edges.forEach(EdgeActions.add);
            };
            fileReader.readAsText(self.fileUpload.files[0]);
        };
    </script>
</models-pane>

import React, { Component } from 'react';
import { findDOMNode } from "react-dom";
import { connect } from "react-redux";
import {
  createElement,
  deleteElement,
  selectElement,
  setStartElement,
  updateElementPosition,
  saveEditorState,
  toggleBreakpoint
} from "../../../redux/actions";
import {Classes, ContextMenu, Divider, Menu, MenuDivider, MenuItem, ResizeSensor} from "@blueprintjs/core";
import { debounce } from "debounce";
import uuid from "uuid/v1"
import Cytoscape from "cytoscape";
import coseBilkent from 'cytoscape-cose-bilkent';
import stylesheet from "./editor-stylesheet";
import "./style.css";

Cytoscape.use( coseBilkent );

class EditorComponent extends Component {

  updateColors = ({ elements }) => {
    const { model: { id }} = this.props;
    const visited = this.props.execution.visited[id];
    elements.forEach(element => {
      const key = `${id},${element.data.id}`;
      element.data.name = element.data.name || "";
      if (this.props.execution.breakpoints[key]) {
        element.data.color = 'Red';
      } else if (visited && visited[element.data.id]) {
        element.data.color = 'LightGreen';
      } else {
        if (this.props.model.startElementId === element.data.id) {
          element.data.color = 'LightGreen';
        } else if (element.data.sharedState) {
          element.data.color = 'LightSalmon';
        } else {
          element.data.color = 'LightSteelBlue';
        }
      }
    });
    return { elements };
  }

  asJson = () => {
    const elements = [];
    const { startElementId } = this.props.model;
    this.props.model.vertices.forEach(({id, name, sharedState, actions, requirements, properties: {x = 0, y = 0}}) => elements.push({
      group: 'nodes',
      data: {
        id,
        name,
        color: id === startElementId ? 'LightGreen' : 'LightSteelBlue',
        sharedState,
        actions,
        requirements
      },
      position: {x, y}
    }));

    this.props.model.edges.filter(({sourceVertexId: source}) => source == null).forEach(() => elements.push({
      group: 'nodes',
      data: {id: 'Start', name: 'Start', color: 'LightGreen'},
      position: {x: 0, y: 0}
    }));

    this.props.model.edges.forEach(({id, name, guard, weight, actions, sourceVertexId: source = 'Start', targetVertexId: target}) => elements.push({
      group: 'edges',
      data: {
        id,
        name,
        source: source == null ? 'Start' : source,
        target,
        color: source == null ? 'LightGreen' : 'LightSteelBlue',
        guard,
        weight,
        actions
      }
    }));
    return this.updateColors({ elements });
  }

  doLayout = (config) => {
    this.editor.layout(config).run()
    this.editor.nodes().forEach(vertex => {
      this.props.updateElementPosition(vertex.id(), vertex.position());
    })
  }

  updateSelected = () => {
    const { selectedElementId } = this.props;
    if (selectedElementId) {
      this.editor.elements().unselect()
      this.editor.elements('#'+selectedElementId).select()
    }
  }

  componentDidMount() {
    const container = findDOMNode(this);
    this.editor = new Cytoscape({ container });
    this.editor.style().clear().fromJson(stylesheet).update()
    this.editor.json(this.updateColors(this.asJson()));
    this.updateSelected();
    this.addEventHandlers();
  }

  componentWillUnmount() {
    this.editor.destroy();
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevProps.selectedModelIndex !== this.props.selectedModelIndex) {
      this.props.saveEditorState(prevProps.selectedModelIndex, this.editor.json())
    }
    this.editor.json(Object.assign({}, this.props.editor, this.updateColors(this.asJson())));
    this.updateSelected();
  }

  addEventHandlers() {

    document.addEventListener('keydown', event => {
      this.keyCode = event.which;
    });

    document.addEventListener('keyup', event => {
      this.keyCode = null;
    });

    this.editor.on('tap', event => {
      if (event.target === this.editor) {
        if (this.editor.elements(':selected').length === 0) {
          if (this.keyCode === 86) { // v key is pressed
            const { position: { x, y }} = event;
            this.props.createElement({
              id: uuid(),
              name: 'v_NewVertex',
              properties: { x, y }
            });
          }
        } else {
          this.props.selectElement(null);
        }
      } else {
        this.props.selectElement(event.target.id());
      }
    });

    this.editor.on('tapstart', 'node', event => {
      if (this.keyCode === 69) {
        this.editor.autoungrabify(true);
        this.source = event.target;
      }
    });

    this.editor.on('dragfree', 'node', event => {
      const { target } = event;
      this.props.updateElementPosition(target.id(), target.position());
    });

    this.editor.on('tapend', event => {
      if (this.keyCode === 69) {
        this.editor.autoungrabify(false);
        if (this.editor != event.target && event.target.isNode()) {
          this.props.createElement({
            id: uuid(),
            sourceVertexId: this.source.id(),
            targetVertexId: event.target.id(),
            name: 'e_NewEdge'
          });
        }
      }
    });

    this.editor.on('cxttap', 'node, edge', event => {
      const { clientX, clientY } = event.originalEvent;
      const { model: { id }} = this.props;
      const key = `${id},${event.target.id()}`;
      const hasBreakpoint = this.props.execution.breakpoints[key] != null;
      ContextMenu.show(
        <Menu>
          { this.props.model.startElementId === event.target.id() ?
            <MenuItem icon="selection" text="Remove start point" onClick={() => this.props.setStartElement(event.target.id())} />
            :
            <MenuItem icon="circle" text="Add start point" onClick={() => this.props.setStartElement(event.target.id())} />
          }
          <MenuItem icon="cross" text="Delete" onClick={() => this.props.deleteElement(event.target.remove().map(element => element.id()))}/>
          <Divider/>
          <MenuItem icon="full-circle" text="Breakpoint...">
            <MenuItem disabled={hasBreakpoint} icon="new-object" text="Add breakpoint" onClick={() => this.props.toggleBreakpoint(id, event.target.id())} />
            <MenuItem disabled={!hasBreakpoint} icon="graph-remove" text="Remove breakpoint" onClick={() => this.props.toggleBreakpoint(id, event.target.id())} />
          </MenuItem>
        </Menu>, { left: clientX, top: clientY });
    })

    this.editor.on('cxttap', event => {
      if (event.target === this.editor) {
        const { clientX, clientY } = event.originalEvent;
        ContextMenu.show(
          <Menu>
            <MenuItem icon="select" text="Select all" onClick={() => this.editor.elements().select()} />
            <MenuItem disabled={this.editor.elements(':selected').length === 0} icon="cross" text="Delete selected"
                      onClick={() => this.props.deleteElement(this.editor.elements(':selected').remove().map(element => element.id()))}/>
            <Divider/>
            <MenuItem icon="layout" text="Layout...">
              <MenuItem icon="layout-auto" text="Auto" onClick={() => this.doLayout({ name: 'cose-bilkent', nodeDimensionsIncludeLabels: true, idealEdgeLength: 200 })} />
              <MenuItem icon="layout-circle" text="Circle" onClick={() => this.doLayout({ name: 'circle' })} />
              <MenuItem icon="layout-grid" text="Grid" onClick={() => this.doLayout({ name: 'grid' })} />
            </MenuItem>
          </Menu>, { left: clientX, top: clientY });
      }
    });
  }

  handleResize = debounce(() => {
    this.editor.resize();
  }, 200)

  render() {
    return (
      <ResizeSensor onResize={this.handleResize}>
        <div className="editor-component"/>
      </ResizeSensor>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex, selectedElementId }, execution }) => {
  return {
    model: models[selectedModelIndex],
    editor: models[selectedModelIndex].editor,
    updated: models.updated,
    execution,
    selectedElementId,
    selectedModelIndex,
  }
};

export default connect(mapStateToProps, { createElement, deleteElement, selectElement, setStartElement, updateElementPosition, saveEditorState, toggleBreakpoint })(EditorComponent);

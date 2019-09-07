import React, { Component } from 'react';
import { findDOMNode } from "react-dom";
import { connect } from "react-redux";
import { createElement, deleteElement, selectElement } from "../../../redux/actions";
import {Classes, ContextMenu, Divider, Menu, MenuDivider, MenuItem, ResizeSensor} from "@blueprintjs/core";
import { debounce } from "debounce";
import uuid from "uuid/v1"
import Cytoscape from "cytoscape";
import coseBilkent from 'cytoscape-cose-bilkent';
import stylesheet from "./editor-stylesheet";
import "./style.css";

Cytoscape.use( coseBilkent );

class EditorComponent extends Component {

  updateColors = () => {
    const { model: { id }, selectedElementId } = this.props;
    const visited = this.props.execution.visited[id];
    this.props.model.editor.elements.forEach(element => {
      if (visited && visited[element.data.id]) {
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
    if (selectedElementId) {
      this.editor.elements().unselect()
      this.editor.elements('#'+selectedElementId).select()
    }
  }

  componentDidMount() {
    const container = findDOMNode(this);
    this.editor = new Cytoscape({
      container,
      style: stylesheet
    });
    this.updateColors();
    this.editor.json(this.props.model.editor);
    this.addEventHandlers();
  }

  componentWillUnmount() {
    this.editor.destroy();
  }

  componentDidUpdate(prevProps, prevState) {
    this.updateColors();
    this.editor.json(this.props.model.editor);
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
              position: { x, y }
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

    this.editor.on('tapend', event => {
      if (this.keyCode === 69) {
        this.editor.autoungrabify(false);
        if (this.editor != event.target && event.target.isNode()) {
          this.props.createElement({
            id: uuid(),
            source: this.source.id(),
            target: event.target.id(),
            name: 'v_NewVertex'
          });
        }
      }
    });

    this.editor.on('cxttap', 'node, edge', event => {
      const { clientX, clientY } = event.originalEvent;
      ContextMenu.show(
        <Menu>
          <MenuItem icon="cross" text="Delete" onClick={() => this.props.deleteElement(event.target.remove().map(element => element.id()))}/>
          <Divider/>
          <MenuItem icon="full-circle" text="Breakpoint...">
            <MenuItem disabled={true} icon="new-object" text="Add breakpoint" />
            <MenuItem disabled={true} icon="graph-remove" text="Remove breakpoint" />
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
              <MenuItem icon="layout-auto" text="Auto" onClick={() => this.editor.layout({ name: 'cose-bilkent', nodeDimensionsIncludeLabels: true, idealEdgeLength: 200 }).run()} />
              <MenuItem icon="layout-circle" text="Circle" onClick={() => this.editor.layout({ name: 'circle' }).run()} />
              <MenuItem icon="layout-grid" text="Grid" onClick={() => this.editor.layout({ name: 'grid' }).run()} />
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
    updated: models.updated,
    execution,
    selectedElementId
  }
};

export default connect(mapStateToProps, { createElement, deleteElement, selectElement })(EditorComponent);

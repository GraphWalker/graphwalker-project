import React, { Component, createElement } from 'react';
import { findDOMNode } from "react-dom";
import { connect } from "react-redux";
import { selectElement } from "../../../redux/actions";
import {Classes, ContextMenu, Divider, Menu, MenuDivider, MenuItem, ResizeSensor} from "@blueprintjs/core";
import { debounce } from "debounce";
import Cytoscape from "cytoscape";
import coseBilkent from 'cytoscape-cose-bilkent';
import stylesheet from "./editor-stylesheet";

import "./style.css";

Cytoscape.use( coseBilkent );

class EditorComponent extends Component {

  componentDidMount() {
    const container = findDOMNode(this);
    this.editor = new Cytoscape({
      container,
      style: stylesheet
    });
    this.editor.json(this.props.model.editor);
    this.addEventHandlers();
  }

  componentWillUnmount() {
    this.editor.destroy();
  }

  componentDidUpdate(prevProps, prevState) {
    this.editor.json(this.props.model.editor);
  }

  addEventHandlers() {
    this.editor.on('tap', event => {
      if (event.target === this.editor) {
        this.props.selectElement(null);
      } else {
        this.props.selectElement(event.target.id());
      }
    });

    this.editor.on('cxttap', 'node, edge', event => {
      const { clientX, clientY } = event.originalEvent;
      ContextMenu.show(
        <Menu>
          <MenuItem icon="cross" text="Delete" onClick={() => event.target.remove()}/>
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
            <MenuItem disabled={true} icon="insert" text="Add vertex" />
            <MenuItem icon="select" text="Select all" onClick={() => this.editor.elements().select()} />
            <MenuItem disabled={this.editor.elements(':selected').length === 0} icon="cross" text="Delete selected" onClick={() => this.editor.remove(':selected')}/>
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

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  return {
    model: models[selectedModelIndex],
    updated: models.updated
  }
};

export default connect(mapStateToProps, { selectElement })(EditorComponent);

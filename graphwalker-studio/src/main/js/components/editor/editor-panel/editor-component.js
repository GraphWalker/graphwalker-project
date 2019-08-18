import React, { Component, createElement } from 'react';
import { findDOMNode } from "react-dom";
import { connect } from "react-redux";
import { selectElement } from "../../../redux/actions";
import { Classes, ContextMenu, Menu, MenuDivider, MenuItem, ResizeSensor } from "@blueprintjs/core";
import { debounce } from "debounce";
import Cytoscape from "cytoscape";
import stylesheet from "./editor-stylesheet";
import "./style.css";

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


    this.editor.on('cxttap', 'node', event => {
      const { clientX, clientY } = event.originalEvent;
      ContextMenu.show(
        <Menu>
          <MenuItem icon="full-circle" text="Breakpoint...">
            <MenuItem icon="new-object" text="Add breakpoint" />
            <MenuItem icon="graph-remove" text="Remove breakpoint" />
          </MenuItem>
          <MenuItem icon="cross" text="Delete" />
        </Menu>, { left: clientX, top: clientY });
    })

    this.editor.on('cxttap', event => {
      if (event.target === this.editor) {
        const { clientX, clientY } = event.originalEvent;
        ContextMenu.show(
          <Menu>
            <MenuItem icon="insert" text="Add vertex" />
            <MenuItem icon="select" text="Select all" />
            <MenuItem icon="layout" text="Layout...">
              <MenuItem icon="layout-auto" text="Auto" />
              <MenuItem icon="layout-circle" text="Circle" />
              <MenuItem icon="layout-grid" text="Grid" />
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

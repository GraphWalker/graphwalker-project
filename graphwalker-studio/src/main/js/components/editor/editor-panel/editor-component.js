import React, { Component, createElement } from 'react';
import { findDOMNode } from "react-dom";
import { connect } from "react-redux";
import { selectElement } from "../../../redux/actions";
import { ResizeSensor } from "@blueprintjs/core";
import { debounce } from "debounce";
import Cytoscape from "cytoscape";
import stylesheet from "./editor-stylesheet";
import "./style.css";

class EditorComponent extends Component {

  constructor(props) {
    super(props);
  }

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
    this.editor.on('tap', (event) => {
      if (event.target === this.editor) {
        this.props.selectElement(null);
      } else {
        this.props.selectElement(event.target.id());
      }
    })
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

import React, { Component } from 'react';
import {ResizeSensor, Tab, Tabs} from "@blueprintjs/core";
import CytoscapeComponent from "react-cytoscapejs";
import Container from "../container";

export default class EditorPanel extends Component {

  handleResize = (entries) => {
    console.log(this.cy.width(), entries.map(e => e.contentRect.width));
    this.cy.resize();
  };

  render() {
    return (
      <ResizeSensor onResize={this.handleResize}>
        <Container className="editor-container">
            <CytoscapeComponent cy={(cy) => { this.cy = cy }} className="editor-component"/>
        </Container>
      </ResizeSensor>
    )
  }
}

import React, { Component } from 'react';
import cytoscape from "cytoscape"

const CYTOSCAPE_TAG = 'cy';

export default class EditorContainer extends Component {

  componentDidMount() {
    this.updateCytoscape();
  }

  updateCytoscape() {
    const properties = Object.assign({}, {
      container: document.getElementById(CYTOSCAPE_TAG),
      elements: {
        nodes: [
          { data: { id: "n1", foo: "one", weight: 0.25 }, classes: "odd one" },
          { data: { id: "n2", foo: "two", weight: 0.5 }, classes: "even two" },
          { data: { id: "n3", foo: "three", weight: 0.75 }, classes: "odd three" },
          { data: { id: "n4", parent: "n5", foo: "bar" } },
          { data: { id: "n5" } }
        ],

        edges: [
          { data: { id: "n1n2", source: "n1", target: "n2", weight: 0.33 }, classes: "uh" },
          { data: { id: "n2n3", source: "n2", target: "n3", weight: 0.66 }, classes: "huh" },
          { data: { id: "n1n1", source: "n1", target: "n1" } }
        ]
      }
    });
    this.cy = cytoscape(properties);
  }

  render() {
    return (
      <div id={CYTOSCAPE_TAG} style={{ padding: 0, background: '#fff', height: '100%' }} />
    );
  }
}

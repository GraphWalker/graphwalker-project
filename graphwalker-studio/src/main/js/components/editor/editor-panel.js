import React, { Component } from 'react';
import { connect } from "react-redux";
import {ResizeSensor, Tab, Tabs} from "@blueprintjs/core";
import CytoscapeComponent from "react-cytoscapejs";
import Container from "../container";
import { selectElement } from "../../redux/actions";

const stylesheet = [
  {
    selector: 'node',
    style: {
      content: 'data(label)',
      'text-wrap': 'wrap',
      'text-valign': 'center',
      'text-halign': 'center',
      shape: 'roundrectangle',
      width: 'label',
      height: 'label',
      color: 'black',
      'background-color': 'data(color)',
      'line-color': 'data(color)',
      'padding-left': '10',
      'padding-right': '10',
      'padding-top': '10',
      'padding-bottom': '10'
    },
  }, {
    selector: 'edge',
    style: {
      'content': 'data(label)',
      'text-wrap': 'wrap',
      'curve-style': 'bezier',
      'text-rotation': 'autorotate',
      'target-arrow-shape': 'triangle',
      'width': '4',
      'line-color': 'data(color)',
      'target-arrow-color': 'data(color)',
      'background-color': 'data(color)'
    },
  }, {
    selector: ':selected',
    style: {
      'border-width': 4,
      'border-color': 'black',
      'line-color': 'black',
      'target-arrow-color': 'black'
    }
  }
];

class EditorPanel extends Component {

  constructor(props) {
    super(props);
    this.state = {
      width: 0,
      height: 0
    }
  }

  handleResize = (entries) => {
    const entry = entries.filter(entry => entry.target.className === 'container-column')[0];
    if (entry)
      console.log([entry.contentRect.width, entry.contentRect.height], [this.cy.width(), this.cy.height()]);


    //const { width, height } = entries[0].contentRect;
    //console.log(entries.map(e => [e.target, e.contentRect.width, e.contentRect.height]));
    //this.cy.resize();
    /*
    this.setState({
      width: width,
      height: height
    });

                                style={{ width: `${this.state.width}px`, height: `${this.state.height}px` }}
     */
    this.cy.resize();
  };

  componentDidMount() {
    this.cy.resize();
    this.cy.fit(null, 50);
    this.cy.on('tap', (event) => {
      if (event.target === this.cy) {
        this.props.selectElement(null);
      } else {
        this.props.selectElement(event.target.id());
      }
    })
  }

  componentDidUpdate() {
    this.cy.resize();
    this.cy.fit(null, 50);
  }

  render() {

    const elements = [];
    const { startElementId } = this.props.model;
    this.props.model.vertices.map(({ id, name: label, sharedState, actions, requirements, properties: { x = 0, y = 0 }}) => elements.push({
      group: 'nodes',
      data: { id, label, color: id === startElementId ? 'LightGreen' : 'LightSteelBlue' },
      position: { x, y }
    }));

    this.props.model.edges.filter(({ sourceVertexId: source }) => source == null).map(() => elements.push({
      group: 'nodes',
      data: { id: 'Start', label: 'Start', color: 'LightGreen'},
      position: { x: 0, y: 0 }
    }));

    this.props.model.edges.map(({ id, name: label, guard, actions, sourceVertexId: source = 'Start', targetVertexId: target }) => elements.push({
      group: 'edges',
      data: { id, label, source: source == null ? 'Start': source, target, color: source == null ? 'LightGreen': 'LightSteelBlue' }
    }));

    return (
      <ResizeSensor observeParents onResize={this.handleResize}>
        <Container className="editor-container">
            <CytoscapeComponent
              elements={elements}
              cy={cy => { this.cy = cy }}
              className="editor-component"
              stylesheet={stylesheet}
            />
        </Container>
      </ResizeSensor>
    )
  }
}

export default connect(null, { selectElement })(EditorPanel);

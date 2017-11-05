import React, { Component } from 'react';
import ContextMenu from './ContextMenu';
import cytoscape from "cytoscape"

export default class EditorContainer extends Component {

  state = {
    openEvent: false,
  };

  openMenu = event => {
    console.log("openMenu");
    this.setState({ openEvent: event });
  };

  closeMenu = () => {
    this.setState({ openEvent: false });
  };

  handleContextMenu = (event) => {
    const clientRect = document.getElementById("cy").getBoundingClientRect();
    const hitX = clientRect.left <= event.pageX && event.pageX <= clientRect.right;
    const hitY = clientRect.top <= event.pageY && event.pageY <= clientRect.bottom;
    if (hitX && hitY) {
      event.preventDefault();
      if (this.state.openEvent) {
        this.closeMenu();
      }
      this.openMenu(event);
      return false;
    }
  };

  componentDidMount() {
    this.updateCytoscape();
    document.addEventListener('contextmenu', this.handleContextMenu);
  }

  componentWillUnmount() {
    document.removeEventListener('contextmenu', this.handleContextMenu);
  }

  updateCytoscape() {
    const properties = Object.assign({}, {
      container: document.getElementById('cy'),
      elements: {
        nodes: [
          { data: { id: "n1", name: "one", weight: 0.25 }, classes: "odd one" },
          { data: { id: "n2", name: "two", weight: 0.5 }, classes: "even two" },
          { data: { id: "n3", name: "three", weight: 0.75 }, classes: "odd three" },
          { data: { id: "n4", parent: "n5", name: "bar" } },
          { data: { id: "n5" } }
        ],
        edges: [
          { data: { id: "n1n2", source: "n1", target: "n2", weight: 0.33 }, classes: "uh" },
          { data: { id: "n2n3", source: "n2", target: "n3", weight: 0.66 }, classes: "huh" },
          { data: { id: "n1n1", source: "n1", target: "n1" } }
        ]
      },
      style: [{
        selector: 'node',
        style: {
          'content': 'data(name)',
          'text-wrap': 'wrap',
          'text-valign': 'center',
          'text-halign': 'center',
          'shape': 'roundrectangle',
          'width': 'label',
          'height': 'label',
          'color': 'black',
          // 'background-color': 'data(color)',
          // 'line-color': 'data(color)',
          'padding-left': '10',
          'padding-right': '10',
          'padding-top': '10',
          'padding-bottom': '10'
        }
      }, {
        selector: 'edge',
        style: {
          // 'content': 'data(name)',
          'text-wrap': 'wrap',
          'curve-style' : 'bezier',
          'text-rotation': 'autorotate',
          'target-arrow-shape': 'triangle',
          'width': '4',
          //'line-color': 'data(color)',
          //'target-arrow-color': 'data(color)',
          //'background-color': 'data(color)'
        }
      }]
    });
    this.cy = cytoscape(properties);
    this.cy.nodes().ungrabify();
    this.cy.on('tap', this.createNode);
    this.cy.on('select', this.selectNode);
    this.cy.on('unselect', this.deselectNode);
    this.cy.on('taphold', 'node', this.newEdgeStart);
    this.cy.on('tapdrag', this.newEdgeDrag);
    // this.cy.on('tapdragover', this.createEdge);
    this.cy.on('tapend', this.newEdgeEnd);
    this.cy.on('cxttap', this.openMenu);
    this.hasSelectedNodes = false;
    this.isCreatingEdge = false;
  }

  createNode = (event) => {
    if (event.target === this.cy && !this.hasSelectedNodes) {
      const position = event.renderedPosition;
      const nodes = this.cy.add([{
        group: 'nodes',
        data: {
          // temp just so it should be more unique
          id: 'testid' + position.x,
          name: '' + this.cy.nodes().length
        },
        renderedPosition: position
      }]);
      nodes.ungrabify();
    }
    this.hasSelectedNodes = false;
  };

  selectNode = (event) => {
    event.target.grabify();
    this.hasSelectedNodes = true;
  };

  deselectNode = (event) => {
    event.target.ungrabify();
  };

  newEdgeStart = (event) => {
    if (!event.target.selected() && !this.isCreatingEdge) {
      this.ghostNode = this.cy.add({
        group: 'nodes',
        data: {
          id: 'ghostNode'
        },
        css: {
          'background-color': 'blue',
          'width': 0.0001,
          'height': 0.0001,
          'opacity': 0,
          'events': 'no'
        },
        renderedPosition: event.renderedPosition
      });
      this.ghostEdge = this.cy.add({
        group: 'edges',
        data: {
          source: event.target.id(),
          target: this.ghostNode.id(),
          id: 'ghostEdge'
        },
        css: {
          'events': 'no'
        }
      });
      this.isCreatingEdge = true;
    }
  };

  newEdgeDrag = (event) => {
    if (this.isCreatingEdge) {
      this.ghostNode.renderedPosition(event.renderedPosition);
    }
  };

  newEdgeEnd = (event) => {
    if (this.ghostEdge && event.target !== this.cy && event.target.isNode()) {
      this.cy.add({
        group: 'edges',
        data: {
          source: this.ghostEdge.source().id(),
          target: event.target.id(),
          id: 'newEdge' + this.ghostEdge.source().id() + event.target.id()
        },
        css: {
          'events': 'no'
        }
      });
    }
    if (this.ghostEdge) {
      this.ghostEdge.remove();
      delete this.ghostEdge;
    }
    if (this.ghostNode) {
      this.ghostNode.remove();
      delete this.ghostNode;
    }
    this.isCreatingEdge = false;
  };

  render() {
    return (
      <div style={{ padding: 0, background: '#fff', height: '100%', width: '100%' }}>
        <div id="cy" style={{ padding: 0, background: '#fff', height: '100%', width: '100%', overflow: 'hidden' }} />
        <ContextMenu openEvent={this.state.openEvent} closeMenu={this.closeMenu}/>
      </div>
    );
  }
}

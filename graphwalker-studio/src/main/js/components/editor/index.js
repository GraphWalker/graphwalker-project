import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import ContextMenu from './ContextMenu';
import cytoscape from "cytoscape"

const styles = theme => ({
  container: {
    padding: 0,
    backgroundColor: theme.palette.background.default,
    height: '100%',
    width: '100%',
  },
  editor: {
    padding: 0,
    height: '100%',
    width: '100%',
    overflow: 'hidden',
    outline: 'none'
  }
});

class EditorContainer extends Component {

  static propTypes = {
    classes: PropTypes.object,
    model: PropTypes.object,
    openMenu: PropTypes.func,
  };

  state = {
    openEvent: false,
  };

  componentDidMount() {
    this.updateCytoscape(this.props.model.graph);
    document.addEventListener('contextmenu', this.handleContextMenu);
  }

  componentWillReceiveProps(nextProps) {
    this.updateCytoscape(nextProps.model.graph);
  }

  componentWillUnmount() {
    document.removeEventListener('contextmenu', this.handleContextMenu);
  }

  openMenu = event => {
    document.getElementById('cy').blur();
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

  handleKeyPress = (event) => {
    if (event.key === 'Backspace') {
      this.cy.remove(':selected');
    }
  };

  focus = (el) => {
    const scrollHierarchy = [];
    let parent = el.parentNode;
    while (parent) {
      scrollHierarchy.push([parent, parent.scrollLeft, parent.scrollTop]);
      parent = parent.parentNode;
    }
    el.focus();
    scrollHierarchy.forEach(function (item) {
      const el = item[0];
      if (el.scrollLeft !== item[1]) {
        el.scrollLeft = item[1];
      }
      if (el.scrollTop !== item[2]) {
        el.scrollTop = item[2];
      }
    });
  };

  setFocus = (event) => {
    if (event.target === this.cy) {
      this.focus(document.getElementById('cy'));
    }
  };

  disableGestures() {
    this.panningEnabled = this.cy.panningEnabled();
    this.zoomingEnabled = this.cy.zoomingEnabled();
    this.boxSelectionEnabled = this.cy.boxSelectionEnabled();
    this.cy
      .zoomingEnabled(false)
      .panningEnabled(false)
      .boxSelectionEnabled(false);
  }

  resetGestures() {
    this.cy
      .zoomingEnabled(this.zoomingEnabled)
      .panningEnabled(this.panningEnabled)
      .boxSelectionEnabled(this.boxSelectionEnabled);
  }

  updateCytoscape(graph) {
    const properties = Object.assign({}, {
      container: document.getElementById('cy'),
      elements: graph,
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
    this.cy.ready(() => {
      this.cy.nodes().ungrabify();
      this.cy.on('tap', this.createNode);
      this.cy.on('select', this.selectNode);
      this.cy.on('unselect', this.deselectNode);
      this.cy.on('taphold', 'node', this.newEdgeStart);
      this.cy.on('tapdrag', this.newEdgeDrag);
      // this.cy.on('tapdragover', this.createEdge);
      this.cy.on('tapend', this.newEdgeEnd);
      this.cy.on('cxttap', this.openMenu);

      this.cy.on('mouseover', this.setFocus);

      this.cy.on('keydown', this.handleKeyPress);


      document.getElementById('cy').addEventListener('keydown', this.handleKeyPress);
    });
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
          id: 'testid' + position.x + new Date(),
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
      this.disableGestures();
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
          id: 'newEdge' + this.ghostEdge.source().id() + event.target.id() + new Date()
        }
      });
      this.resetGestures();
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
    const { classes } = this.props;
    return (
      <div className={classes.container}>
        <div className={classes.editor}
            id="cy"
            tabIndex="-1"
        />
        <ContextMenu closeMenu={this.closeMenu}
            openEvent={this.state.openEvent}
        />
      </div>
    );
  }
}

export default compose(
  withStyles(styles, {
    withTheme: true,
  }),
  connect(state => ({
    model: state.project.models[state.project.activeModelId],
  }))
)(EditorContainer);

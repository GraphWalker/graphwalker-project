import React, { Component } from 'react';
import { connect } from "react-redux";
import {FormGroup, InputGroup, Switch} from "@blueprintjs/core";

class ElementGroup extends Component {
  render() {
    const { id = "", name = "", sharedState = "", guard = "", actions = [], requirements = [] } = this.props.element;
    return (
      <FormGroup label="Element Properties" labelFor="text-input">
        <InputGroup placeholder="Element name" value={name}/>
        <InputGroup placeholder="Element id" value={id}/>
        <InputGroup placeholder="Shared name" value={sharedState}/>
        <InputGroup placeholder="Guard" value={guard}/>
        <InputGroup placeholder="Actions" value={actions.join()}/>
        <InputGroup placeholder="Requirements" value={requirements.join()}/>
        <Switch id="text-input" label="Start element" checked={this.props.isStartElement}/>
      </FormGroup>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex, selectedElementId }}) => {
  const model = models[selectedModelIndex];
  const elements = [...model.vertices, ...model.edges];
  return {
    element: elements.filter(element => element.id === selectedElementId)[0] || {},
    isStartElement: model.startElementId === selectedElementId
  }
};

export default connect(mapStateToProps)(ElementGroup);

import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup, Switch, TextArea } from "@blueprintjs/core";
import { updateElement, setStartElement } from "../../redux/actions";

class ElementGroup extends Component {
  render() {
    const { id, name, sharedState, guard, actions, requirements, updateElement, isStartElement, setStartElement } = this.props;
    return (
      <FormGroup label="Element Properties" labelFor="text-input">
        <InputGroup placeholder="Element name" value={name} onChange={(event) => updateElement('name', event)}/>
        <InputGroup placeholder="Element id" value={id} onChange={(event) => updateElement('id', event)}/>
        <InputGroup placeholder="Shared name" value={sharedState} onChange={(event) => updateElement('sharedState', event)}/>
        <InputGroup placeholder="Guard" value={guard} onChange={(event) => updateElement('guard', event)}/>
        <div className="bp3-input-group">
          <TextArea placeholder="Actions" value={actions} onChange={(event) => updateElement('actions', event)}/>
        </div>
        <div className="bp3-input-group">
          <TextArea placeholder="Requirements" value={requirements} onChange={(event) => updateElement('requirements', event)}/>
        </div>
        <Switch id="text-input" label="Start element" checked={isStartElement} onChange={setStartElement}/>
      </FormGroup>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex, selectedElementId }}) => {
  const model = models[selectedModelIndex];
  const elements = [...model.vertices, ...model.edges];
  const element = elements.filter(element => element.id === selectedElementId)[0] || {};
  const { id = "", name = "", sharedState = "", guard = "", actions = [], requirements = [] } = element;
  return {
    id,
    name,
    sharedState,
    guard,
    actions,
    requirements,
    isStartElement: model.startElementId === selectedElementId
  }
};

export default connect(mapStateToProps, { updateElement, setStartElement })(ElementGroup);

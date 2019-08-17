import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup, Switch, TextArea } from "@blueprintjs/core";
import { updateElement, setStartElement } from "../../redux/actions";

class ElementGroup extends Component {
  render() {
    const { id, name, sharedState, guard, actions, requirements, updateElement, isStartElement, setStartElement, disabled } = this.props;
    return (
      <>
        <FormGroup label="Element Name" disabled={disabled}>
          <InputGroup disabled={disabled} value={name} onChange={(event) => updateElement('name', event)}/>
        </FormGroup>
        <FormGroup label="Element ID" disabled={disabled}>
          <InputGroup disabled={disabled} value={id} onChange={(event) => updateElement('id', event)}/>
        </FormGroup>
        <FormGroup label="Shared Name" disabled={disabled}>
          <InputGroup disabled={disabled} value={sharedState} onChange={(event) => updateElement('sharedState', event)}/>
        </FormGroup>
        <FormGroup label="Guard" disabled={disabled}>
          <InputGroup disabled={disabled} value={guard} onChange={(event) => updateElement('guard', event)}/>
        </FormGroup>
        <FormGroup label="Actions" disabled={disabled}>
          <div className="bp3-input-group">
            <TextArea disabled={disabled} value={actions} onChange={(event) => updateElement('actions', event)}/>
          </div>
        </FormGroup>
        <FormGroup label="Requirements" disabled={disabled}>
          <div className="bp3-input-group">
            <TextArea disabled={disabled} value={requirements} onChange={(event) => updateElement('requirements', event)}/>
          </div>
        </FormGroup>
        <Switch disabled={disabled} label="Start element" checked={isStartElement} onChange={setStartElement}/>
      </>
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
    isStartElement: model.startElementId === selectedElementId,
    disabled: selectedElementId === null
  }
};

export default connect(mapStateToProps, { updateElement, setStartElement })(ElementGroup);

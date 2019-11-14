import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup, Switch, TextArea } from "@blueprintjs/core";
import { updateElement, setStartElement } from "../../redux/actions";
import Group from "./group";

class ElementGroup extends Component {
  render() {
    const { id, name, sharedState, guard, actions, requirements, updateElement, isStartElement, setStartElement, disabled } = this.props;
    return (
      <Group name="Element" isOpen={true}>
        <FormGroup label="Name" disabled={disabled}>
          <InputGroup disabled={disabled} value={name} onChange={({ target: { value }}) => updateElement('name', value)}/>
        </FormGroup>
        <FormGroup label="Shared Name" disabled={disabled}>
          <InputGroup disabled={disabled} value={sharedState} onChange={({ target: { value }}) => updateElement('sharedState', value)}/>
        </FormGroup>
        <FormGroup label="Guard" disabled={disabled}>
          <InputGroup disabled={disabled} value={guard} onChange={({ target: { value }}) => updateElement('guard', value)}/>
        </FormGroup>
        <FormGroup label="Actions" disabled={disabled}>
          <div className="bp3-input-group">
            <TextArea disabled={disabled} value={actions.join("\n")} onChange={({ target: { value }}) => updateElement('actions', value.split("\n"))}/>
          </div>
        </FormGroup>
        <FormGroup label="Requirements" disabled={disabled}>
          <div className="bp3-input-group">
            <TextArea disabled={disabled} value={requirements.join("\n")} onChange={({ target: { value }}) => updateElement('requirements', value.split("\n"))}/>
          </div>
        </FormGroup>
        <Switch disabled={disabled} label="Start element" checked={isStartElement} onChange={({ target: { checked }}) => setStartElement(id)}/>
      </Group>
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

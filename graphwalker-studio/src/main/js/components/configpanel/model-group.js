import React, { Component } from 'react';
import { connect } from "react-redux";
import {FormGroup, InputGroup, TextArea} from "@blueprintjs/core";
import { updateModel } from "../../redux/actions";

class ModelGroup extends Component {
  render() {
    const { name, generator, actions, updateModel} = this.props;
    return (
      <>
        <FormGroup label="Model Name">
          <InputGroup placeholder="Model Name" value={name} onChange={({ target: { value }}) => updateModel('name', value)}/>
        </FormGroup>
        <FormGroup label="Model Generator">
          <InputGroup placeholder="Model Generator" value={generator} onChange={({ target: { value }}) => updateModel('generator', value)}/>
        </FormGroup>
        <FormGroup label="Model Actions">
          <div className="bp3-input-group">
            <TextArea value={actions.join("\n")} onChange={({ target: { value }}) => updateModel('actions', value.split("\n"))}/>
          </div>
        </FormGroup>
      </>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  const { name, generator, actions = [] } = models[selectedModelIndex];
  return {
    name,
    generator,
    actions
  }
};

export default connect(mapStateToProps, { updateModel })(ModelGroup);

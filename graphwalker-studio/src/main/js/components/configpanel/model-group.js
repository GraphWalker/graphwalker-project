import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup, TextArea } from "@blueprintjs/core";
import { updateModel } from "../../redux/actions";
import Group from "./group";

class ModelGroup extends Component {
  render() {
    const { name, actions, updateModel} = this.props;
    return (
      <Group name="Model" isOpen={true}>
        <FormGroup label="Name">
          <InputGroup placeholder="Model Name" value={name} onChange={({ target: { value }}) => updateModel('name', value)}/>
        </FormGroup>
        <FormGroup label="Actions">
          <div className="bp3-input-group">
            <TextArea value={actions.join("\n")} onChange={({ target: { value }}) => updateModel('actions', value.split("\n"))}/>
          </div>
        </FormGroup>
      </Group>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  const { name, actions = [] } = models[selectedModelIndex];
  return {
    name,
    actions
  }
};

export default connect(mapStateToProps, { updateModel })(ModelGroup);

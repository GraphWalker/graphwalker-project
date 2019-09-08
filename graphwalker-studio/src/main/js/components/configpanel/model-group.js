import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup } from "@blueprintjs/core";
import { updateModel } from "../../redux/actions";

class ModelGroup extends Component {
  render() {
    const { name, generator, updateModel} = this.props;
    return (
      <>
        <FormGroup label="Model Name">
          <InputGroup placeholder="Model Name" value={name} onChange={({ target: { value }}) => updateModel('name', value)}/>
        </FormGroup>
        <FormGroup label="Model Generator">
          <InputGroup placeholder="Model Generator" value={generator} onChange={({ target: { value }}) => updateModel('generator', value)}/>
        </FormGroup>
      </>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  const { name, generator } = models[selectedModelIndex]
  return {
    name,
    generator
  }
};

export default connect(mapStateToProps, { updateModel })(ModelGroup);

import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup } from "@blueprintjs/core";
import { updateModel } from "../../redux/actions";

class ModelGroup extends Component {
  render() {
    const { name, generator, updateModel} = this.props;
    return (
      <FormGroup label="Model Properties">
        <InputGroup placeholder="Model Name" value={name} onChange={(event) => updateModel('name', event)}/>
        <InputGroup placeholder="Model Generator Builder"/>
        <InputGroup placeholder="Model Generator" value={generator} onChange={(event) => updateModel('generator', event)}/>
      </FormGroup>
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

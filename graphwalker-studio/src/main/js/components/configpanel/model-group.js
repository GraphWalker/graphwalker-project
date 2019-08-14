import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup } from "@blueprintjs/core";

class ModelGroup extends Component {
  render() {
    return (
      <FormGroup label="Model Properties" labelFor="text-input">
        <InputGroup placeholder="Model Name" value={this.props.model.name}/>
        <InputGroup placeholder="Model Generator Builder"/>
        <InputGroup placeholder="Model Generator" value={this.props.model.generator}/>
      </FormGroup>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  return {
    model: models[selectedModelIndex]
  }
};

export default connect(mapStateToProps)(ModelGroup);

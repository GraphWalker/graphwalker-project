import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup } from "@blueprintjs/core";
import { updateModel } from "../../redux/actions";

class ModelGroup extends Component {
  render() {
    return (
      <FormGroup label="Model Properties" labelFor="text-input">
        <InputGroup placeholder="Model Name" value={this.props.name} onChange={(event) => this.props.updateModel('name', event)}/>
        <InputGroup placeholder="Model Generator Builder"/>
        <InputGroup placeholder="Model Generator" value={this.props.generator} onChange={(event) => this.props.updateModel('generator', event)}/>
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

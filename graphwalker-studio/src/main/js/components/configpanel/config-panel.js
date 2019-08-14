import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup, Switch, Slider } from "@blueprintjs/core";
import './style.css';

class ConfigPanel extends Component {

  render() {
    if (this.props.model) {
      return (
        <div className="config-panel">
          <FormGroup label="Model Properties" labelFor="text-input">
            <InputGroup placeholder="Model Name" value={this.props.model.name}/>
            <InputGroup placeholder="Model Generator Builder"/>
            <InputGroup placeholder="Model Generator" value={this.props.model.generator}/>
          </FormGroup>
          <FormGroup label="Element Properties" labelFor="text-input">
            <InputGroup placeholder="Element name"/>
            <InputGroup placeholder="Element id"/>
            <InputGroup placeholder="Shared name"/>
            <InputGroup placeholder="Guard"/>
            <InputGroup placeholder="Actions"/>
            <InputGroup placeholder="Requirements"/>
            <Switch id="text-input" label="Start element"/>
          </FormGroup>
          <FormGroup label="Execution Delay" labelFor="text-input">
            <div>
              <Slider min={0} max={500} stepSize={1} labelRenderer={false} value={250}/>
            </div>
          </FormGroup>
        </div>
      )
    } else {
      return null;
    }
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  return {
    model: selectedModelIndex == null ? null : models[selectedModelIndex]
  }
}

export default connect(mapStateToProps)(ConfigPanel);

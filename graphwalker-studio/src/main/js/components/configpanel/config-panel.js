import React, { Component } from 'react';
import './style.css';
import {Divider, FormGroup, InputGroup, Switch} from "@blueprintjs/core";

export default class ConfigPanel extends Component {

  render() {
    return (
      <div className="config-panel">
        <FormGroup label="Model Properties" labelFor="text-input">
          <InputGroup placeholder="Model Name"/>
          <InputGroup placeholder="Model Generator Builder"/>
          <InputGroup placeholder="Model Generator"/>
        </FormGroup>
        <FormGroup label="Element Properties" labelFor="text-input">
          <InputGroup placeholder="Element name"/>
          <InputGroup placeholder="Element id"/>
          <InputGroup placeholder="Shared name"/>
          <InputGroup placeholder="Guard"/>
          <InputGroup placeholder="Actions"/>
          <InputGroup placeholder="Requirements"/>
          <Switch id="text-input" label="Start element" />
        </FormGroup>
      </div>
    )
  }
}

import React, { Component } from 'react';
import Tabs, { Tab } from 'material-ui/Tabs';

export class ModelSelector extends Component {

  state = {
    value: 0,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    const { value } = this.state;
    return (
      <Tabs value={value} onChange={this.handleChange} scrollable scrollButtons="auto">
        <Tab label="FindOwnersSharedState" />
        <Tab label="NewOwnerSharedState" />
        <Tab label="OwnerInformationSharedState" />
        <Tab label="PetClinicSharedState" />
        <Tab label="VeterinariensSharedState" />
      </Tabs>
    );
  }
}

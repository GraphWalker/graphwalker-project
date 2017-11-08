import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Tabs, { Tab } from 'material-ui/Tabs';

const styles = theme => ({
});

class ModelSelector extends Component {

  state = {
    value: 0,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    const { value } = this.state;
    return (
      <Tabs value={value} onChange={this.handleChange} indicatorColor="accent" textColor="accent" scrollable scrollButtons="on">
        <Tab label="FindOwnersSharedState" />
        <Tab label="NewOwnerSharedState" />
        <Tab label="OwnerInformationSharedState" />
        <Tab label="PetClinicSharedState" />
        <Tab label="VeterinariensSharedState" />
      </Tabs>
    );
  }
}

export default withStyles(styles, { withTheme: true })(ModelSelector);

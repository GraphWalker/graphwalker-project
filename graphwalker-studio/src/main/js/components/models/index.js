import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Tabs, { Tab } from 'material-ui/Tabs';

const styles = theme => ({
  root: {
    flexGrow: 1,
    width: '100%',
    backgroundColor: theme.palette.background.paper,
    color: theme.palette.text.primary,
  },
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
    const { classes } = this.props;
    return (
      <div className={classes.root}>
        <Tabs value={value} onChange={this.handleChange} indicatorColor="accent" textColor="accent" scrollable scrollButtons="on">
          <Tab label="FindOwnersSharedState" />
          <Tab label="NewOwnerSharedState" />
          <Tab label="OwnerInformationSharedState" />
          <Tab label="PetClinicSharedState" />
          <Tab label="VeterinariensSharedState" />
        </Tabs>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(ModelSelector);

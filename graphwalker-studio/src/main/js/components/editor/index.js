import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Divider from 'material-ui/Divider';
import Tabs, { Tab } from 'material-ui/Tabs';
import EditorContainer from './container';
import { styles } from '../../styles'

class Editor extends Component {
  state = {
    value: 0,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    const { classes } = this.props;
    const { value } = this.state;
    return (
      <div className={classes.root}>
        <Tabs value={value} onChange={this.handleChange} scrollable scrollButtons="auto">
          <Tab label="FindOwnersSharedState" />
          <Tab label="NewOwnerSharedState" />
          <Tab label="OwnerInformationSharedState" />
          <Tab label="PetClinicSharedState" />
          <Tab label="VeterinariensSharedState" />
        </Tabs>
        <Divider/>
        <EditorContainer/>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Editor);

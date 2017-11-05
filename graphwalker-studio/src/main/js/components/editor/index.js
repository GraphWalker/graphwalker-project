import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Divider from 'material-ui/Divider';
import Tabs, { Tab } from 'material-ui/Tabs';
import EditorContainer from './EditorContainer';
import PropertiesTable from "./PropertiesTable";
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
        <div style={{ padding: 0, height: '100%', width: '100%' }}>
          <div style={{ float: 'left', padding: 0, background: '#fff', height: '100%', width: 'calc(100% - 340px)' }}>
            <EditorContainer/>
          </div>
          <div style={{float: 'right', width: '340px', height: '100%' }}>
            <PropertiesTable/>
          </div>
        </div>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Editor);

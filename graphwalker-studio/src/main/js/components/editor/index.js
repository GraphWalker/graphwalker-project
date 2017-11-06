import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import classNames from 'classnames';
import Divider from 'material-ui/Divider';
import Drawer from 'material-ui/Drawer';
import Typography from 'material-ui/Typography';
import Tabs, { Tab } from 'material-ui/Tabs';
import IconButton from 'material-ui/IconButton';
import ListIcon from 'material-ui-icons/List';
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
        <div style={{ padding: 0, height: '100%', width: '100%', background: '#fff' }}>
          <div style={{ float: 'left', padding: 0, background: '#fff', height: '100%', width: 'calc(100% - 340px)' }}>
            <EditorContainer/>
          </div>
          <Drawer
            type="permanent"
            anchor="right"
            classes={{
              paper: classNames(classes.propertiesDrawerPaper),
              docked: classNames(classes.propertiesDrawerDocked)
            }}
            open={true}>
            <div className={classes.propertiesDrawerInner}>
              <div className={classes.propertiesDrawerHeader}>
                <IconButton onClick={this.handleDrawerClose}>
                  <ListIcon/>
                </IconButton>
                <Typography color="inherit" noWrap>
                  Properties
                </Typography>
              </div>
              <Divider/>
              <PropertiesTable/>
              <Divider/>
              <div className={classes.spacer2} />
            </div>
          </Drawer>
        </div>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Editor);

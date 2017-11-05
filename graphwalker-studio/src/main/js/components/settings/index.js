import React, { Component } from 'react';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import Divider from 'material-ui/Divider';
import SettingsIcon from 'material-ui-icons/Settings';
import SettingsDialog from './SettingsDialog';

export class Settings extends Component {
  state = {
    open: false,
  };

  openSettings = () => {
    this.setState({
      open: true,
    });
  };

  closeSettings = () => {
    this.setState({
      open: false
    });
  };

  render() {
    return (
      <div>
        <Divider/>
        <List>
          <ListItem button onClick={this.openSettings}>
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
        <SettingsDialog open={this.state.open} onRequestClose={this.closeSettings}/>
      </div>
    );
  }
}

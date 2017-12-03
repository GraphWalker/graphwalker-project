import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import SettingsIcon from 'material-ui-icons/Settings';
import SettingsDialog from './SettingsDialog';

const styles = theme => ({
});

class Settings extends Component {
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
        <List>
          <ListItem button
              onClick={this.openSettings}
          >
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
        <SettingsDialog onRequestClose={this.closeSettings}
            open={this.state.open}
        />
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Settings);

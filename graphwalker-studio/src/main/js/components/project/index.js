import React, { Component } from 'react';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import Divider from 'material-ui/Divider';
import NewIcon from 'material-ui-icons/InsertDriveFile';
import LoadIcon from 'material-ui-icons/OpenInBrowser';
import SaveIcon from 'material-ui-icons/Save';
import SettingsIcon from 'material-ui-icons/Settings';

export class Project extends Component {
  render() {
    return (
      <div>
        <Divider/>
        <List>
          <ListItem button>
            <ListItemIcon>
              <NewIcon />
            </ListItemIcon>
            <ListItemText primary="New" />
          </ListItem>
          <ListItem button>
            <ListItemIcon>
              <LoadIcon />
            </ListItemIcon>
            <ListItemText primary="Load" />
          </ListItem>
          <ListItem button>
            <ListItemIcon>
              <SaveIcon />
            </ListItemIcon>
            <ListItemText primary="Save" />
          </ListItem>
        </List>
        <Divider/>
        <List>
          <ListItem button>
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
      </div>
    );
  }
}

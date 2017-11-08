import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import NewIcon from 'material-ui-icons/InsertDriveFile';
import LoadIcon from 'material-ui-icons/OpenInBrowser';
import SaveIcon from 'material-ui-icons/Save';

const styles = theme => ({
});

class Project extends Component {
  render() {
    return (
      <div>
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
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Project);

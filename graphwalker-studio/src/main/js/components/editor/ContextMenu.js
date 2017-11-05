import React, { Component } from 'react';
import { ListItemIcon } from 'material-ui/List';
import Popover from 'material-ui/Popover';
import { MenuItem } from 'material-ui/Menu';
import NetworkCheckIcon from 'material-ui-icons/NetworkCheck';
import PagesIcon from 'material-ui-icons/Pages';
import SaveIcon from 'material-ui-icons/Save';

export default class ContextMenu extends Component {

  get mockAnchorEl() {
    const openEvent = this.props.openEvent.originalEvent ? this.props.openEvent.originalEvent: this.props.openEvent;
    return {
      getBoundingClientRect: () => Object.assign({}, {
        left: openEvent ? openEvent.x : 0,
        top: openEvent ? openEvent.y : 0
      })
    };
  }

  render() {
    return (
        <Popover
          open={!!this.props.openEvent}
          anchorEl={this.mockAnchorEl}
          anchorOrigin={{horizontal: 'left', vertical: 'top'}}
          onRequestClose={this.props.closeMenu}
          modal="false">
          <MenuItem onClick={this.props.closeMenu}>
            <ListItemIcon>
              <NetworkCheckIcon />
            </ListItemIcon>
            TODO: Context menu
          </MenuItem>
          <MenuItem onClick={this.props.closeMenu}>
            <ListItemIcon>
              <PagesIcon />
            </ListItemIcon>
            Item Two
          </MenuItem>
          <MenuItem onClick={this.props.closeMenu}>
            <ListItemIcon>
              <SaveIcon />
            </ListItemIcon>
            Save
          </MenuItem>
        </Popover>
    );
  }
}

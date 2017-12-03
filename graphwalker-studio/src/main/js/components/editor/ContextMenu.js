import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { ListItemIcon } from 'material-ui/List';
import Popover from 'material-ui/Popover';
import { MenuItem } from 'material-ui/Menu';
import NetworkCheckIcon from 'material-ui-icons/NetworkCheck';
import PagesIcon from 'material-ui-icons/Pages';
import SaveIcon from 'material-ui-icons/Save';

export default class ContextMenu extends Component {

  static propTypes = {
    closeMenu: PropTypes.func,
    openEvent: PropTypes.oneOfType([PropTypes.bool, PropTypes.object]),
  };

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
    const { closeMenu, openEvent } = this.props;
    return (
        <Popover
            anchorEl={this.mockAnchorEl}
            anchorOrigin={{horizontal: 'left', vertical: 'top'}}
            modal="false"
            onRequestClose={closeMenu}
            open={!!openEvent}
        >
          <MenuItem onClick={closeMenu}>
            <ListItemIcon>
              <NetworkCheckIcon />
            </ListItemIcon>
            {'TODO: Context menu'}
          </MenuItem>
          <MenuItem onClick={closeMenu}>
            <ListItemIcon>
              <PagesIcon />
            </ListItemIcon>
            {'Item Two'}
          </MenuItem>
          <MenuItem onClick={closeMenu}>
            <ListItemIcon>
              <SaveIcon />
            </ListItemIcon>
            {'Save'}
          </MenuItem>
        </Popover>
    );
  }
}

import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import { openSettings, closeSettings } from '../../redux/actions/settings';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import SettingsIcon from 'material-ui-icons/Settings';
import SettingsDialog from './SettingsDialog';

const styles = theme => ({
});

class Settings extends Component {
  render() {
    return (
      <div>
        <List>
          <ListItem button
              onClick={this.props.openSettings}
          >
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
        <SettingsDialog onRequestClose={this.props.closeSettings}
            open={this.props.showModal}
        />
      </div>
    );
  }
}

export default compose(
  withStyles(styles, {
    withTheme: true,
  }),
  connect(state => ({
      showModal: state.settings.showModal,
    }),
    dispatch => ({
      openSettings: () => dispatch(openSettings()),
      closeSettings: () => dispatch(closeSettings()),
    })),
)(Settings);
